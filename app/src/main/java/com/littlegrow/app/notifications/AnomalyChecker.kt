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
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.littlegrow.app.MainActivity
import com.littlegrow.app.R
import com.littlegrow.app.data.AgeBasedReference
import com.littlegrow.app.data.AllergyStatus
import com.littlegrow.app.data.AppDatabase
import com.littlegrow.app.data.DiaperType
import com.littlegrow.app.data.LittleGrowRepository
import com.littlegrow.app.data.PreferencesRepository
import com.littlegrow.app.data.BabyProfile
import com.littlegrow.app.data.toProfile
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

private const val CHANNEL_ID = "anomaly_reminder"

class AnomalyChecker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        ensureChannel(applicationContext)
        val repository = LittleGrowRepository(
            appContext = applicationContext,
            database = AppDatabase.getInstance(applicationContext),
            preferencesRepository = PreferencesRepository(applicationContext),
        )
        val preferences = PreferencesRepository(applicationContext)
        val profile = repository.profile.firstOrNullProfile()
        val feedings = repository.feedings.firstList()
        val sleeps = repository.sleeps.firstList()
        val diapers = repository.diapers.firstList()
        if (preferences.anomalyRemindersEnabled.firstValue()) {
            val ageMonths = profile?.birthday?.let { ChronoUnit.MONTHS.between(it, LocalDate.now()).toInt() } ?: 0
            feedings.firstOrNull()?.let { latest ->
                val gapHours = Duration.between(latest.happenedAt, LocalDateTime.now()).toHours().toDouble()
                val range = AgeBasedReference.bottleIntervalHours(ageMonths)
                if (gapHours > range.max) {
                    postNotification(
                        applicationContext,
                        4101,
                        "喂养间隔偏长",
                        "距离上次喂奶已约 ${gapHours.toInt()} 小时。",
                    )
                }
            }
            val sleepRange = AgeBasedReference.sleepHoursPerDay(ageMonths)
            val recentSleepHours = (0..2).map { offset ->
                val day = LocalDate.now().minusDays(offset.toLong())
                sleeps.filter { it.startTime.toLocalDate() == day }.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() } / 60.0
            }
            if (recentSleepHours.all { it < sleepRange.min }) {
                postNotification(
                    applicationContext,
                    4102,
                    "最近睡眠偏少",
                    "连续 3 天睡眠低于月龄参考值，建议结合状态继续观察。",
                )
            }
            val overdueObservation = feedings.firstOrNull {
                it.allergyObservation == AllergyStatus.OBSERVING &&
                    it.observationEndDate != null &&
                    !it.observationEndDate.isAfter(LocalDate.now())
            }
            overdueObservation?.let {
                postNotification(
                    applicationContext,
                    4103,
                    "辅食观察期结束",
                    "${it.foodName ?: "该食材"} 观察期已结束，如无不适可继续添加。",
                )
                AppDatabase.getInstance(applicationContext).feedingDao().upsert(
                    it.copy(allergyObservation = AllergyStatus.SAFE),
                )
            }
        }
        if (preferences.diaperRemindersEnabled.firstValue()) {
            val noPoopDays = (0..2).count { offset ->
                val date = LocalDate.now().minusDays(offset.toLong())
                diapers.none { it.type == DiaperType.POOP && it.happenedAt.toLocalDate() == date }
            }
            if (noPoopDays == 3) {
                postNotification(
                    applicationContext,
                    4104,
                    "大便记录提醒",
                    "已经连续 3 天没有大便记录，建议留意宝宝状态。",
                )
            }
        }
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "little_grow_anomaly_checker"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "智能提醒",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "用于喂养、睡眠、辅食观察和大便频次提醒。"
            }
            manager.createNotificationChannel(channel)
        }

        fun sync(context: Context, enabled: Boolean) {
            val manager = WorkManager.getInstance(context)
            if (!enabled) {
                manager.cancelUniqueWork(UNIQUE_NAME)
                return
            }
            val request = PeriodicWorkRequestBuilder<AnomalyChecker>(6, TimeUnit.HOURS).build()
            manager.enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        private fun postNotification(
            context: Context,
            id: Int,
            title: String,
            message: String,
        ) {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val contentIntent = PendingIntent.getActivity(
                context,
                id,
                android.content.Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            NotificationManagerCompat.from(context).notify(
                id,
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build(),
            )
        }
    }
}

private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.firstValue(): T = first()
private suspend fun kotlinx.coroutines.flow.Flow<com.littlegrow.app.data.BabyEntity?>.firstOrNullProfile(): BabyProfile? = first()?.toProfile()
private suspend fun <T> kotlinx.coroutines.flow.Flow<List<T>>.firstList(): List<T> = first()
