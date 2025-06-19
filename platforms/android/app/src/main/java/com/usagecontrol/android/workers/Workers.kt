package com.usagecontrol.android.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.usagecontrol.android.data.dao.*
import com.usagecontrol.android.services.monitoring.UsageStatsMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class UsageMonitoringWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageStatsMonitor: UsageStatsMonitor,
    private val appRestrictionDao: AppRestrictionDao,
    private val usageSessionDao: UsageSessionDao,
    private val userSettingsDao: UserSettingsDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check if usage stats permission is available
            if (!usageStatsMonitor.hasUsageStatsPermission()) {
                return Result.retry()
            }

            // Get current user settings
            val settings = userSettingsDao.getSettings().first() ?: return Result.success()
            
            // Monitor usage for the last hour
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.HOURS.toMillis(1)
            
            val usageStats = usageStatsMonitor.getUsageStatsForPeriod(startTime, endTime)
            
            // Process usage data
            usageStats.forEach { usageStat ->
                if (usageStat.totalTimeInForeground > 0) {
                    // Update app restriction usage
                    val restriction = appRestrictionDao.getRestriction(usageStat.packageName)
                    restriction?.let {
                        val newUsedTime = it.usedTimeToday + usageStat.totalTimeInForeground
                        appRestrictionDao.updateUsageTime(
                            usageStat.packageName,
                            newUsedTime,
                            endTime
                        )
                    }
                    
                    // Create usage session
                    val session = usageStatsMonitor.convertToUsageSession(
                        usageStat.packageName,
                        usageStat.firstTimeStamp,
                        usageStat.lastTimeUsed,
                        settings.currentLevel
                    )
                    usageSessionDao.insertSession(session)
                }
            }
            
            // Clean up old sessions (older than 30 days)
            val thirtyDaysAgo = endTime - TimeUnit.DAYS.toMillis(30)
            usageSessionDao.deleteOldSessions(thirtyDaysAgo)
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "usage_monitoring_work"

        fun enqueueWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<UsageMonitoringWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
    }
}

@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appRestrictionDao: AppRestrictionDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Reset daily usage for all apps
            appRestrictionDao.resetDailyUsage()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "daily_reset_work"

        fun enqueueWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            // Schedule to run at midnight every day
            val currentTime = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = currentTime
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                add(java.util.Calendar.DAY_OF_MONTH, 1) // Next midnight
            }

            val delay = calendar.timeInMillis - currentTime

            val workRequest = PeriodicWorkRequestBuilder<DailyResetWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }
}

@HiltWorker
class FirebaseSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appRestrictionDao: AppRestrictionDao,
    private val usageSessionDao: UsageSessionDao,
    private val userSettingsDao: UserSettingsDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check if sync is enabled
            val settings = userSettingsDao.getSettings().first()
            if (settings?.syncEnabled != true) {
                return Result.success()
            }

            // Sync unsynced sessions to Firebase
            val unsyncedSessions = usageSessionDao.getUnsyncedSessions()
            if (unsyncedSessions.isNotEmpty()) {
                // TODO: Implement Firebase sync
                // For now, just mark as synced
                val sessionIds = unsyncedSessions.map { it.id }
                usageSessionDao.markSessionsAsSynced(sessionIds)
            }

            // Update last sync time
            userSettingsDao.updateLastSyncTime(System.currentTimeMillis())

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "firebase_sync_work"

        fun enqueueWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<FirebaseSyncWorker>(
                4, TimeUnit.HOURS,
                1, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
        }
    }
}
