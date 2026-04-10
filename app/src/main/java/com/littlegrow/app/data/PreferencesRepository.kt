package com.littlegrow.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(
    context: Context,
) {
    private val sharedPreferences = context.getSharedPreferences("little_grow_preferences", Context.MODE_PRIVATE)
    private val themeKey = "theme_mode"
    private val appThemeKey = "app_theme"
    private val vaccineReminderKey = "vaccine_reminders_enabled"
    private val quickActionNotificationKey = "quick_action_notifications_enabled"
    private val anomalyReminderKey = "anomaly_reminders_enabled"
    private val diaperReminderKey = "diaper_reminders_enabled"
    private val largeTextModeKey = "large_text_mode_enabled"
    private val darkModeScheduleEnabledKey = "dark_mode_schedule_enabled"
    private val darkModeStartHourKey = "dark_mode_start_hour"
    private val darkModeEndHourKey = "dark_mode_end_hour"
    private val homeModulesKey = "home_modules"
    private val caregiversKey = "caregivers"
    private val currentCaregiverKey = "current_caregiver"
    private val autoBackupFrequencyKey = "auto_backup_frequency"
    private val shownCelebrationsKey = "shown_celebrations"
    private val seenGuidesKey = "seen_guides"
    private val onboardingCompletedKey = "onboarding_completed"
    private val themeState = MutableStateFlow(
        sharedPreferences.getString(themeKey, ThemeMode.SYSTEM.name)
            ?.let(ThemeMode::valueOf)
            ?: ThemeMode.SYSTEM,
    )
    private val appThemeState = MutableStateFlow(
        sharedPreferences.getString(appThemeKey, AppTheme.EARTHY.name)
            ?.let(AppTheme::valueOf)
            ?: AppTheme.EARTHY,
    )
    private val vaccineReminderState = MutableStateFlow(
        sharedPreferences.getBoolean(vaccineReminderKey, true),
    )
    private val quickActionNotificationState = MutableStateFlow(
        sharedPreferences.getBoolean(quickActionNotificationKey, false),
    )
    private val anomalyReminderState = MutableStateFlow(
        sharedPreferences.getBoolean(anomalyReminderKey, false),
    )
    private val diaperReminderState = MutableStateFlow(
        sharedPreferences.getBoolean(diaperReminderKey, false),
    )
    private val largeTextModeState = MutableStateFlow(
        sharedPreferences.getBoolean(largeTextModeKey, false),
    )
    private val darkModeScheduleEnabledState = MutableStateFlow(
        sharedPreferences.getBoolean(darkModeScheduleEnabledKey, false),
    )
    private val darkModeStartHourState = MutableStateFlow(
        sharedPreferences.getInt(darkModeStartHourKey, 20),
    )
    private val darkModeEndHourState = MutableStateFlow(
        sharedPreferences.getInt(darkModeEndHourKey, 7),
    )
    private val homeModulesState = MutableStateFlow(
        sharedPreferences.getStringSet(homeModulesKey, defaultHomeModules().map { it.name }.toSet())
            ?.toSet()
            ?.mapNotNull { runCatching { HomeModule.valueOf(it) }.getOrNull() }
            ?.toSet()
            ?: defaultHomeModules(),
    )
    private val caregiversState = MutableStateFlow(
        sharedPreferences.getStringSet(caregiversKey, setOf("妈妈", "爸爸"))
            ?.toList()
            ?.sorted()
            ?: listOf("妈妈", "爸爸"),
    )
    private val currentCaregiverState = MutableStateFlow(
        sharedPreferences.getString(currentCaregiverKey, "妈妈") ?: "妈妈",
    )
    private val autoBackupFrequencyState = MutableStateFlow(
        sharedPreferences.getString(autoBackupFrequencyKey, BackupFrequency.OFF.name)
            ?.let(BackupFrequency::valueOf)
            ?: BackupFrequency.OFF,
    )
    private val shownCelebrationsState = MutableStateFlow(
        sharedPreferences.getStringSet(shownCelebrationsKey, emptySet())?.toSet() ?: emptySet(),
    )
    private val seenGuidesState = MutableStateFlow(
        sharedPreferences.getStringSet(seenGuidesKey, emptySet())?.toSet() ?: emptySet(),
    )
    private val onboardingCompletedState = MutableStateFlow(
        sharedPreferences.getBoolean(onboardingCompletedKey, false),
    )

    val themeMode: Flow<ThemeMode> = themeState.asStateFlow()
    val appTheme: Flow<AppTheme> = appThemeState.asStateFlow()
    val vaccineRemindersEnabled: Flow<Boolean> = vaccineReminderState.asStateFlow()
    val quickActionNotificationsEnabled: Flow<Boolean> = quickActionNotificationState.asStateFlow()
    val anomalyRemindersEnabled: Flow<Boolean> = anomalyReminderState.asStateFlow()
    val diaperRemindersEnabled: Flow<Boolean> = diaperReminderState.asStateFlow()
    val largeTextModeEnabled: Flow<Boolean> = largeTextModeState.asStateFlow()
    val darkModeScheduleEnabled: Flow<Boolean> = darkModeScheduleEnabledState.asStateFlow()
    val darkModeStartHour: Flow<Int> = darkModeStartHourState.asStateFlow()
    val darkModeEndHour: Flow<Int> = darkModeEndHourState.asStateFlow()
    val homeModules: Flow<Set<HomeModule>> = homeModulesState.asStateFlow()
    val caregivers: Flow<List<String>> = caregiversState.asStateFlow()
    val currentCaregiver: Flow<String> = currentCaregiverState.asStateFlow()
    val autoBackupFrequency: Flow<BackupFrequency> = autoBackupFrequencyState.asStateFlow()
    val shownCelebrations: Flow<Set<String>> = shownCelebrationsState.asStateFlow()
    val seenGuides: Flow<Set<String>> = seenGuidesState.asStateFlow()
    val onboardingCompleted: Flow<Boolean> = onboardingCompletedState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString(themeKey, mode.name).apply()
        themeState.value = mode
    }

    fun setAppTheme(theme: AppTheme) {
        sharedPreferences.edit().putString(appThemeKey, theme.name).apply()
        appThemeState.value = theme
    }

    fun setVaccineRemindersEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(vaccineReminderKey, enabled).apply()
        vaccineReminderState.value = enabled
    }

    fun setQuickActionNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(quickActionNotificationKey, enabled).apply()
        quickActionNotificationState.value = enabled
    }

    fun setAnomalyRemindersEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(anomalyReminderKey, enabled).apply()
        anomalyReminderState.value = enabled
    }

    fun setDiaperRemindersEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(diaperReminderKey, enabled).apply()
        diaperReminderState.value = enabled
    }

    fun setLargeTextModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(largeTextModeKey, enabled).apply()
        largeTextModeState.value = enabled
    }

    fun setDarkModeSchedule(
        enabled: Boolean,
        startHour: Int,
        endHour: Int,
    ) {
        sharedPreferences.edit()
            .putBoolean(darkModeScheduleEnabledKey, enabled)
            .putInt(darkModeStartHourKey, startHour)
            .putInt(darkModeEndHourKey, endHour)
            .apply()
        darkModeScheduleEnabledState.value = enabled
        darkModeStartHourState.value = startHour
        darkModeEndHourState.value = endHour
    }

    fun setHomeModules(modules: Set<HomeModule>) {
        sharedPreferences.edit().putStringSet(homeModulesKey, modules.map { it.name }.toSet()).apply()
        homeModulesState.value = modules
    }

    fun setCaregivers(caregivers: List<String>) {
        val normalized = caregivers.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val safe = if (normalized.isEmpty()) listOf("妈妈") else normalized
        sharedPreferences.edit().putStringSet(caregiversKey, safe.toSet()).apply()
        caregiversState.value = safe
        if (currentCaregiverState.value !in safe) {
            setCurrentCaregiver(safe.first())
        }
    }

    fun setCurrentCaregiver(name: String) {
        sharedPreferences.edit().putString(currentCaregiverKey, name).apply()
        currentCaregiverState.value = name
    }

    fun setAutoBackupFrequency(frequency: BackupFrequency) {
        sharedPreferences.edit().putString(autoBackupFrequencyKey, frequency.name).apply()
        autoBackupFrequencyState.value = frequency
    }

    fun markCelebrationShown(key: String) {
        val next = shownCelebrationsState.value + key
        sharedPreferences.edit().putStringSet(shownCelebrationsKey, next).apply()
        shownCelebrationsState.value = next
    }

    fun markGuideSeen(month: Int) {
        val next = seenGuidesState.value + month.toString()
        sharedPreferences.edit().putStringSet(seenGuidesKey, next).apply()
        seenGuidesState.value = next
    }

    fun setOnboardingCompleted() {
        sharedPreferences.edit().putBoolean(onboardingCompletedKey, true).apply()
        onboardingCompletedState.value = true
    }

    private fun defaultHomeModules(): Set<HomeModule> {
        return setOf(
            HomeModule.TODAY_SUMMARY,
            HomeModule.RECENT_FEEDINGS,
            HomeModule.RECENT_SLEEP,
            HomeModule.LATEST_GROWTH,
            HomeModule.MILESTONE,
            HomeModule.VACCINE,
            HomeModule.TREND,
            HomeModule.ROUTINE,
            HomeModule.GUIDE,
        )
    }
}
