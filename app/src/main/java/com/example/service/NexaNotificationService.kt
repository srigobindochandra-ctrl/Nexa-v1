package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import android.util.Log
import com.example.data.NexaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Notification Listener for NEXA AI to capture incoming messages from WhatsApp, Messenger, etc.
 * Allows NEXA to read and reply to messages autonomously.
 */
class NexaNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val pkg = sbn?.packageName ?: return
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (pkg.contains("whatsapp") || pkg.contains("facebook.orca") || pkg.contains("messenger")) {
            Log.d("NexaNotification", "Captured message from $pkg: $title - $text")
            _lastNotification.value = CapturedMessage(pkg, title, text)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceConnected = true
        Log.d("NexaNotification", "NEXA Notification Listener Connected!")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceConnected = false
    }

    data class CapturedMessage(val pkg: String, val sender: String, val content: String)

    companion object {
        var isServiceConnected = false
        private val _lastNotification = MutableStateFlow<CapturedMessage?>(null)
        val lastNotification: StateFlow<CapturedMessage?> = _lastNotification.asStateFlow()
        
        fun clearLast() {
            _lastNotification.value = null
        }
    }
}
