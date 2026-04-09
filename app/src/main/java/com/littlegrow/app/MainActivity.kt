package com.littlegrow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.littlegrow.app.notifications.VaccineReminderScheduler

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.handleLaunchIntent(intent)
        VaccineReminderScheduler.ensureChannel(this)
        enableEdgeToEdge()
        setContent {
            LittleGrowApp(viewModel = viewModel)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleLaunchIntent(intent)
    }
}
