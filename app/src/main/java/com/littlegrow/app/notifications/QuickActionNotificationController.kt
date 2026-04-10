package com.littlegrow.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.littlegrow.app.AppDestination
import com.littlegrow.app.AppLaunchTarget
import com.littlegrow.app.R
import com.littlegrow.app.RecordQuickAction
import com.littlegrow.app.buildAppLaunchIntent
import com.littlegrow.app.data.PreferencesRepository
import com.littlegrow.app.data.RecordTab
import kotlinx.coroutines.flow.first

private const val QUICK_ACTION_CHANNEL_ID = "quick_action"
private const val QUICK_ACTION_NOTIFICATION_ID = 1201

object QuickActionNotificationController {
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            QUICK_ACTION_CHANNEL_ID,
            "快捷记录",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "在通知栏保留常用记录入口。"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun sync(
        context: Context,
        enabled: Boolean,
    ) {
        ensureChannel(context)
        val notificationManager = NotificationManagerCompat.from(context)
        if (!enabled || !canPostNotifications(context)) {
            notificationManager.cancel(QUICK_ACTION_NOTIFICATION_ID)
            return
        }
        notificationManager.notify(
            QUICK_ACTION_NOTIFICATION_ID,
            buildNotification(context),
        )
    }

    suspend fun restoreFromStorage(context: Context) {
        val appContext = context.applicationContext
        sync(
            context = appContext,
            enabled = PreferencesRepository(appContext).quickActionNotificationsEnabled.first(),
        )
    }

    private fun buildNotification(context: Context) = NotificationCompat.Builder(context, QUICK_ACTION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(context.getString(R.string.quick_action_notification_title))
        .setContentText(context.getString(R.string.quick_action_notification_message))
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                20_000,
                buildAppLaunchIntent(
                    context,
                    AppLaunchTarget(
                        destination = AppDestination.RECORDS,
                        recordTab = RecordTab.FEEDING,
                    ),
                ),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setShowWhen(false)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .addAction(
            0,
            "喂奶",
            quickActionIntent(context, requestCode = 20_001, tab = RecordTab.FEEDING),
        )
        .addAction(
            0,
            "换尿布",
            quickActionIntent(context, requestCode = 20_002, tab = RecordTab.DIAPER),
        )
        .addAction(
            0,
            "入睡",
            quickActionIntent(context, requestCode = 20_003, tab = RecordTab.SLEEP),
        )
        .build()

    private fun quickActionIntent(
        context: Context,
        requestCode: Int,
        tab: RecordTab,
    ): PendingIntent {
        val intent = buildAppLaunchIntent(
            context,
            AppLaunchTarget(
                destination = AppDestination.RECORDS,
                recordTab = tab,
                quickAction = RecordQuickAction.ADD,
            ),
        )
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }
}
