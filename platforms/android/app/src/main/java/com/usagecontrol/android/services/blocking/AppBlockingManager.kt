package com.usagecontrol.android.services.blocking

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.usagecontrol.android.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBlockingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var overlayView: View? = null
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun blockApp(packageName: String, appName: String) {
        if (!hasOverlayPermission()) {
            requestOverlayPermission()
            return
        }

        // Remove existing overlay if any
        removeOverlay()

        // Create blocking overlay
        createBlockingOverlay(packageName, appName)

        // Auto-remove after a delay and redirect to home
        coroutineScope.launch {
            delay(3000) // Show for 3 seconds
            removeOverlay()
            redirectToHome()
        }
    }

    private fun createBlockingOverlay(packageName: String, appName: String) {
        val layoutInflater = LayoutInflater.from(context)
        overlayView = layoutInflater.inflate(R.layout.app_blocked_overlay, null)

        // Configure overlay view
        overlayView?.let { view ->
            val appNameText = view.findViewById<TextView>(R.id.blocked_app_name)
            val messageText = view.findViewById<TextView>(R.id.blocking_message)
            val okButton = view.findViewById<Button>(R.id.ok_button)

            appNameText.text = appName
            messageText.text = "この時間帯はこのアプリの使用が制限されています"

            okButton.setOnClickListener {
                removeOverlay()
                redirectToHome()
            }

            // Window layout parameters
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.CENTER

            try {
                windowManager.addView(view, params)
            } catch (e: Exception) {
                // Handle overlay permission issues
                e.printStackTrace()
            }
        }
    }

    fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                // View might already be removed
            }
            overlayView = null
        }
    }

    private fun redirectToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)
    }

    fun isAppCurrentlyBlocked(): Boolean {
        return overlayView != null
    }
}

// Compose version of blocking overlay
@Composable
fun AppBlockedOverlay(
    appName: String,
    onDismiss: () -> Unit
) {
    var isVisible by mutableStateOf(true)
    
    if (isVisible) {
        // This would be implemented with Compose UI
        // For now, using the XML-based overlay
    }
}
