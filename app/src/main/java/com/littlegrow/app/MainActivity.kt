package com.littlegrow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.littlegrow.app.notifications.VaccineReminderScheduler
import com.littlegrow.app.ui.theme.LittleGrowTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.handleLaunchIntent(intent)
        VaccineReminderScheduler.ensureChannel(this)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()

            LittleGrowTheme(
                themeMode = themeMode,
                appTheme = appTheme,
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
