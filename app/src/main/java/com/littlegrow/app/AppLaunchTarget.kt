package com.littlegrow.app

import android.content.Context
import android.content.Intent
import com.littlegrow.app.data.RecordTab

enum class AppDestination(val route: String) {
    HOME("home"),
    RECORDS("records"),
    BATCH_RECORDS("batch_records"),
    GROWTH("growth"),
    TIMELINE("timeline"),
    MEDICAL_SUMMARY("medical_summary"),
    SETTINGS("settings"),
}

enum class RecordQuickAction {
    ADD,
    TIMER,
}

data class AppLaunchTarget(
    val destination: AppDestination,
    val recordTab: RecordTab? = null,
    val quickAction: RecordQuickAction? = null,
)

private const val EXTRA_DESTINATION = "launch_destination"
private const val EXTRA_RECORD_TAB = "launch_record_tab"
private const val EXTRA_RECORD_QUICK_ACTION = "launch_record_quick_action"

fun buildAppLaunchIntent(
    context: Context,
    target: AppLaunchTarget,
): Intent {
    return Intent(context, MainActivity::class.java).apply {
        putExtra(EXTRA_DESTINATION, target.destination.name)
        putExtra(EXTRA_RECORD_TAB, target.recordTab?.name)
        putExtra(EXTRA_RECORD_QUICK_ACTION, target.quickAction?.name)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }
}

fun Intent.toAppLaunchTarget(): AppLaunchTarget? {
    val destinationName = getStringExtra(EXTRA_DESTINATION) ?: return null
    val destination = runCatching { AppDestination.valueOf(destinationName) }.getOrNull() ?: return null
    val recordTab = getStringExtra(EXTRA_RECORD_TAB)?.let {
        runCatching { RecordTab.valueOf(it) }.getOrNull()
    }
    val quickAction = getStringExtra(EXTRA_RECORD_QUICK_ACTION)?.let {
        runCatching { RecordQuickAction.valueOf(it) }.getOrNull()
    }
    return AppLaunchTarget(
        destination = destination,
        recordTab = recordTab,
        quickAction = quickAction,
    )
}
