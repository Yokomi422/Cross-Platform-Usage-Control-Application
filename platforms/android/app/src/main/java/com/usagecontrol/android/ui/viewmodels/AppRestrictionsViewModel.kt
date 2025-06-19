package com.usagecontrol.android.ui.viewmodels

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usagecontrol.android.data.dao.AppRestrictionDao
import com.usagecontrol.android.data.entities.AppRestriction
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppRestrictionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appRestrictionDao: AppRestrictionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppRestrictionsUiState())
    val uiState: StateFlow<AppRestrictionsUiState> = _uiState.asStateFlow()

    init {
        loadRestrictions()
    }

    private fun loadRestrictions() {
        viewModelScope.launch {
            appRestrictionDao.getAllRestrictions()
                .collect { restrictions ->
                    _uiState.value = AppRestrictionsUiState(
                        restrictions = restrictions,
                        isLoading = false
                    )
                }
        }
    }

    fun toggleAppBlocked(packageName: String) {
        viewModelScope.launch {
            val restriction = appRestrictionDao.getRestriction(packageName)
            restriction?.let {
                appRestrictionDao.updateRestriction(
                    it.copy(
                        isBlocked = !it.isBlocked,
                        lastUpdated = System.currentTimeMillis(),
                        isSynced = false
                    )
                )
            }
        }
    }

    fun updateTimeLimit(packageName: String, timeLimitMs: Long) {
        viewModelScope.launch {
            val restriction = appRestrictionDao.getRestriction(packageName)
            restriction?.let {
                appRestrictionDao.updateRestriction(
                    it.copy(
                        dailyTimeLimit = timeLimitMs,
                        lastUpdated = System.currentTimeMillis(),
                        isSynced = false
                    )
                )
            }
        }
    }

    fun addAllInstalledApps() {
        viewModelScope.launch {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            installedApps.forEach { appInfo ->
                // Skip system apps unless they're commonly used
                if (!isSystemApp(appInfo) || isCommonUserApp(appInfo.packageName)) {
                    val existing = appRestrictionDao.getRestriction(appInfo.packageName)
                    if (existing == null) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val restriction = AppRestriction(
                            packageName = appInfo.packageName,
                            appName = appName,
                            isBlocked = false,
                            dailyTimeLimit = 0,
                            usedTimeToday = 0,
                            level = 1
                        )
                        appRestrictionDao.insertRestriction(restriction)
                    }
                }
            }
        }
    }

    fun refreshInstalledApps() {
        addAllInstalledApps()
    }

    private fun isSystemApp(appInfo: android.content.pm.ApplicationInfo): Boolean {
        return (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
    }

    private fun isCommonUserApp(packageName: String): Boolean {
        val commonApps = setOf(
            "com.android.chrome",
            "com.google.android.youtube",
            "com.facebook.katana",
            "com.instagram.android",
            "com.twitter.android",
            "com.snapchat.android",
            "com.tiktok",
            "com.whatsapp",
            "com.spotify.music",
            "com.netflix.mediaclient",
            "com.discord",
            "com.amazon.mShop.android.shopping",
            "com.google.android.gm",
            "com.microsoft.office.outlook",
            "com.google.android.apps.maps"
        )
        return commonApps.contains(packageName)
    }
}

data class AppRestrictionsUiState(
    val restrictions: List<AppRestriction> = emptyList(),
    val isLoading: Boolean = true
)
