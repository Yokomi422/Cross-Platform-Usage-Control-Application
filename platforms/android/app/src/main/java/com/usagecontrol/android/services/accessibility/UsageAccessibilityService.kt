package com.usagecontrol.android.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import com.usagecontrol.android.data.dao.AppRestrictionDao
import com.usagecontrol.android.services.blocking.AppBlockingManager
import javax.inject.Inject

@AndroidEntryPoint
class UsageAccessibilityService : AccessibilityService() {
    
    @Inject
    lateinit var appRestrictionDao: AppRestrictionDao
    
    @Inject
    lateinit var appBlockingManager: AppBlockingManager
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentPackageName: String? = null
    private var sessionStartTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { handleAccessibilityEvent(it) }
    }

    private fun handleAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString()
                if (packageName != null && packageName != currentPackageName) {
                    onAppChanged(packageName)
                }
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                // Monitor specific interactions if needed
                monitorUserInteraction(event)
            }
        }
    }

    private fun onAppChanged(newPackageName: String) {
        val currentTime = System.currentTimeMillis()
        
        // End previous session
        currentPackageName?.let { previousPackage ->
            if (sessionStartTime > 0) {
                serviceScope.launch {
                    recordUsageSession(previousPackage, sessionStartTime, currentTime)
                }
            }
        }
        
        // Start new session
        currentPackageName = newPackageName
        sessionStartTime = currentTime
        
        // Check if app should be blocked
        serviceScope.launch {
            checkAndBlockApp(newPackageName)
        }
    }

    private suspend fun checkAndBlockApp(packageName: String) {
        val restriction = appRestrictionDao.getRestriction(packageName)
        restriction?.let { 
            if (it.isBlocked || isTimeExceeded(it)) {
                appBlockingManager.blockApp(packageName, it.appName)
            }
        }
    }

    private fun isTimeExceeded(restriction: com.usagecontrol.android.data.entities.AppRestriction): Boolean {
        return restriction.dailyTimeLimit > 0 && 
               restriction.usedTimeToday >= restriction.dailyTimeLimit
    }

    private suspend fun recordUsageSession(packageName: String, startTime: Long, endTime: Long) {
        // This would be implemented to record usage session
        // For now, we'll just update the daily usage time
        val duration = endTime - startTime
        if (duration > 0) {
            val restriction = appRestrictionDao.getRestriction(packageName)
            restriction?.let {
                val newUsedTime = it.usedTimeToday + duration
                appRestrictionDao.updateUsageTime(packageName, newUsedTime, endTime)
            }
        }
    }

    private fun monitorUserInteraction(event: AccessibilityEvent) {
        // Monitor specific user interactions for analytics
        // This can be extended for more detailed monitoring
        val nodeInfo = event.source
        nodeInfo?.let { node ->
            // Analyze the interaction context
            analyzeInteractionContext(node)
        }
    }

    private fun analyzeInteractionContext(nodeInfo: AccessibilityNodeInfo) {
        // Analyze what type of content user is interacting with
        // This could help in understanding usage patterns
        val text = nodeInfo.text?.toString()
        val contentDescription = nodeInfo.contentDescription?.toString()
        
        // Log or analyze the interaction context
        // This data could be used for usage insights
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Service connected and ready
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        
        // Record final session if exists
        currentPackageName?.let { packageName ->
            if (sessionStartTime > 0) {
                runBlocking {
                    recordUsageSession(packageName, sessionStartTime, System.currentTimeMillis())
                }
            }
        }
    }
}
