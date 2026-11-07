package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.NexaRepository
import com.example.service.NexaBackgroundService

/**
 * Automatically launches NEXA Background Service on device boot if background service is enabled.
 */
class NexaBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = NexaRepository(context)
            if (repository.isBackgroundServiceEnabled()) {
                NexaBackgroundService.start(context)
            }
        }
    }
}
