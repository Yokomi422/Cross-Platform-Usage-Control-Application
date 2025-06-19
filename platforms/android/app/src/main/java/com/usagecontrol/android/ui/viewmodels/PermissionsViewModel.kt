package com.usagecontrol.android.ui.viewmodels

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usagecontrol.android.services.admin.DeviceAdminReceiver
import com.usagecontrol.android.services.monitoring.UsageStatsMonitor
import com.usagecontrol.android.services.blocking.AppBlockingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val usageStatsMonitor: UsageStatsMonitor,
    private val appBlockingManager: AppBlockingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        viewModelScope.launch {
            val hasUsageStats = usageStatsMonitor.hasUsageStatsPermission()
            val hasOverlay = appBlockingManager.hasOverlayPermission()
            val hasAccessibility = false // Would implement accessibility service check
            val hasDeviceAdmin = false // Would implement device admin check

            _uiState.value = PermissionsUiState(
                hasUsageStatsPermission = hasUsageStats,
                hasAccessibilityPermission = hasAccessibility,
                hasOverlayPermission = hasOverlay,
                hasDeviceAdminPermission = hasDeviceAdmin,
                allRequiredPermissionsGranted = hasUsageStats && hasAccessibility && hasOverlay
            )
        }
    }

    fun requestDeviceAdminPermission(context: Context) {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context, DeviceAdminReceiver::class.java)
        
        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "このアプリをデバイス管理者として設定すると、より強力なアプリ制限機能を使用できます。"
                )
            }
            context.startActivity(intent)
        }
    }
}

data class PermissionsUiState(
    val hasUsageStatsPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasDeviceAdminPermission: Boolean = false,
    val allRequiredPermissionsGranted: Boolean = false
)
