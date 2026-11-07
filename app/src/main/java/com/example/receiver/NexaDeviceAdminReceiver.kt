package com.example.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Device Admin Receiver for NEXA AI to support locking device screen upon voice command.
 */
class NexaDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d("NexaDeviceAdmin", "NEXA Device Admin Enabled for screen lock.")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d("NexaDeviceAdmin", "NEXA Device Admin Disabled.")
    }
}
