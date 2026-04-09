package com.littlegrow.app

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.littlegrow.app.data.ActivityDraft
import com.littlegrow.app.data.ActivityEntity
import com.littlegrow.app.data.AppDatabase
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.DiaperDraft
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.FeedingDraft
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.FeedingType
import com.littlegrow.app.data.GrowthDraft
import com.littlegrow.app.data.GrowthEntity
import com.littlegrow.app.data.HomeSummary
import com.littlegrow.app.data.LittleGrowRepository
import com.littlegrow.app.data.MedicalDraft
import com.littlegrow.app.data.MedicalEntity
import com.littlegrow.app.data.MilestoneDraft
import com.littlegrow.app.data.MilestoneEntity
import com.littlegrow.app.data.PreferencesRepository
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.SleepDraft
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.data.ThemeMode
import com.littlegrow.app.data.VaccineEntity
import com.littlegrow.app.data.toProfile
import com.littlegrow.app.export.writeCsvExport
import com.littlegrow.app.export.writePdfExport
import com.littlegrow.app.notifications.VaccineReminderScheduler
import com.littlegrow.app.widget.LittleGrowWidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId

data class BreastfeedingTimerState(
    val activeType: FeedingType? = null,
    val startedAtEpochMillis: Long? = null,
) {
    val isRunning: Boolean = activeType != null && startedAtEpochMillis != null
}

