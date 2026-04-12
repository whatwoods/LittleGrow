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
import com.littlegrow.app.data.AgeBasedReference
import com.littlegrow.app.data.AppDatabase
import com.littlegrow.app.data.AppTheme
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.BackupFrequency
import com.littlegrow.app.data.BackupManager
import com.littlegrow.app.data.CsvImporter
import com.littlegrow.app.data.DiaperDraft
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.EncouragementProvider
import com.littlegrow.app.data.FeedingDraft
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.FeedingType
import com.littlegrow.app.data.GrowthDraft
import com.littlegrow.app.data.GrowthEntity
import com.littlegrow.app.data.HandoverSummary
import com.littlegrow.app.data.HandoverSummaryGenerator
import com.littlegrow.app.data.HomeModule
import com.littlegrow.app.data.HomeSummary
import com.littlegrow.app.data.LittleGrowRepository
import com.littlegrow.app.data.MedicalDraft
import com.littlegrow.app.data.MedicalEntity
import com.littlegrow.app.data.MedicalSummary
import com.littlegrow.app.data.MedicalSummaryGenerator
import com.littlegrow.app.data.MemorySnapshot
import com.littlegrow.app.data.MilestoneDraft
import com.littlegrow.app.data.MilestoneEntity
import com.littlegrow.app.data.MonthlyGuide
import com.littlegrow.app.data.MonthlyGuideEntry
import com.littlegrow.app.data.PreferencesRepository
import com.littlegrow.app.data.RecordTab
import com.littlegrow.app.data.SmartDefaultsProvider
import com.littlegrow.app.data.SleepDraft
import com.littlegrow.app.data.SleepEntity
import com.littlegrow.app.data.StageConfigProvider
import com.littlegrow.app.data.StageReportEntry
import com.littlegrow.app.data.StageReportGenerator
import com.littlegrow.app.data.ThemeMode
import com.littlegrow.app.data.TrendAnalyzer
import com.littlegrow.app.data.TrendInsight
import com.littlegrow.app.data.VaccineEntity
import com.littlegrow.app.data.VaccineReactionDraft
import com.littlegrow.app.data.toProfile
import com.littlegrow.app.data.AutoBackupWorker
import com.littlegrow.app.export.writeCsvExport
import com.littlegrow.app.export.writePdfExport
import com.littlegrow.app.notifications.AnomalyChecker
import com.littlegrow.app.notifications.QuickActionNotificationController
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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
    private val smartDefaultsProvider = SmartDefaultsProvider()
    private val backupManager = BackupManager(application, AppDatabase.getInstance(application))
    private val csvImporter = CsvImporter(application, AppDatabase.getInstance(application))
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
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _medicalSummary = MutableStateFlow<MedicalSummary?>(null)
    private val _handoverSummary = MutableStateFlow<HandoverSummary?>(null)
    private val _homeCaregiverFilter = MutableStateFlow<String?>(null)
    private val _refreshing = MutableStateFlow(false)

    val themeMode: StateFlow<ThemeMode> = repository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM,
    )

    val appTheme: StateFlow<AppTheme> = repository.appTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppTheme.EARTHY,
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

    val quickActionNotificationsEnabled: StateFlow<Boolean> = repository.quickActionNotificationsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val anomalyRemindersEnabled = repository.anomalyRemindersEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val diaperRemindersEnabled = repository.diaperRemindersEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val largeTextModeEnabled = repository.largeTextModeEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val darkModeScheduleEnabled = repository.darkModeScheduleEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    val darkModeStartHour = repository.darkModeStartHour.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 20,
    )

    val darkModeEndHour = repository.darkModeEndHour.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 7,
    )

    val homeModules = repository.homeModules.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet(),
    )

    val caregivers = repository.caregivers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = listOf("妈妈", "爸爸"),
    )

    val currentCaregiver = repository.currentCaregiver.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = "妈妈",
    )

    val autoBackupFrequency = repository.autoBackupFrequency.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BackupFrequency.OFF,
    )

    private val seenGuides = repository.seenGuides.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet(),
    )

    private val shownCelebrations = repository.shownCelebrations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet(),
    )

    val currentRecordTab: StateFlow<RecordTab> = selectedRecordTab.asStateFlow()
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()
    val breastfeedingTimer: StateFlow<BreastfeedingTimerState> = _breastfeedingTimer.asStateFlow()
    val pendingDestination: StateFlow<AppDestination?> = _pendingDestination.asStateFlow()
    val pendingRecordQuickAction: StateFlow<RecordQuickAction?> = _pendingRecordQuickAction.asStateFlow()
    val launchState: StateFlow<AppLaunchState> = _launchState.asStateFlow()
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()
    val medicalSummary: StateFlow<MedicalSummary?> = _medicalSummary.asStateFlow()
    val handoverSummary: StateFlow<HandoverSummary?> = _handoverSummary.asStateFlow()
    val homeCaregiverFilter: StateFlow<String?> = _homeCaregiverFilter.asStateFlow()
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val ageMonths = profile.map { baby ->
        baby?.birthday?.let { ChronoUnit.MONTHS.between(it, LocalDate.now()).toInt() } ?: 0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
    )

    val orderedRecordTabs = ageMonths.map { age ->
        StageConfigProvider.forAgeMonths(age).prioritizedTabs
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordTab.entries,
    )

    val activeHomeModules = combine(ageMonths, homeModules) { age, modules ->
        val stageModules = StageConfigProvider.forAgeMonths(age).prioritizedModules
        val visible = modules.ifEmpty { stageModules.toSet() }
        (stageModules + HomeModule.entries).distinct().filter { it in visible }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeModule.entries,
    )

    val feedingFormDefaults = combine(profile, feedings) { profile, feedings ->
        smartDefaultsProvider.buildFeedingDefaults(
            profile = profile,
            feedings = feedings,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = smartDefaultsProvider.buildFeedingDefaults(
            profile = null,
            feedings = emptyList(),
        ),
    )

    private val homeSummaryInputs = combine(
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
    }

    val homeSummary: StateFlow<HomeSummary> = homeSummaryInputs
        .combine(milestones) { inputs, milestones -> inputs to milestones }
        .combine(homeCaregiverFilter) { (inputs, milestones), caregiverFilter ->
        buildHomeSummary(
            profile = inputs.profile,
            feedings = inputs.feedings,
            sleeps = inputs.sleeps,
            diapers = inputs.diapers,
            growth = inputs.growthRecords,
            milestones = milestones,
            caregiverFilter = caregiverFilter,
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
            todaySleepReference = null,
            todayFeedingReference = null,
            latestGrowth = null,
            latestMilestone = null,
            recentFeedings = emptyList(),
            recentSleeps = emptyList(),
            recentDiapers = emptyList(),
        ),
    )

    val weeklyTrends: StateFlow<List<TrendInsight>> = combine(feedings, sleeps) { feedings, sleeps ->
        TrendAnalyzer.weeklyTrends(LocalDate.now(), feedings, sleeps)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val routineInsights = combine(feedings, sleeps) { feedings, sleeps ->
        TrendAnalyzer.routineInsights(feedings, sleeps)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val encouragementText = combine(homeSummary, feedings) { summary, feedings ->
        EncouragementProvider.build(
            todayFeedings = summary.todayFeedings,
            todaySleepMinutes = summary.todaySleepMinutes,
            consecutiveDays = consecutiveRecordedDays(feedings),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = "",
    )

    val monthlyGuide = combine(ageMonths, seenGuides) { age, seenGuides ->
        MonthlyGuide.guideFor(age)?.takeIf { guide -> guide.month.toString() !in seenGuides }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val stageReportRecordInputs = combine(
        feedings,
        sleeps,
        diapers,
        growthRecords,
        milestones,
    ) { feedings, sleeps, diapers, growthRecords, milestones ->
        StageReportRecordInputs(
            feedings = feedings,
            sleeps = sleeps,
            diapers = diapers,
            growthRecords = growthRecords,
            milestones = milestones,
        )
    }

    val pendingStageReport = combine(profile, stageReportRecordInputs, shownCelebrations) { profile, inputs, shown ->
        val ageDays = profile?.birthday
            ?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).toInt() }
            ?: return@combine null
        StageReportGenerator.pendingReport(
            birthday = profile.birthday,
            ageDays = ageDays,
            shownKeys = shown,
            feedings = inputs.feedings,
            sleeps = inputs.sleeps,
            diapers = inputs.diapers,
            growthRecords = inputs.growthRecords,
            milestones = inputs.milestones,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val memoryOfTheDay = combine(profile, feedings, sleeps, milestones) { profile, feedings, sleeps, milestones ->
        buildMemoryOfTheDay(profile, feedings, sleeps, milestones)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val nightWakeCount = sleeps.map { computeNightWakeCount(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
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
            refreshQuickActionNotifications()
            refreshBackgroundWorkers()
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
        addRecord(draft, LittleGrowRepository::addFeeding)
    }

    fun updateFeeding(
        id: Long,
        draft: FeedingDraft,
    ) {
        updateRecord(id, draft, LittleGrowRepository::saveFeeding)
    }

    fun deleteFeeding(id: Long) {
        deleteRecord(id, LittleGrowRepository::deleteFeeding)
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
        mutateData {
            saveBreastfeedingTimerIfRunning()
        }
    }

    fun addSleep(draft: SleepDraft) {
        mutateData {
            val autoStopped = saveBreastfeedingTimerIfRunning()
            repository.addSleep(draft)
            if (autoStopped) {
                _userMessage.value = "已自动结束喂奶记录。"
            }
        }
    }

    fun updateSleep(
        id: Long,
        draft: SleepDraft,
    ) {
        updateRecord(id, draft, LittleGrowRepository::saveSleep)
    }

    fun deleteSleep(id: Long) {
        deleteRecord(id, LittleGrowRepository::deleteSleep)
    }

    fun addDiaper(draft: DiaperDraft) {
        addRecord(draft, LittleGrowRepository::addDiaper)
    }

    fun updateDiaper(
        id: Long,
        draft: DiaperDraft,
    ) {
        updateRecord(id, draft, LittleGrowRepository::saveDiaper)
    }

    fun deleteDiaper(id: Long) {
        deleteRecord(id, LittleGrowRepository::deleteDiaper)
    }

    fun addGrowth(draft: GrowthDraft) {
        addRecord(draft, LittleGrowRepository::addGrowth)
    }

    fun updateGrowth(
        id: Long,
        draft: GrowthDraft,
    ) {
        updateRecord(id, draft, LittleGrowRepository::saveGrowth)
    }

    fun deleteGrowth(id: Long) {
        deleteRecord(id, LittleGrowRepository::deleteGrowth)
    }

    fun addMilestone(draft: MilestoneDraft) {
        addRecord(draft, LittleGrowRepository::addMilestone)
    }

    fun updateMilestone(
        id: Long,
        draft: MilestoneDraft,
    ) {
        updateRecord(id, draft, LittleGrowRepository::saveMilestone)
    }

    fun deleteMilestone(id: Long) {
        deleteRecord(id, LittleGrowRepository::deleteMilestone)
    }

    fun addMedical(draft: MedicalDraft) {
        addRecord(draft, LittleGrowRepository::addMedical)
    }

    fun updateMedical(
        id: Long,
        draft: MedicalDraft,
    ) {
        updateRecord(id, draft, LittleGrowRepository::saveMedical)
    }

    fun deleteMedical(id: Long) {
        deleteRecord(id, LittleGrowRepository::deleteMedical)
    }

    fun addActivity(draft: ActivityDraft) {
        addRecord(draft, LittleGrowRepository::addActivity)
    }

    fun updateActivity(
        id: Long,
        draft: ActivityDraft,
    ) {
        updateRecord(id, draft, LittleGrowRepository::saveActivity)
    }

    fun deleteActivity(id: Long) {
        deleteRecord(id, LittleGrowRepository::deleteActivity)
    }

    fun setVaccineStatus(
        scheduleKey: String,
        isDone: Boolean,
    ) {
        mutateData(refreshReminders = true) {
            repository.setVaccineStatus(scheduleKey, isDone)
        }
    }

    fun updateVaccineReaction(
        scheduleKey: String,
        draft: VaccineReactionDraft,
    ) {
        mutateData {
            repository.updateVaccineReaction(scheduleKey, draft)
        }
    }

    fun completeOnboarding(profile: BabyProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            repository.setOnboardingCompleted()
            _launchState.value = AppLaunchState.READY
            _homeCaregiverFilter.value = null
            refreshVaccineReminders()
            refreshWidgets()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }

    fun setAppTheme(theme: AppTheme) {
        repository.setAppTheme(theme)
    }

    fun setVaccineRemindersEnabled(enabled: Boolean) {
        repository.setVaccineRemindersEnabled(enabled)
        viewModelScope.launch {
            refreshVaccineReminders()
        }
    }

    fun setQuickActionNotificationsEnabled(enabled: Boolean) {
        repository.setQuickActionNotificationsEnabled(enabled)
        viewModelScope.launch {
            refreshQuickActionNotifications()
        }
    }

    fun setAnomalyRemindersEnabled(enabled: Boolean) {
        repository.setAnomalyRemindersEnabled(enabled)
        viewModelScope.launch {
            refreshBackgroundWorkers()
        }
    }

    fun setDiaperRemindersEnabled(enabled: Boolean) {
        repository.setDiaperRemindersEnabled(enabled)
        viewModelScope.launch {
            refreshBackgroundWorkers()
        }
    }

    fun setLargeTextModeEnabled(enabled: Boolean) {
        repository.setLargeTextModeEnabled(enabled)
    }

    fun setDarkModeSchedule(
        enabled: Boolean,
        startHour: Int,
        endHour: Int,
    ) {
        repository.setDarkModeSchedule(enabled, startHour, endHour)
    }

    fun setHomeModules(modules: Set<HomeModule>) {
        repository.setHomeModules(modules)
    }

    fun setCaregivers(raw: String) {
        repository.setCaregivers(
            raw.split('、', ',', '，', '\n')
                .map { it.trim() }
                .filter { it.isNotEmpty() },
        )
    }

    fun setCurrentCaregiver(name: String) {
        repository.setCurrentCaregiver(name)
    }

    fun setHomeCaregiverFilter(name: String?) {
        _homeCaregiverFilter.value = name?.takeIf { it.isNotBlank() }
    }

    fun refresh() {
        if (_refreshing.value) return
        viewModelScope.launch {
            _refreshing.value = true
            delay(300)
            _refreshing.value = false
        }
    }

    fun setAutoBackupFrequency(frequency: BackupFrequency) {
        repository.setAutoBackupFrequency(frequency)
        viewModelScope.launch {
            refreshBackgroundWorkers()
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

    fun consumeUserMessage() {
        _userMessage.value = null
    }

    fun dismissMonthlyGuide(month: Int) {
        repository.markGuideSeen(month)
    }

    fun dismissStageReport(day: Int) {
        repository.markCelebrationShown(day.toString())
    }

    fun buildMedicalSummary(days: Long = 30) {
        _medicalSummary.value = MedicalSummaryGenerator.build(
            profile = profile.value,
            days = days,
            now = LocalDate.now(),
            feedings = feedings.value,
            sleeps = sleeps.value,
            diapers = diapers.value,
            medicalRecords = medicalRecords.value,
            growthRecords = growthRecords.value,
        )
    }

    fun clearMedicalSummary() {
        _medicalSummary.value = null
    }

    fun buildHandoverSummary(from: LocalDateTime) {
        _handoverSummary.value = HandoverSummaryGenerator.build(
            from = from,
            to = LocalDateTime.now(),
            feedings = feedings.value,
            sleeps = sleeps.value,
            diapers = diapers.value,
            medicalRecords = medicalRecords.value,
            activityRecords = activityRecords.value,
        )
    }

    fun clearHandoverSummary() {
        _handoverSummary.value = null
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _isExporting.value = true
            val result = runCatching {
                backupManager.exportBackup(uri, repository.buildExportSnapshot())
            }
            _isExporting.value = false
            _exportMessage.value = result.fold(
                onSuccess = { "完整备份已导出。" },
                onFailure = { "备份导出失败：${it.message ?: "未知错误"}" },
            )
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            _isExporting.value = true
            val result = runCatching {
                backupManager.restoreBackup(uri)
                refreshVaccineReminders()
                refreshBackgroundWorkers()
                refreshWidgets()
            }
            _isExporting.value = false
            _exportMessage.value = result.fold(
                onSuccess = { "备份恢复完成。" },
                onFailure = { "备份恢复失败：${it.message ?: "未知错误"}" },
            )
        }
    }

    fun importCsv(uri: Uri) {
        viewModelScope.launch {
            _isExporting.value = true
            val result = runCatching {
                val count = csvImporter.importCsv(uri)
                refreshVaccineReminders()
                refreshWidgets()
                count
            }
            _isExporting.value = false
            _exportMessage.value = result.fold(
                onSuccess = { "CSV 已导入 $it 条记录。" },
                onFailure = { "CSV 导入失败：${it.message ?: "未知错误"}" },
            )
        }
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

    private fun <Draft> addRecord(
        draft: Draft,
        operation: suspend LittleGrowRepository.(Draft) -> Unit,
    ) {
        mutateData {
            operation(repository, draft)
        }
    }

    private fun <Draft> updateRecord(
        id: Long,
        draft: Draft,
        operation: suspend LittleGrowRepository.(Long?, Draft) -> Unit,
    ) {
        mutateData {
            operation(repository, id, draft)
        }
    }

    private fun deleteRecord(
        id: Long,
        operation: suspend LittleGrowRepository.(Long) -> Unit,
    ) {
        mutateData {
            operation(repository, id)
        }
    }

    private suspend fun refreshVaccineReminders() {
        VaccineReminderScheduler.rescheduleAll(
            context = getApplication(),
            vaccines = repository.vaccines.first(),
            enabled = repository.vaccineRemindersEnabled.first(),
        )
    }

    private suspend fun refreshQuickActionNotifications() {
        QuickActionNotificationController.sync(
            context = getApplication(),
            enabled = repository.quickActionNotificationsEnabled.first(),
        )
    }

    private suspend fun refreshBackgroundWorkers() {
        val anomalyEnabled = repository.anomalyRemindersEnabled.first() || repository.diaperRemindersEnabled.first()
        AnomalyChecker.sync(
            context = getApplication(),
            enabled = anomalyEnabled,
        )
        AutoBackupWorker.sync(
            context = getApplication(),
            frequency = repository.autoBackupFrequency.first(),
        )
    }

    private suspend fun refreshWidgets() {
        runCatching {
            LittleGrowWidgetUpdater.updateAll(getApplication())
        }
    }

    private suspend fun saveBreastfeedingTimerIfRunning(): Boolean {
        val state = _breastfeedingTimer.value
        val startedAt = state.startedAtEpochMillis ?: return false
        val type = state.activeType ?: return false
        val now = System.currentTimeMillis()
        val minutes = ((now - startedAt) / 60_000L).toInt().coerceAtLeast(1)
        val happenedAt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(startedAt),
            ZoneId.systemDefault(),
        )
        _breastfeedingTimer.value = BreastfeedingTimerState()
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
        return true
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
        caregiverFilter: String?,
    ): HomeSummary {
        val today = LocalDate.now()
        val filteredFeedings = feedings.filterFeedingsByCaregiver(caregiverFilter)
        val filteredSleeps = sleeps.filterSleepsByCaregiver(caregiverFilter)
        val filteredDiapers = diapers.filterDiapersByCaregiver(caregiverFilter)
        return HomeSummary(
            profile = profile,
            ageText = profile?.birthday?.let(::formatAge) ?: "请先填写宝宝资料",
            todayFeedings = filteredFeedings.count { it.happenedAt.toLocalDate() == today },
            todayDiapers = filteredDiapers.count { it.happenedAt.toLocalDate() == today },
            todaySleepMinutes = calculateTodaySleepMinutes(filteredSleeps, today),
            todaySleepReference = profile?.birthday?.let {
                val ageMonths = ChronoUnit.MONTHS.between(it, today).toInt()
                val range = AgeBasedReference.sleepHoursPerDay(ageMonths)
                "参考 ${range.min.toInt()}-${range.max.toInt()} ${range.unit}"
            },
            todayFeedingReference = profile?.birthday?.let {
                val ageMonths = ChronoUnit.MONTHS.between(it, today).toInt()
                val range = AgeBasedReference.feedingTimesPerDay(ageMonths)
                "参考 ${range.min.toInt()}-${range.max.toInt()} ${range.unit}"
            },
            latestGrowth = growth.maxByOrNull { it.date },
            latestMilestone = milestones.maxByOrNull { it.achievedDate },
            recentFeedings = filteredFeedings.take(3),
            recentSleeps = filteredSleeps.take(2),
            recentDiapers = filteredDiapers.take(3),
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

    private fun consecutiveRecordedDays(feedings: List<FeedingEntity>): Int {
        if (feedings.isEmpty()) return 0
        val dates = feedings.map { it.happenedAt.toLocalDate() }.distinct().sortedDescending()
        var count = 0
        var expected = LocalDate.now()
        while (expected in dates) {
            count += 1
            expected = expected.minusDays(1)
        }
        return count
    }

    private fun computeNightWakeCount(sleeps: List<SleepEntity>): Int {
        val targetDate = LocalDate.now().minusDays(1)
        val nightSleeps = sleeps
            .filter { it.sleepType == com.littlegrow.app.data.SleepType.NIGHT_SLEEP && it.startTime.toLocalDate() >= targetDate.minusDays(1) }
            .sortedBy { it.startTime }
        if (nightSleeps.size < 2) return 0
        return nightSleeps.zipWithNext().count { (previous, next) ->
            Duration.between(previous.endTime, next.startTime).toMinutes() > 10
        }
    }

    private fun buildMemoryOfTheDay(
        profile: BabyProfile?,
        feedings: List<FeedingEntity>,
        sleeps: List<SleepEntity>,
        milestones: List<MilestoneEntity>,
    ): MemorySnapshot? {
        val baby = profile ?: return null
        if (ChronoUnit.DAYS.between(baby.birthday, LocalDate.now()) < 365) return null
        val target = LocalDate.now().minusYears(1)
        val targetFeedings = feedings.filter { it.happenedAt.toLocalDate() == target }
        val targetSleeps = sleeps.filter { it.startTime.toLocalDate() == target }
        val targetMilestones = milestones.filter { it.achievedDate == target }
        if (targetFeedings.isEmpty() && targetSleeps.isEmpty() && targetMilestones.isEmpty()) return null
        return MemorySnapshot(
            title = "一年前的今天",
            lines = buildList {
                add("${targetFeedings.size} 次喂养")
                add("${targetSleeps.size} 段睡眠")
                if (targetMilestones.isNotEmpty()) add("当天达成里程碑：${targetMilestones.joinToString { it.title }}")
            },
            photoPaths = targetMilestones.mapNotNull { it.photoPath },
        )
    }
}

private data class HomeSummaryInputs(
    val profile: BabyProfile?,
    val feedings: List<FeedingEntity>,
    val sleeps: List<SleepEntity>,
    val diapers: List<DiaperEntity>,
    val growthRecords: List<GrowthEntity>,
)

private data class StageReportRecordInputs(
    val feedings: List<FeedingEntity>,
    val sleeps: List<SleepEntity>,
    val diapers: List<DiaperEntity>,
    val growthRecords: List<GrowthEntity>,
    val milestones: List<MilestoneEntity>,
)

private fun List<FeedingEntity>.filterFeedingsByCaregiver(caregiver: String?): List<FeedingEntity> {
    return if (caregiver == null) this else filter { it.caregiver == caregiver }
}

private fun List<SleepEntity>.filterSleepsByCaregiver(caregiver: String?): List<SleepEntity> {
    return if (caregiver == null) this else filter { it.caregiver == caregiver }
}

private fun List<DiaperEntity>.filterDiapersByCaregiver(caregiver: String?): List<DiaperEntity> {
    return if (caregiver == null) this else filter { it.caregiver == caregiver }
}
