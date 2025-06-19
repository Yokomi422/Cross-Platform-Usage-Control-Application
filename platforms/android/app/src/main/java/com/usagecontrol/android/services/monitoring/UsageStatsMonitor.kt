package com.usagecontrol.android.services.monitoring

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.usagecontrol.android.data.entities.UsageSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsMonitor @Inject constructor(
    private val context: Context
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    fun hasUsageStatsPermission(): Boolean {
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000,
            System.currentTimeMillis()
        )
        return stats.isNotEmpty()
    }

    fun getCurrentlyRunningApp(): String? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 60000 // Last minute
        
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )
        
        return stats.maxByOrNull { it.lastTimeUsed }?.packageName
    }

    fun getDailyUsageStats(): Flow<List<AppUsageInfo>> = flow {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000 // Last 24 hours
        
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        
        val usageInfoList = stats.mapNotNull { usageStats ->
            try {
                val appInfo = packageManager.getApplicationInfo(usageStats.packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                
                AppUsageInfo(
                    packageName = usageStats.packageName,
                    appName = appName,
                    totalTimeInForeground = usageStats.totalTimeInForeground,
                    lastTimeUsed = usageStats.lastTimeUsed,
                    firstTimeStamp = usageStats.firstTimeStamp
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }.filter { it.totalTimeInForeground > 0 }
        
        emit(usageInfoList)
    }

    fun getUsageStatsForPeriod(startTime: Long, endTime: Long): List<UsageStats> {
        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )
    }

    fun convertToUsageSession(packageName: String, startTime: Long, endTime: Long, level: Int): UsageSession {
        return UsageSession(
            packageName = packageName,
            startTime = startTime,
            endTime = endTime,
            duration = endTime - startTime,
            level = level,
            wasBlocked = false
        )
    }
}

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long,
    val lastTimeUsed: Long,
    val firstTimeStamp: Long
)