enum class AppLaunchState {
    LOADING,
    ONBOARDING,
    READY,
}

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository = LittleGrowRepository(
        appContext = application,
        database = AppDatabase.getInstance(application),
        preferencesRepository = PreferencesRepository(application),
    )

    private val selectedRecordTab = MutableStateFlow(RecordTab.FEEDING)
    private val _exportMessage = MutableStateFlow<String?>(null)
    private val _isExporting = MutableStateFlow(false)
    private val _breastfeedingTimer = MutableStateFlow(BreastfeedingTimerState())
    private val _pendingDestination = MutableStateFlow<AppDestination?>(null)
    private val _pendingRecordQuickAction = MutableStateFlow<RecordQuickAction?>(null)
    private val _launchState = MutableStateFlow(AppLaunchState.LOADING)

    val themeMode: StateFlow<ThemeMode> = repository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM,
    )

    val profile: StateFlow<BabyProfile?> = repository.profile
        .map { it?.toProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val feedings: StateFlow<List<FeedingEntity>> = repository.feedings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val sleeps: StateFlow<List<SleepEntity>> = repository.sleeps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val diapers: StateFlow<List<DiaperEntity>> = repository.diapers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val growthRecords: StateFlow<List<GrowthEntity>> = repository.growthRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val milestones: StateFlow<List<MilestoneEntity>> = repository.milestones.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val medicalRecords: StateFlow<List<MedicalEntity>> = repository.medicalRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val activityRecords: StateFlow<List<ActivityEntity>> = repository.activityRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val vaccines: StateFlow<List<VaccineEntity>> = repository.vaccines.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val vaccineRemindersEnabled: StateFlow<Boolean> = repository.vaccineRemindersEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )

    val currentRecordTab: StateFlow<RecordTab> = selectedRecordTab.asStateFlow()
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()
    val breastfeedingTimer: StateFlow<BreastfeedingTimerState> = _breastfeedingTimer.asStateFlow()
    val pendingDestination: StateFlow<AppDestination?> = _pendingDestination.asStateFlow()
    val pendingRecordQuickAction: StateFlow<RecordQuickAction?> = _pendingRecordQuickAction.asStateFlow()
    val launchState: StateFlow<AppLaunchState> = _launchState.asStateFlow()

    val homeSummary: StateFlow<HomeSummary> = combine(
        profile,
        feedings,
        sleeps,
        diapers,
        growthRecords,
    ) { profile, feedings, sleeps, diapers, growthRecords ->
        HomeSummaryInputs(
            profile = profile,
            feedings = feedings,
            sleeps = sleeps,
            diapers = diapers,
            growthRecords = growthRecords,
        )
    }.combine(milestones) { inputs, milestones ->
        buildHomeSummary(
            profile = inputs.profile,
            feedings = inputs.feedings,
            sleeps = inputs.sleeps,
            diapers = inputs.diapers,
            growth = inputs.growthRecords,
            milestones = milestones,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeSummary(
            profile = null,
            ageText = "请先填写宝宝资料",
            todayFeedings = 0,
            todayDiapers = 0,
            todaySleepMinutes = 0L,
            latestGrowth = null,
            latestMilestone = null,
            recentFeedings = emptyList(),
            recentSleeps = emptyList(),
            recentDiapers = emptyList(),
        ),
    )

    init {
        viewModelScope.launch {
            val hasProfile = repository.profile.first() != null
            val hasOnboarded = repository.onboardingCompleted.first()
            when {
                hasProfile -> {
                    if (!hasOnboarded) {
                        repository.setOnboardingCompleted()
                    }
                    _launchState.value = AppLaunchState.READY
                }

                hasOnboarded -> {
                    repository.seedIfNeeded()
                    _launchState.value = AppLaunchState.READY
                }

                else -> {
                    _launchState.value = AppLaunchState.ONBOARDING
                }
            }
            refreshVaccineReminders()
            refreshWidgets()
        }
    }

    fun handleLaunchIntent(intent: Intent?) {
        val target = intent?.toAppLaunchTarget() ?: return
        target.recordTab?.let { selectedRecordTab.value = it }
        _pendingDestination.value = target.destination
        _pendingRecordQuickAction.value = target.quickAction
    }

    fun consumePendingDestination() {
        _pendingDestination.value = null
    }

    fun consumePendingRecordQuickAction() {
        _pendingRecordQuickAction.value = null
    }

    fun selectRecordTab(tab: RecordTab) {
        selectedRecordTab.value = tab
    }

    fun saveProfile(profile: BabyProfile) {
        mutateData(refreshReminders = true) {
            repository.saveProfile(profile)
        }
    }

    fun addFeeding(draft: FeedingDraft) {
        mutateData {
            repository.addFeeding(draft)
        }
    }

    fun updateFeeding(
        id: Long,
        draft: FeedingDraft,
    ) {
        mutateData {
            repository.saveFeeding(recordId = id, draft = draft)
        }
    }

    fun deleteFeeding(id: Long) {
        mutateData {
            repository.deleteFeeding(id)
        }
    }

    fun startBreastfeedingTimer(type: FeedingType) {
        _breastfeedingTimer.value = BreastfeedingTimerState(
            activeType = type,
            startedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    fun cancelBreastfeedingTimer() {
        _breastfeedingTimer.value = BreastfeedingTimerState()
    }

    fun saveBreastfeedingTimer() {
        val state = _breastfeedingTimer.value
        val startedAt = state.startedAtEpochMillis ?: return
        val type = state.activeType ?: return
        val now = System.currentTimeMillis()
        val minutes = ((now - startedAt) / 60_000L).toInt().coerceAtLeast(1)
        val happenedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(startedAt),
            ZoneId.systemDefault(),
        )
        _breastfeedingTimer.value = BreastfeedingTimerState()
        mutateData {
            repository.addFeeding(
                FeedingDraft(
                    type = type,
                    happenedAt = happenedAt,
                    durationMinutes = minutes,
                    amountMl = null,
                    foodName = null,
                    photoPath = null,
                    note = null,
                ),
            )
        }
    }

    fun addSleep(draft: SleepDraft) {
        mutateData {
            repository.addSleep(draft)
        }
    }

    fun updateSleep(
        id: Long,
        draft: SleepDraft,
    ) {
        mutateData {
            repository.saveSleep(recordId = id, draft = draft)
        }
    }

    fun deleteSleep(id: Long) {
        mutateData {
            repository.deleteSleep(id)
        }
    }

    fun addDiaper(draft: DiaperDraft) {
        mutateData {
            repository.addDiaper(draft)
        }
    }

    fun updateDiaper(
        id: Long,
        draft: DiaperDraft,
    ) {
        mutateData {
            repository.saveDiaper(recordId = id, draft = draft)
        }
    }

    fun deleteDiaper(id: Long) {
        mutateData {
            repository.deleteDiaper(id)
        }
    }

    fun addGrowth(draft: GrowthDraft) {
        mutateData {
            repository.addGrowth(draft)
        }
    }

    fun updateGrowth(
        id: Long,
        draft: GrowthDraft,
    ) {
        mutateData {
            repository.saveGrowth(recordId = id, draft = draft)
        }
    }

    fun deleteGrowth(id: Long) {
        mutateData {
            repository.deleteGrowth(id)
        }
    }

    fun addMilestone(draft: MilestoneDraft) {
        mutateData {
            repository.addMilestone(draft)
        }
    }

    fun updateMilestone(
        id: Long,
        draft: MilestoneDraft,
    ) {
        mutateData {
            repository.saveMilestone(recordId = id, draft = draft)
        }
    }

    fun deleteMilestone(id: Long) {
        mutateData {
            repository.deleteMilestone(id)
        }
    }

    fun addMedical(draft: MedicalDraft) {
        mutateData {
            repository.addMedical(draft)
        }
    }

    fun updateMedical(
        id: Long,
        draft: MedicalDraft,
    ) {
        mutateData {
            repository.saveMedical(recordId = id, draft = draft)
        }
    }

    fun deleteMedical(id: Long) {
        mutateData {
            repository.deleteMedical(id)
        }
    }

    fun addActivity(draft: ActivityDraft) {
        mutateData {
            repository.addActivity(draft)
        }
    }

    fun updateActivity(
        id: Long,
        draft: ActivityDraft,
    ) {
        mutateData {
            repository.saveActivity(recordId = id, draft = draft)
        }
    }

    fun deleteActivity(id: Long) {
        mutateData {
            repository.deleteActivity(id)
        }
    }

    fun setVaccineStatus(
        scheduleKey: String,
        isDone: Boolean,
    ) {
        mutateData(refreshReminders = true) {
            repository.setVaccineStatus(scheduleKey, isDone)
        }
    }

    fun completeOnboarding(profile: BabyProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            repository.setOnboardingCompleted()
            _launchState.value = AppLaunchState.READY
            refreshVaccineReminders()
            refreshWidgets()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }

    fun setVaccineRemindersEnabled(enabled: Boolean) {
        repository.setVaccineRemindersEnabled(enabled)
        viewModelScope.launch {
            refreshVaccineReminders()
        }
    }

    fun exportCsv(uri: Uri) {
        exportFile(uri = uri, label = "CSV") { outputStream, snapshot ->
            writeCsvExport(outputStream, snapshot)
        }
    }

    fun exportPdf(uri: Uri) {
        exportFile(uri = uri, label = "PDF") { outputStream, snapshot ->
            writePdfExport(outputStream, snapshot)
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }

    private fun mutateData(
        refreshReminders: Boolean = false,
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            block()
            if (refreshReminders) {
                refreshVaccineReminders()
            }
            refreshWidgets()
        }
    }

    private suspend fun refreshVaccineReminders() {
        VaccineReminderScheduler.rescheduleAll(
            context = getApplication(),
            vaccines = repository.vaccines.first(),
            enabled = repository.vaccineRemindersEnabled.first(),
        )
    }

    private suspend fun refreshWidgets() {
        runCatching {
            LittleGrowWidgetUpdater.updateAll(getApplication())
        }
    }

    private fun exportFile(
        uri: Uri,
        label: String,
        writer: (java.io.OutputStream, com.littlegrow.app.data.ExportSnapshot) -> Unit,
    ) {
        viewModelScope.launch {
            _isExporting.value = true
            val result = runCatching {
                val snapshot = repository.buildExportSnapshot()
                val outputStream = getApplication<Application>().contentResolver.openOutputStream(uri)
                    ?: error("无法打开导出文件。")
                outputStream.use {
                    writer(it, snapshot)
                }
            }
            _isExporting.value = false
            _exportMessage.value = result.fold(
                onSuccess = { "$label 导出完成。" },
                onFailure = { "$label 导出失败：${it.message ?: "未知错误"}" },
            )
        }
    }

    private fun buildHomeSummary(
        profile: BabyProfile?,
        feedings: List<FeedingEntity>,
        sleeps: List<SleepEntity>,
        diapers: List<DiaperEntity>,
        growth: List<GrowthEntity>,
        milestones: List<MilestoneEntity>,
    ): HomeSummary {
        val today = LocalDate.now()
        return HomeSummary(
            profile = profile,
            ageText = profile?.birthday?.let(::formatAge) ?: "请先填写宝宝资料",
            todayFeedings = feedings.count { it.happenedAt.toLocalDate() == today },
            todayDiapers = diapers.count { it.happenedAt.toLocalDate() == today },
            todaySleepMinutes = calculateTodaySleepMinutes(sleeps, today),
            latestGrowth = growth.maxByOrNull { it.date },
            latestMilestone = milestones.maxByOrNull { it.achievedDate },
            recentFeedings = feedings.take(3),
            recentSleeps = sleeps.take(2),
            recentDiapers = diapers.take(3),
        )
    }

    private fun calculateTodaySleepMinutes(
        sleeps: List<SleepEntity>,
        today: LocalDate,
    ): Long {
        val todayStart = today.atStartOfDay()
        val todayEnd = today.plusDays(1).atStartOfDay()
        return sleeps.sumOf { sleep ->
            val overlapStart = maxOf(sleep.startTime, todayStart)
            val overlapEnd = minOf(sleep.endTime, todayEnd)
            Duration.between(overlapStart, overlapEnd).toMinutes().coerceAtLeast(0)
        }
    }

    private fun formatAge(birthday: LocalDate): String {
        val today = LocalDate.now()
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(birthday, today)
        return when {
            totalDays < 31 -> "出生第 ${totalDays + 1} 天"
            else -> {
                val period = Period.between(birthday, today)
                "${period.toTotalMonths()} 个月 ${period.days} 天"
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        ?: error("Application is required."),
                )
            }
        }
    }
}

private data class HomeSummaryInputs(
    val profile: BabyProfile?,
    val feedings: List<FeedingEntity>,
    val sleeps: List<SleepEntity>,
    val diapers: List<DiaperEntity>,
    val growthRecords: List<GrowthEntity>,
)
