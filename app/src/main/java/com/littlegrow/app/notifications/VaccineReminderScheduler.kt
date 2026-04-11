package com.littlegrow.app.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.littlegrow.app.MainActivity
import com.littlegrow.app.R
import com.littlegrow.app.data.AppDatabase
import com.littlegrow.app.data.PreferencesRepository
import com.littlegrow.app.data.VaccineEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneId

private const val CHANNEL_ID = "vaccine_reminders"
private const val EXTRA_NAME = "extra_name"
private const val EXTRA_DATE = "extra_date"
private const val EXTRA_KEY = "extra_key"
private const val RESTORE_TAG = "ReminderRestore"

object VaccineReminderScheduler {
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "疫苗提醒",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "在推荐接种日前 3 天提醒家长安排时间。"
        }
        manager.createNotificationChannel(channel)
    }

    fun rescheduleAll(
        context: Context,
        vaccines: List<VaccineEntity>,
        enabled: Boolean,
    ) {
        ensureChannel(context)
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        vaccines.forEach { vaccine ->
            alarmManager.cancel(reminderIntent(context, vaccine))
        }
        if (!enabled) return

        val now = System.currentTimeMillis()
        vaccines
            .filter { !it.isDone }
            .forEach { vaccine ->
                val triggerAt = vaccine.scheduledDate
                    .minusDays(3)
                    .atTime(9, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                if (triggerAt <= now) return@forEach
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    reminderIntent(context, vaccine),
                )
            }
    }

    suspend fun rescheduleFromStorage(context: Context) {
        val appContext = context.applicationContext
        rescheduleAll(
            context = appContext,
            vaccines = AppDatabase.getInstance(appContext).vaccineDao().getAll(),
            enabled = PreferencesRepository(appContext).vaccineRemindersEnabled.first(),
        )
    }

    private fun reminderIntent(
        context: Context,
        vaccine: VaccineEntity,
    ): PendingIntent {
        val intent = Intent(context, VaccineReminderReceiver::class.java).apply {
            putExtra(EXTRA_KEY, vaccine.scheduleKey)
            putExtra(EXTRA_NAME, vaccine.vaccineName)
            putExtra(EXTRA_DATE, vaccine.scheduledDate.toString())
        }
        return PendingIntent.getBroadcast(
            context,
            vaccine.scheduleKey.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

class VaccineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        VaccineReminderScheduler.ensureChannel(context)
        val vaccineName = intent.getStringExtra(EXTRA_NAME) ?: return
        val scheduledDate = intent.getStringExtra(EXTRA_DATE) ?: return
        val key = intent.getStringExtra(EXTRA_KEY) ?: vaccineName
        val contentIntent = PendingIntent.getActivity(
            context,
            key.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.vaccine_notification_title))
            .setContentText(
                context.getString(
                    R.string.vaccine_notification_message,
                    vaccineName,
                    scheduledDate,
                ),
            )
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(key.hashCode(), notification)
    }
}

class VaccineReminderRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                runCatching {
                    VaccineReminderScheduler.rescheduleFromStorage(context)
                }.onFailure { throwable ->
                    Log.e(RESTORE_TAG, "Failed to restore vaccine reminders.", throwable)
                }
                runCatching {
                    QuickActionNotificationController.restoreFromStorage(context)
                }.onFailure { throwable ->
                    Log.e(RESTORE_TAG, "Failed to restore quick action notifications.", throwable)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
