package com.usagecontrol.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usagecontrol.android.data.dao.*
import com.usagecontrol.android.services.monitoring.UsageStatsMonitor
import com.usagecontrol.android.services.blocking.AppBlockingManager
import com.usagecontrol.android.ui.screens.RecentAppUsage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
    private val appRestrictionDao: AppRestrictionDao,
    private val usageSessionDao: UsageSessionDao,
    private val progressiveLevelDao: ProgressiveLevelDao,
    private val usageStatsMonitor: UsageStatsMonitor,
    private val appBlockingManager: AppBlockingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Combine multiple data sources
            combine(
                userSettingsDao.getSettings(),
                appRestrictionDao.getAllRestrictions(),
                usageStatsMonitor.getDailyUsageStats()
            ) { settings, restrictions, usageStats ->
                val currentLevel = settings?.currentLevel ?: 1
                val currentLevelInfo = progressiveLevelDao.getLevel(currentLevel)
                
                val todayStart = getTodayStartTime()
                val todaySessions = usageSessionDao.getSessionsSince(todayStart)
                
                HomeUiState(
                    currentLevel = currentLevel,
                    currentLevelName = currentLevelInfo?.name ?: "ビギナー",
                    levelProgress = calculateLevelProgress(currentLevel, todaySessions),
                    hasUsageStatsPermission = usageStatsMonitor.hasUsageStatsPermission(),
                    hasAccessibilityPermission = false, // Would check accessibility service status
                    hasOverlayPermission = appBlockingManager.hasOverlayPermission(),
                    todayTotalUsage = todaySessions.sumOf { it.duration },
                    todayBlockedAttempts = todaySessions.count { it.wasBlocked },
                    activeRestrictions = restrictions.count { it.isBlocked },
                    recentlyUsedApps = usageStats.take(5).map { appInfo ->
                        val restriction = restrictions.find { it.packageName == appInfo.packageName }
                        RecentAppUsage(
                            appName = appInfo.appName,
                            packageName = appInfo.packageName,
                            usageTime = appInfo.totalTimeInForeground,
                            isRestricted = restriction?.isBlocked == true
                        )
                    }
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun calculateLevelProgress(currentLevel: Int, sessions: List<com.usagecontrol.android.data.entities.UsageSession>): Float {
        // Calculate progress based on level completion criteria
        // This is a simplified version - actual implementation would be more complex
        val completedDays = sessions.groupBy { getDateFromTimestamp(it.startTime) }.size
        val requiredDays = currentLevel * 7 // Example: level 1 = 7 days, level 2 = 14 days
        return (completedDays.toFloat() / requiredDays).coerceAtMost(1f)
    }

    private fun getTodayStartTime(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getDateFromTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}

data class HomeUiState(
    val currentLevel: Int = 1,
    val currentLevelName: String = "ビギナー",
    val levelProgress: Float = 0f,
    val hasUsageStatsPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val todayTotalUsage: Long = 0,
    val todayBlockedAttempts: Int = 0,
    val activeRestrictions: Int = 0,
    val recentlyUsedApps: List<RecentAppUsage> = emptyList()
)
