package com.nevruz.galaxysnapdragon

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class SnapdragonAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 250
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val targetPackage = event?.packageName?.toString() ?: return
        if (targetPackage == packageName) return
        val selected = getSharedPreferences("snapdragon_settings", MODE_PRIVATE)
            .getStringSet("selected_apps", emptySet()) ?: emptySet()
        if (targetPackage !in selected || !android.provider.Settings.canDrawOverlays(this)) return
        val intent = Intent(this, SnapdragonMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
    }
    override fun onInterrupt() = Unit
}
