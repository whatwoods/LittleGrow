package com.littlegrow.app.data

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime

class CsvImporter(
    private val context: Context,
    private val database: AppDatabase,
) {
    suspend fun previewImport(uri: Uri): Map<String, Int> {
        return parseSections(uri).mapValues { (_, rows) -> rows.size }
    }

    suspend fun importCsv(uri: Uri): Int {
        val sections = parseSections(uri)
        var importedCount = 0

        val existingFeedings = database.feedingDao().getAll().map { "${it.happenedAt}|${it.type}|${it.amountMl}|${it.foodName}" }.toMutableSet()
        val existingSleeps = database.sleepDao().getAll().map { "${it.startTime}|${it.endTime}" }.toMutableSet()
        val existingDiapers = database.diaperDao().getAll().map { "${it.happenedAt}|${it.type}" }.toMutableSet()
        val existingGrowths = database.growthDao().getAll().map { it.date.toString() }.toMutableSet()
        val existingMilestones = database.milestoneDao().getAll().map { "${it.achievedDate}|${it.title}" }.toMutableSet()
        val existingMedical = database.medicalDao().getAll().map { "${it.happenedAt}|${it.type}|${it.title}" }.toMutableSet()
        val existingActivities = database.activityDao().getAll().map { "${it.happenedAt}|${it.type}|${it.durationMinutes}" }.toMutableSet()
        val existingVaccines = database.vaccineDao().getAll().associateBy { it.scheduleKey }.toMutableMap()

        sections["喂养记录"]?.forEach { row ->
            val happenedAt = row.getOrNull(0)?.takeIf { it.isNotBlank() }?.let(LocalDateTime::parse) ?: return@forEach
            val type = mapFeedingType(row.getOrNull(1)) ?: return@forEach
            val key = "$happenedAt|$type|${row.getOrNull(3)}|${row.getOrNull(4)}"
            if (!existingFeedings.add(key)) return@forEach
            database.feedingDao().upsert(
                FeedingEntity(
                    type = type,
                    happenedAt = happenedAt,
                    durationMinutes = row.getOrNull(2)?.toIntOrNull(),
                    amountMl = row.getOrNull(3)?.toIntOrNull(),
                    foodName = row.getOrNull(4)?.takeIf { it.isNotBlank() },
                    photoPath = row.getOrNull(5)?.takeIf { it.isNotBlank() },
                    note = row.getOrNull(6)?.takeIf { it.isNotBlank() },
                ),
            )
            importedCount += 1
        }
        sections["睡眠记录"]?.forEach { row ->
            val start = row.getOrNull(0)?.takeIf { it.isNotBlank() }?.let(LocalDateTime::parse) ?: return@forEach
            val end = row.getOrNull(1)?.takeIf { it.isNotBlank() }?.let(LocalDateTime::parse) ?: return@forEach
            val key = "$start|$end"
            if (!existingSleeps.add(key)) return@forEach
            database.sleepDao().upsert(SleepEntity(startTime = start, endTime = end, note = row.getOrNull(3)?.takeIf { it.isNotBlank() }))
            importedCount += 1
        }
        sections["排泄记录"]?.forEach { row ->
            val happenedAt = row.getOrNull(0)?.takeIf { it.isNotBlank() }?.let(LocalDateTime::parse) ?: return@forEach
            val type = mapDiaperType(row.getOrNull(1)) ?: return@forEach
            val key = "$happenedAt|$type"
            if (!existingDiapers.add(key)) return@forEach
            database.diaperDao().upsert(
                DiaperEntity(
                    happenedAt = happenedAt,
                    type = type,
                    poopColor = row.getOrNull(2)?.takeIf { it.isNotBlank() }?.let(::mapPoopColor),
                    poopTexture = row.getOrNull(3)?.takeIf { it.isNotBlank() }?.let(::mapPoopTexture),
                    note = row.getOrNull(4)?.takeIf { it.isNotBlank() },
                ),
            )
            importedCount += 1
        }
        sections["成长记录"]?.forEach { row ->
            val date = row.getOrNull(0)?.takeIf { it.isNotBlank() }?.let(LocalDate::parse) ?: return@forEach
            if (!existingGrowths.add(date.toString())) return@forEach
            database.growthDao().upsert(
                GrowthEntity(
                    date = date,
                    weightKg = row.getOrNull(1)?.toFloatOrNull(),
                    heightCm = row.getOrNull(2)?.toFloatOrNull(),
                    headCircCm = row.getOrNull(3)?.toFloatOrNull(),
                ),
            )
            importedCount += 1
        }
        sections["里程碑"]?.forEach { row ->
            val date = row.getOrNull(0)?.takeIf { it.isNotBlank() }?.let(LocalDate::parse) ?: return@forEach
            val title = row.getOrNull(2)?.takeIf { it.isNotBlank() } ?: return@forEach
            val key = "$date|$title"
            if (!existingMilestones.add(key)) return@forEach
            database.milestoneDao().upsert(
                MilestoneEntity(
                    title = title,
                    category = mapMilestoneCategory(row.getOrNull(1)) ?: MilestoneCategory.COGNITIVE,
                    achievedDate = date,
                    photoPath = row.getOrNull(3)?.takeIf { it.isNotBlank() },
                    note = row.getOrNull(4)?.takeIf { it.isNotBlank() },
                ),
            )
            importedCount += 1
        }
        sections["健康记录"]?.forEach { row ->
            val happenedAt = row.getOrNull(0)?.takeIf { it.isNotBlank() }?.let(LocalDateTime::parse) ?: return@forEach
            val type = mapMedicalType(row.getOrNull(1)) ?: return@forEach
            val title = row.getOrNull(2)?.takeIf { it.isNotBlank() } ?: return@forEach
            val key = "$happenedAt|$type|$title"
            if (!existingMedical.add(key)) return@forEach
            database.medicalDao().upsert(
                MedicalEntity(
                    happenedAt = happenedAt,
                    type = type,
                    title = title,
                    temperatureC = row.getOrNull(3)?.toFloatOrNull(),
                    dosage = row.getOrNull(4)?.takeIf { it.isNotBlank() },
                    note = row.getOrNull(5)?.takeIf { it.isNotBlank() },
                ),
            )
            importedCount += 1
        }
        sections["活动记录"]?.forEach { row ->
            val happenedAt = row.getOrNull(0)?.takeIf { it.isNotBlank() }?.let(LocalDateTime::parse) ?: return@forEach
            val type = mapActivityType(row.getOrNull(1)) ?: return@forEach
            val key = "$happenedAt|$type|${row.getOrNull(2)}"
            if (!existingActivities.add(key)) return@forEach
            database.activityDao().upsert(
                ActivityEntity(
                    happenedAt = happenedAt,
                    type = type,
                    durationMinutes = row.getOrNull(2)?.toIntOrNull(),
                    note = row.getOrNull(3)?.takeIf { it.isNotBlank() },
                ),
            )
            importedCount += 1
        }
        sections["疫苗计划"]?.forEach { row ->
            val scheduleKey = row.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@forEach
            if (scheduleKey in existingVaccines) return@forEach
            database.vaccineDao().upsertAll(
                listOf(
                    VaccineEntity(
                        scheduleKey = scheduleKey,
                        vaccineName = row.getOrNull(1).orEmpty(),
                        doseNumber = row.getOrNull(2)?.toIntOrNull() ?: 1,
                        scheduledDate = row.getOrNull(3)?.takeIf { it.isNotBlank() }?.let(LocalDate::parse) ?: LocalDate.now(),
                        actualDate = row.getOrNull(5)?.takeIf { it.isNotBlank() }?.let(LocalDate::parse),
                        isDone = row.getOrNull(4) == "已接种",
                    ),
                ),
            )
            importedCount += 1
        }
        return importedCount
    }

    private fun parseSections(uri: Uri): Map<String, List<List<String>>> {
        val sections = linkedMapOf<String, MutableList<List<String>>>()
        context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).useLines { lines ->
                var currentSection: String? = null
                var skipHeader = false
                lines.forEach { rawLine ->
                    val line = rawLine.removePrefix("\uFEFF")
                    if (line.isBlank()) {
                        currentSection = null
                        skipHeader = false
                        return@forEach
                    }
                    val cells = parseCsvLine(line)
                    if (cells.size == 1 && cells.first() != "LittleGrow 数据导出" && cells.first() != "生成时间") {
                        currentSection = cells.first()
                        sections.getOrPut(currentSection!!) { mutableListOf() }
                        skipHeader = true
                        return@forEach
                    }
                    if (skipHeader) {
                        skipHeader = false
                        return@forEach
                    }
                    currentSection?.let { sections.getValue(it).add(cells) }
                }
            }
        } ?: error("无法读取 CSV。")
        return sections
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' && inQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                    current.append('"')
                    index += 1
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
            index += 1
        }
        result += current.toString()
        return result
    }

    private fun mapFeedingType(value: String?): FeedingType? = FeedingType.entries.firstOrNull { it.label == value }
    private fun mapDiaperType(value: String?): DiaperType? = DiaperType.entries.firstOrNull { it.label == value }
    private fun mapPoopColor(value: String): PoopColor? = PoopColor.entries.firstOrNull { it.label == value }
    private fun mapPoopTexture(value: String): PoopTexture? = PoopTexture.entries.firstOrNull { it.label == value }
    private fun mapMilestoneCategory(value: String?): MilestoneCategory? = MilestoneCategory.entries.firstOrNull { it.label == value }
    private fun mapMedicalType(value: String?): MedicalRecordType? = MedicalRecordType.entries.firstOrNull { it.label == value }
    private fun mapActivityType(value: String?): ActivityType? = ActivityType.entries.firstOrNull { it.label == value }
}
