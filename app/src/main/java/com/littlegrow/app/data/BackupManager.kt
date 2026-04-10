package com.littlegrow.app.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.littlegrow.app.BuildConfig
import com.littlegrow.app.media.PhotoStore
import java.io.File
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(
    private val context: Context,
    private val database: AppDatabase,
) {
    suspend fun exportBackup(
        uri: Uri,
        snapshot: ExportSnapshot,
    ) {
        val contentResolver = context.contentResolver
        val attachmentFiles = snapshot.collectPhotoPaths()
            .mapNotNull { path -> File(path).takeIf { it.exists() } }
            .associate { file -> file.absolutePath to "attachments/${file.name}" }
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            ZipOutputStream(outputStream).use { zip ->
                writeEntry(
                    zip = zip,
                    name = "meta.json",
                    content = JSONObject()
                        .put("versionName", BuildConfig.VERSION_NAME)
                        .put("databaseVersion", 5)
                        .put("generatedAt", snapshot.generatedAt.toString())
                        .put("babyName", snapshot.profile?.name ?: "")
                        .toString(2),
                )
                writeEntry(
                    zip = zip,
                    name = "snapshot.json",
                    content = snapshot.toJson(attachmentFiles).toString(2),
                )
                attachmentFiles.forEach { (path, archivePath) ->
                    zip.putNextEntry(ZipEntry(archivePath))
                    File(path).inputStream().use { input -> input.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        } ?: error("无法创建备份文件。")
    }

    suspend fun restoreBackup(uri: Uri) {
        val contentResolver = context.contentResolver
        val attachmentBytes = mutableMapOf<String, ByteArray>()
        val snapshotJson = buildString {
            contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        when {
                            entry.name == "snapshot.json" -> {
                                append(InputStreamReader(zip, Charsets.UTF_8).readText())
                            }
                            entry.name.startsWith("attachments/") -> {
                                attachmentBytes[entry.name] = zip.readBytes()
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            } ?: error("无法读取备份文件。")
        }
        if (snapshotJson.isBlank()) error("备份文件缺少 snapshot.json。")
        val restoredSnapshot = RestorableSnapshot.fromJson(
            context = context,
            json = JSONObject(snapshotJson),
            attachments = attachmentBytes,
        )
        database.withTransaction {
            database.clearAllTables()
            restoredSnapshot.profile?.let { database.babyDao().upsertProfile(it) }
            database.feedingDao().upsertAll(restoredSnapshot.feedings)
            database.sleepDao().upsertAll(restoredSnapshot.sleeps)
            database.diaperDao().upsertAll(restoredSnapshot.diapers)
            database.growthDao().upsertAll(restoredSnapshot.growthRecords)
            database.milestoneDao().upsertAll(restoredSnapshot.milestones)
            database.medicalDao().upsertAll(restoredSnapshot.medicalRecords)
            database.activityDao().upsertAll(restoredSnapshot.activityRecords)
            database.vaccineDao().upsertAll(restoredSnapshot.vaccines)
        }
    }

    private fun writeEntry(
        zip: ZipOutputStream,
        name: String,
        content: String,
    ) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
}

private data class RestorableSnapshot(
    val profile: BabyEntity?,
    val feedings: List<FeedingEntity>,
    val sleeps: List<SleepEntity>,
    val diapers: List<DiaperEntity>,
    val growthRecords: List<GrowthEntity>,
    val milestones: List<MilestoneEntity>,
    val medicalRecords: List<MedicalEntity>,
    val activityRecords: List<ActivityEntity>,
    val vaccines: List<VaccineEntity>,
) {
    companion object {
        fun fromJson(
            context: Context,
            json: JSONObject,
            attachments: Map<String, ByteArray>,
        ): RestorableSnapshot {
            val attachmentDir = PhotoStore.attachmentsDirectory(context)
            val restoreAttachment: (String?) -> String? = { archiveName ->
                archiveName?.takeIf { it.isNotBlank() }?.let { name ->
                    val bytes = attachments[name] ?: return@let null
                    val file = File(attachmentDir, name.substringAfterLast('/'))
                    file.outputStream().use { it.write(bytes) }
                    file.absolutePath
                }
            }
            val profileJson = json.optJSONObject("profile")
            return RestorableSnapshot(
                profile = profileJson?.let {
                    BabyEntity(
                        id = 1,
                        name = it.getString("name"),
                        birthday = java.time.LocalDate.parse(it.getString("birthday")),
                        gender = Gender.valueOf(it.getString("gender")),
                        avatarPath = restoreAttachment(it.optStringOrNull("avatarArchive")),
                    )
                },
                feedings = json.getJSONArray("feedings").toList { item ->
                    FeedingEntity(
                        id = item.getLong("id"),
                        type = FeedingType.valueOf(item.getString("type")),
                        happenedAt = java.time.LocalDateTime.parse(item.getString("happenedAt")),
                        durationMinutes = item.optIntOrNull("durationMinutes"),
                        amountMl = item.optIntOrNull("amountMl"),
                        foodName = item.optStringOrNull("foodName"),
                        photoPath = restoreAttachment(item.optStringOrNull("photoArchive")),
                        note = item.optStringOrNull("note"),
                        allergyObservation = AllergyStatus.valueOf(item.optString("allergyObservation", AllergyStatus.NONE.name)),
                        observationEndDate = item.optStringOrNull("observationEndDate")?.let(java.time.LocalDate::parse),
                        caregiver = item.optStringOrNull("caregiver"),
                    )
                },
                sleeps = json.getJSONArray("sleeps").toList { item ->
                    SleepEntity(
                        id = item.getLong("id"),
                        startTime = java.time.LocalDateTime.parse(item.getString("startTime")),
                        endTime = java.time.LocalDateTime.parse(item.getString("endTime")),
                        note = item.optStringOrNull("note"),
                        sleepType = SleepType.valueOf(item.optString("sleepType", SleepType.NAP.name)),
                        fallingAsleepMethod = item.optStringOrNull("fallingAsleepMethod")?.let(FallingAsleepMethod::valueOf),
                        caregiver = item.optStringOrNull("caregiver"),
                    )
                },
                diapers = json.getJSONArray("diapers").toList { item ->
                    DiaperEntity(
                        id = item.getLong("id"),
                        happenedAt = java.time.LocalDateTime.parse(item.getString("happenedAt")),
                        type = DiaperType.valueOf(item.getString("type")),
                        poopColor = item.optStringOrNull("poopColor")?.let(PoopColor::valueOf),
                        poopTexture = item.optStringOrNull("poopTexture")?.let(PoopTexture::valueOf),
                        note = item.optStringOrNull("note"),
                        photoPath = restoreAttachment(item.optStringOrNull("photoArchive")),
                        caregiver = item.optStringOrNull("caregiver"),
                    )
                },
                growthRecords = json.getJSONArray("growthRecords").toList { item ->
                    GrowthEntity(
                        id = item.getLong("id"),
                        date = java.time.LocalDate.parse(item.getString("date")),
                        weightKg = item.optFloatOrNull("weightKg"),
                        heightCm = item.optFloatOrNull("heightCm"),
                        headCircCm = item.optFloatOrNull("headCircCm"),
                    )
                },
                milestones = json.getJSONArray("milestones").toList { item ->
                    MilestoneEntity(
                        id = item.getLong("id"),
                        title = item.getString("title"),
                        category = MilestoneCategory.valueOf(item.getString("category")),
                        achievedDate = java.time.LocalDate.parse(item.getString("achievedDate")),
                        photoPath = restoreAttachment(item.optStringOrNull("photoArchive")),
                        note = item.optStringOrNull("note"),
                    )
                },
                medicalRecords = json.getJSONArray("medicalRecords").toList { item ->
                    MedicalEntity(
                        id = item.getLong("id"),
                        happenedAt = java.time.LocalDateTime.parse(item.getString("happenedAt")),
                        type = MedicalRecordType.valueOf(item.getString("type")),
                        title = item.getString("title"),
                        temperatureC = item.optFloatOrNull("temperatureC"),
                        dosage = item.optStringOrNull("dosage"),
                        note = item.optStringOrNull("note"),
                        caregiver = item.optStringOrNull("caregiver"),
                    )
                },
                activityRecords = json.getJSONArray("activityRecords").toList { item ->
                    ActivityEntity(
                        id = item.getLong("id"),
                        happenedAt = java.time.LocalDateTime.parse(item.getString("happenedAt")),
                        type = ActivityType.valueOf(item.getString("type")),
                        durationMinutes = item.optIntOrNull("durationMinutes"),
                        note = item.optStringOrNull("note"),
                        caregiver = item.optStringOrNull("caregiver"),
                    )
                },
                vaccines = json.getJSONArray("vaccines").toList { item ->
                    VaccineEntity(
                        scheduleKey = item.getString("scheduleKey"),
                        vaccineName = item.getString("vaccineName"),
                        doseNumber = item.getInt("doseNumber"),
                        category = VaccineCategory.valueOf(item.optString("category", VaccineCategory.NATIONAL.name)),
                        scheduledDate = java.time.LocalDate.parse(item.getString("scheduledDate")),
                        actualDate = item.optStringOrNull("actualDate")?.let(java.time.LocalDate::parse),
                        isDone = item.getBoolean("isDone"),
                        reactionNote = item.optStringOrNull("reactionNote"),
                        hadFever = item.optBoolean("hadFever", false),
                        reactionSeverity = item.optStringOrNull("reactionSeverity")?.let(ReactionSeverity::valueOf),
                    )
                },
            )
        }
    }
}

private fun ExportSnapshot.collectPhotoPaths(): Set<String> {
    return buildSet {
        profile?.avatarPath?.let(::add)
        feedings.mapNotNullTo(this) { it.photoPath }
        diapers.mapNotNullTo(this) { it.photoPath }
        milestones.mapNotNullTo(this) { it.photoPath }
    }
}

private fun ExportSnapshot.toJson(attachmentMap: Map<String, String>): JSONObject {
    return JSONObject().apply {
        put("profile", profile?.let {
            JSONObject()
                .put("name", it.name)
                .put("birthday", it.birthday.toString())
                .put("gender", it.gender.name)
                .put("avatarArchive", attachmentMap[it.avatarPath])
        })
        put("feedings", JSONArray(feedings.map { feeding ->
            JSONObject()
                .put("id", feeding.id)
                .put("type", feeding.type.name)
                .put("happenedAt", feeding.happenedAt.toString())
                .put("durationMinutes", feeding.durationMinutes)
                .put("amountMl", feeding.amountMl)
                .put("foodName", feeding.foodName)
                .put("photoArchive", attachmentMap[feeding.photoPath])
                .put("note", feeding.note)
                .put("allergyObservation", feeding.allergyObservation.name)
                .put("observationEndDate", feeding.observationEndDate?.toString())
                .put("caregiver", feeding.caregiver)
        }))
        put("sleeps", JSONArray(sleeps.map { sleep ->
            JSONObject()
                .put("id", sleep.id)
                .put("startTime", sleep.startTime.toString())
                .put("endTime", sleep.endTime.toString())
                .put("note", sleep.note)
                .put("sleepType", sleep.sleepType.name)
                .put("fallingAsleepMethod", sleep.fallingAsleepMethod?.name)
                .put("caregiver", sleep.caregiver)
        }))
        put("diapers", JSONArray(diapers.map { diaper ->
            JSONObject()
                .put("id", diaper.id)
                .put("happenedAt", diaper.happenedAt.toString())
                .put("type", diaper.type.name)
                .put("poopColor", diaper.poopColor?.name)
                .put("poopTexture", diaper.poopTexture?.name)
                .put("note", diaper.note)
                .put("photoArchive", attachmentMap[diaper.photoPath])
                .put("caregiver", diaper.caregiver)
        }))
        put("growthRecords", JSONArray(growthRecords.map { growth ->
            JSONObject()
                .put("id", growth.id)
                .put("date", growth.date.toString())
                .put("weightKg", growth.weightKg)
                .put("heightCm", growth.heightCm)
                .put("headCircCm", growth.headCircCm)
        }))
        put("milestones", JSONArray(milestones.map { milestone ->
            JSONObject()
                .put("id", milestone.id)
                .put("title", milestone.title)
                .put("category", milestone.category.name)
                .put("achievedDate", milestone.achievedDate.toString())
                .put("photoArchive", attachmentMap[milestone.photoPath])
                .put("note", milestone.note)
        }))
        put("medicalRecords", JSONArray(medicalRecords.map { medical ->
            JSONObject()
                .put("id", medical.id)
                .put("happenedAt", medical.happenedAt.toString())
                .put("type", medical.type.name)
                .put("title", medical.title)
                .put("temperatureC", medical.temperatureC)
                .put("dosage", medical.dosage)
                .put("note", medical.note)
                .put("caregiver", medical.caregiver)
        }))
        put("activityRecords", JSONArray(activityRecords.map { activity ->
            JSONObject()
                .put("id", activity.id)
                .put("happenedAt", activity.happenedAt.toString())
                .put("type", activity.type.name)
                .put("durationMinutes", activity.durationMinutes)
                .put("note", activity.note)
                .put("caregiver", activity.caregiver)
        }))
        put("vaccines", JSONArray(vaccines.map { vaccine ->
            JSONObject()
                .put("scheduleKey", vaccine.scheduleKey)
                .put("vaccineName", vaccine.vaccineName)
                .put("doseNumber", vaccine.doseNumber)
                .put("category", vaccine.category.name)
                .put("scheduledDate", vaccine.scheduledDate.toString())
                .put("actualDate", vaccine.actualDate?.toString())
                .put("isDone", vaccine.isDone)
                .put("reactionNote", vaccine.reactionNote)
                .put("hadFever", vaccine.hadFever)
                .put("reactionSeverity", vaccine.reactionSeverity?.name)
        }))
    }
}

private fun <T> JSONArray.toList(transform: (JSONObject) -> T): List<T> {
    return buildList {
        for (index in 0 until length()) {
            add(transform(getJSONObject(index)))
        }
    }
}

private fun JSONObject.optStringOrNull(key: String): String? {
    return if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
}

private fun JSONObject.optIntOrNull(key: String): Int? {
    return if (isNull(key)) null else optInt(key)
}

private fun JSONObject.optFloatOrNull(key: String): Float? {
    return if (isNull(key)) null else optDouble(key).toFloat()
}
