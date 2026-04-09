package com.littlegrow.app

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.littlegrow.app.data.AppDatabase
import com.littlegrow.app.data.BabyEntity
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.DiaperDraft
import com.littlegrow.app.data.DiaperEntity
import com.littlegrow.app.data.FeedingDraft
import com.littlegrow.app.data.FeedingEntity
import com.littlegrow.app.data.GrowthDraft
import com.littlegrow.app.data.GrowthEntity
import com.littlegrow.app.data.HomeSummary
import com.littlegrow.app.data.LittleGrowRepository
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
import java.time.LocalDate
import java.time.Period

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository = LittleGrowRepository(
        database = AppDatabase.getInstance(application),
        preferencesRepository = PreferencesRepository(application),
    )

    private val selectedRecordTab = MutableStateFlow(RecordTab.FEEDING)
    private val _exportMessage = MutableStateFlow<String?>(null)
    private val _isExporting = MutableStateFlow(false)

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
            if (repository.profile.first() == null) {
                repository.seedIfNeeded()
            }
            refreshVaccineReminders()
        }
    }

    fun selectRecordTab(tab: RecordTab) {
        selectedRecordTab.value = tab
    }

    fun saveProfile(profile: BabyProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            refreshVaccineReminders()
        }
    }

    fun addFeeding(draft: FeedingDraft) {
        viewModelScope.launch {
            repository.addFeeding(draft)
        }
    }

    fun updateFeeding(
        id: Long,
        draft: FeedingDraft,
    ) {
        viewModelScope.launch {
            repository.saveFeeding(recordId = id, draft = draft)
        }
    }

    fun deleteFeeding(id: Long) {
        viewModelScope.launch {
            repository.deleteFeeding(id)
        }
    }

    fun addSleep(draft: SleepDraft) {
        viewModelScope.launch {
            repository.addSleep(draft)
        }
    }

    fun updateSleep(
        id: Long,
        draft: SleepDraft,
    ) {
        viewModelScope.launch {
            repository.saveSleep(recordId = id, draft = draft)
        }
    }

    fun deleteSleep(id: Long) {
        viewModelScope.launch {
            repository.deleteSleep(id)
        }
    }

    fun addDiaper(draft: DiaperDraft) {
        viewModelScope.launch {
            repository.addDiaper(draft)
        }
    }

    fun updateDiaper(
        id: Long,
        draft: DiaperDraft,
    ) {
        viewModelScope.launch {
            repository.saveDiaper(recordId = id, draft = draft)
        }
    }

    fun deleteDiaper(id: Long) {
        viewModelScope.launch {
            repository.deleteDiaper(id)
        }
    }

    fun addGrowth(draft: GrowthDraft) {
        viewModelScope.launch {
            repository.addGrowth(draft)
        }
    }

    fun updateGrowth(
        id: Long,
        draft: GrowthDraft,
    ) {
        viewModelScope.launch {
            repository.saveGrowth(recordId = id, draft = draft)
        }
    }

    fun deleteGrowth(id: Long) {
        viewModelScope.launch {
            repository.deleteGrowth(id)
        }
    }

    fun addMilestone(draft: MilestoneDraft) {
        viewModelScope.launch {
            repository.addMilestone(draft)
        }
    }

    fun updateMilestone(
        id: Long,
        draft: MilestoneDraft,
    ) {
        viewModelScope.launch {
            repository.saveMilestone(recordId = id, draft = draft)
        }
    }

    fun deleteMilestone(id: Long) {
        viewModelScope.launch {
            repository.deleteMilestone(id)
        }
    }

    fun setVaccineStatus(
        scheduleKey: String,
        isDone: Boolean,
    ) {
        viewModelScope.launch {
            repository.setVaccineStatus(scheduleKey, isDone)
            refreshVaccineReminders()
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

    private suspend fun refreshVaccineReminders() {
        VaccineReminderScheduler.rescheduleAll(
            context = getApplication(),
            vaccines = repository.vaccines.first(),
            enabled = repository.vaccineRemindersEnabled.first(),
        )
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
