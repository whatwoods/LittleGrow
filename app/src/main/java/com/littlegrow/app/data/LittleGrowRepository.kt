package com.littlegrow.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime

class LittleGrowRepository(
    private val database: AppDatabase,
    private val preferencesRepository: PreferencesRepository,
) {
    val profile: Flow<BabyEntity?> = database.babyDao().observeProfile()
    val feedings: Flow<List<FeedingEntity>> = database.feedingDao().observeAll()
    val sleeps: Flow<List<SleepEntity>> = database.sleepDao().observeAll()
    val diapers: Flow<List<DiaperEntity>> = database.diaperDao().observeAll()
    val growthRecords: Flow<List<GrowthEntity>> = database.growthDao().observeAll()
    val milestones: Flow<List<MilestoneEntity>> = database.milestoneDao().observeAll()
    val vaccines: Flow<List<VaccineEntity>> = database.vaccineDao().observeAll()
    val themeMode: Flow<ThemeMode> = preferencesRepository.themeMode
    val vaccineRemindersEnabled: Flow<Boolean> = preferencesRepository.vaccineRemindersEnabled

    suspend fun saveProfile(profile: BabyProfile) {
        database.babyDao().upsertProfile(
            BabyEntity(
                name = profile.name.trim(),
                birthday = profile.birthday,
                gender = profile.gender,
            ),
        )
        syncVaccinesForBirthday(profile.birthday)
    }

    suspend fun saveFeeding(
        recordId: Long?,
        draft: FeedingDraft,
    ) {
        database.feedingDao().upsert(
            FeedingEntity(
                id = recordId ?: 0,
                type = draft.type,
                happenedAt = draft.happenedAt,
                durationMinutes = draft.durationMinutes,
                amountMl = draft.amountMl,
                foodName = draft.foodName?.trim()?.takeIf { it.isNotEmpty() },
                note = draft.note?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
    }

    suspend fun addFeeding(draft: FeedingDraft) {
        saveFeeding(recordId = null, draft = draft)
    }

    suspend fun deleteFeeding(id: Long) {
        database.feedingDao().deleteById(id)
    }

    suspend fun saveSleep(
        recordId: Long?,
        draft: SleepDraft,
    ) {
        database.sleepDao().upsert(
            SleepEntity(
                id = recordId ?: 0,
                startTime = draft.startTime,
                endTime = draft.endTime,
                note = draft.note?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
    }

    suspend fun addSleep(draft: SleepDraft) {
        saveSleep(recordId = null, draft = draft)
    }

    suspend fun deleteSleep(id: Long) {
        database.sleepDao().deleteById(id)
    }

    suspend fun saveDiaper(
        recordId: Long?,
        draft: DiaperDraft,
    ) {
        database.diaperDao().upsert(
            DiaperEntity(
                id = recordId ?: 0,
                happenedAt = draft.happenedAt,
                type = draft.type,
                poopColor = draft.poopColor,
                poopTexture = draft.poopTexture,
                note = draft.note?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
    }

    suspend fun addDiaper(draft: DiaperDraft) {
        saveDiaper(recordId = null, draft = draft)
    }

    suspend fun deleteDiaper(id: Long) {
        database.diaperDao().deleteById(id)
    }

    suspend fun saveGrowth(
        recordId: Long?,
        draft: GrowthDraft,
    ) {
        database.growthDao().upsert(
            GrowthEntity(
                id = recordId ?: 0,
                date = draft.date,
                weightKg = draft.weightKg,
                heightCm = draft.heightCm,
                headCircCm = draft.headCircCm,
            ),
        )
    }

    suspend fun addGrowth(draft: GrowthDraft) {
        saveGrowth(recordId = null, draft = draft)
    }

    suspend fun deleteGrowth(id: Long) {
        database.growthDao().deleteById(id)
    }

    suspend fun saveMilestone(
        recordId: Long?,
        draft: MilestoneDraft,
    ) {
        database.milestoneDao().upsert(
            MilestoneEntity(
                id = recordId ?: 0,
                title = draft.title.trim(),
                category = draft.category,
                achievedDate = draft.achievedDate,
                note = draft.note?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
    }

    suspend fun addMilestone(draft: MilestoneDraft) {
        saveMilestone(recordId = null, draft = draft)
    }

    suspend fun deleteMilestone(id: Long) {
        database.milestoneDao().deleteById(id)
    }

    fun setThemeMode(mode: ThemeMode) {
        preferencesRepository.setThemeMode(mode)
    }

    fun setVaccineRemindersEnabled(enabled: Boolean) {
        preferencesRepository.setVaccineRemindersEnabled(enabled)
    }

    suspend fun syncVaccinesForBirthday(birthday: LocalDate) {
        val existing = database.vaccineDao().getAll()
        database.vaccineDao().upsertAll(
            buildNationalVaccineSchedule(
                birthday = birthday,
                existing = existing,
            ),
        )
    }

    suspend fun setVaccineStatus(
        scheduleKey: String,
        isDone: Boolean,
        actualDate: LocalDate? = if (isDone) LocalDate.now() else null,
    ) {
        database.vaccineDao().updateStatus(scheduleKey, isDone, actualDate)
    }

    suspend fun buildExportSnapshot(): ExportSnapshot {
        return ExportSnapshot(
            generatedAt = LocalDateTime.now(),
            profile = profile.first()?.toProfile(),
            feedings = feedings.first(),
            sleeps = sleeps.first(),
            diapers = diapers.first(),
            growthRecords = growthRecords.first(),
            milestones = milestones.first(),
            vaccines = vaccines.first(),
        )
    }

    suspend fun seedIfNeeded() {
        val today = LocalDate.now()
        val birthday = today.minusMonths(4).minusDays(3)
        saveProfile(
            BabyProfile(
                name = "小小芽",
                birthday = birthday,
                gender = Gender.GIRL,
            ),
        )
        addGrowth(
            GrowthDraft(
                date = today.minusMonths(2),
                weightKg = 5.8f,
                heightCm = 59.2f,
                headCircCm = 39.0f,
            ),
        )
        addGrowth(
            GrowthDraft(
                date = today.minusMonths(1),
                weightKg = 6.4f,
                heightCm = 62.1f,
                headCircCm = 40.3f,
            ),
        )
        addGrowth(
            GrowthDraft(
                date = today,
                weightKg = 6.9f,
                heightCm = 64.0f,
                headCircCm = 41.0f,
            ),
        )
        addMilestone(
            MilestoneDraft(
                title = "会追视玩具了",
                category = MilestoneCategory.COGNITIVE,
                achievedDate = today.minusDays(6),
                note = "会盯着摇铃看很久。",
            ),
        )
        setVaccineStatus("hepb_1", true, birthday)
        setVaccineStatus("bcg_1", true, birthday.plusDays(1))
        setVaccineStatus("hepb_2", true, birthday.plusMonths(1))
        setVaccineStatus("polio_1", true, birthday.plusMonths(2))
        setVaccineStatus("dtap_1", true, birthday.plusMonths(3))
    }
}
