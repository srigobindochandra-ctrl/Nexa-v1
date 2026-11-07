package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class NexaAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("NexaAccessibility", "NEXA Accessibility Service Connected!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Can monitor interactive state or incoming events
    }

    override fun onInterrupt() {
        Log.d("NexaAccessibility", "NEXA Accessibility Service Interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * Simulates a screen click at (x, y) coordinates using Accessibility Gestures.
     */
    fun simulateTap(x: Float, y: Float, durationMs: Long = 100): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            return dispatchGesture(gesture, null, null)
        }
        return false
    }

    /**
     * Searches for a button or text on screen (e.g. "Like", "Follow", "Subscribe", "Comment")
     * and performs a click on the clickable node or its clickable parent.
     */
    fun clickByText(query: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(query)
        if (!nodes.isNullOrEmpty()) {
            for (node in nodes) {
                var current: AccessibilityNodeInfo? = node
                while (current != null) {
                    if (current.isClickable) {
                        val clicked = current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        if (clicked) return true
                    }
                    current = current.parent
                }
            }
        }
        return false
    }

    /**
     * Executes standard Android global accessibility actions:
     * Home, Back, Recents, Notifications, Quick Settings
     */
    fun triggerGlobalAction(actionId: Int): Boolean {
        return performGlobalAction(actionId)
    }

    /**
     * Locks the device screen via Accessibility Global Action (Android 9.0+ / API 28+).
     */
    fun lockScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            false
        }
    }

    companion object {
        var instance: NexaAccessibilityService? = null

        fun isServiceRunning(): Boolean = instance != null

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}
