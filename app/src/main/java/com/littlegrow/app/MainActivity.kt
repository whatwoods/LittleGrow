package com.littlegrow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.littlegrow.app.notifications.QuickActionNotificationController
import com.littlegrow.app.notifications.VaccineReminderScheduler
import com.littlegrow.app.ui.theme.LittleGrowTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.handleLaunchIntent(intent)
        QuickActionNotificationController.ensureChannel(this)
        VaccineReminderScheduler.ensureChannel(this)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
            val largeTextModeEnabled by viewModel.largeTextModeEnabled.collectAsStateWithLifecycle()
            val darkModeScheduleEnabled by viewModel.darkModeScheduleEnabled.collectAsStateWithLifecycle()
            val darkModeStartHour by viewModel.darkModeStartHour.collectAsStateWithLifecycle()
            val darkModeEndHour by viewModel.darkModeEndHour.collectAsStateWithLifecycle()

            LittleGrowTheme(
                themeMode = themeMode,
                appTheme = appTheme,
                largeTextModeEnabled = largeTextModeEnabled,
                darkModeScheduleEnabled = darkModeScheduleEnabled,
                darkModeStartHour = darkModeStartHour,
                darkModeEndHour = darkModeEndHour,
            ) {
                LittleGrowApp(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    appTheme = appTheme,
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleLaunchIntent(intent)
    }
}
