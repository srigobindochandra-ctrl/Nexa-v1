package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.NexaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Background Foreground Service that keeps NEXA AI alive and monitors screen-on / wake events.
 * Automatically activates NEXA AI the moment the device screen turns on.
 */
class NexaBackgroundService : Service() {

    private var screenReceiver: BroadcastReceiver? = null
    private var repository: NexaRepository? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        _isRunning.value = true
        repository = NexaRepository(applicationContext)

        createNotificationChannel()
        val initialNotification = buildNotification("⚡ NEXA AI Active | Ready on Screen Wake & Voice")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            Log.e("NexaBackgroundService", "Error starting foreground service: ${e.message}")
            try {
                startForeground(NOTIFICATION_ID, initialNotification)
            } catch (fallbackEx: Exception) {
                Log.e("NexaBackgroundService", "Fallback startForeground failed: ${fallbackEx.message}")
            }
        }

        registerScreenReceiver()
        Log.d("NexaBackgroundService", "NEXA Background Service created & screen receiver registered")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        _isRunning.value = true
        val action = intent?.action
        if (action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun registerScreenReceiver() {
        if (screenReceiver != null) return

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        Log.d("NexaBackgroundService", "⚡ Screen ON detected! Auto-enabling NEXA in background.")
                        handleScreenWake(context, "SCREEN_ON")
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        Log.d("NexaBackgroundService", "⚡ User Present (Unlocked) detected!")
                        handleScreenWake(context, "USER_PRESENT")
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        Log.d("NexaBackgroundService", "Screen OFF detected.")
                        updateNotification("Standby Mode | Screen Off | Waiting for Wake")
                        _lastScreenEvent.value = "SCREEN_OFF"
                    }
                }
            }
        }

        registerReceiver(screenReceiver, filter)
    }

    private fun handleScreenWake(context: Context?, eventType: String) {
        _lastScreenEvent.value = eventType
        val repo = repository ?: NexaRepository(applicationContext)
        val isAutoWakeEnabled = repo.isAutoWakeOnScreenOn()

        if (!isAutoWakeEnabled) {
            Log.d("NexaBackgroundService", "Auto-wake on screen on is disabled in settings.")
            return
        }

        _screenWakeCount.value += 1
        updateNotification("⚡ NEXA Awake! Screen Active | Voice Ready")

        // If auto-launch UI on screen wake is enabled in settings, bring NEXA to front
        if (repo.isAutoLaunchOnScreenWake()) {
            val launchIntent = Intent(context ?: this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_SCREEN_WAKE_TRIGGER", true)
            }
            context?.startActivity(launchIntent) ?: startActivity(launchIntent)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NEXA AI Assistant")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NEXA AI Background System",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors screen-on events and keeps NEXA ready in background"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        instance = null
        _isRunning.value = false
        try {
            screenReceiver?.let { unregisterReceiver(it) }
            screenReceiver = null
        } catch (e: Exception) {
            Log.e("NexaBackgroundService", "Error unregistering receiver: ${e.message}")
        }
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "nexa_background_channel"
        const val NOTIFICATION_ID = 9001
        const val ACTION_STOP_SERVICE = "com.example.service.STOP_NEXA_BACKGROUND"

        var instance: NexaBackgroundService? = null
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _lastScreenEvent = MutableStateFlow("INITIALIZED")
        val lastScreenEvent: StateFlow<String> = _lastScreenEvent.asStateFlow()

        private val _screenWakeCount = MutableStateFlow(0)
        val screenWakeCount: StateFlow<Int> = _screenWakeCount.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, NexaBackgroundService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("NexaBackgroundService", "Error starting background service: ${e.message}")
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NexaBackgroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
