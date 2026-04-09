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
    private val vaccineReminderKey = "vaccine_reminders_enabled"
    private val themeState = MutableStateFlow(
        sharedPreferences.getString(themeKey, ThemeMode.SYSTEM.name)
            ?.let(ThemeMode::valueOf)
            ?: ThemeMode.SYSTEM,
    )
    private val vaccineReminderState = MutableStateFlow(
        sharedPreferences.getBoolean(vaccineReminderKey, true),
    )

    val themeMode: Flow<ThemeMode> = themeState.asStateFlow()
    val vaccineRemindersEnabled: Flow<Boolean> = vaccineReminderState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString(themeKey, mode.name).apply()
        themeState.value = mode
    }

    fun setVaccineRemindersEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(vaccineReminderKey, enabled).apply()
        vaccineReminderState.value = enabled
    }
}
