package com.littlegrow.app.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = LittleGrowRepository(
            appContext = applicationContext,
            database = database,
            preferencesRepository = PreferencesRepository(applicationContext),
        )
        val backupManager = BackupManager(applicationContext, database)
        val snapshot = repository.buildExportSnapshot()
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            applicationContext.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, "littlegrow-auto-${LocalDate.now()}.lgbackup")
                    put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                    put(MediaStore.Downloads.IS_PENDING, 0)
                },
            )
        } else {
            null
        } ?: return Result.failure()
        backupManager.exportBackup(uri, snapshot)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "little_grow_auto_backup"

        fun sync(context: Context, frequency: BackupFrequency) {
            val manager = WorkManager.getInstance(context)
            if (frequency.days == null) {
                manager.cancelUniqueWork(UNIQUE_NAME)
                return
            }
            val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(
                frequency.days.toLong(),
                TimeUnit.DAYS,
            ).build()
            manager.enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
