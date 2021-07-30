package com.example.tracetogether.pause

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tracetogether.logging.CentralLog

class PauseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            CentralLog.d(TAG, "Broadcast Received")
            PauseScheduler.togglePause(context)
        }
    }

    companion object {
        private const val TAG = "PauseReceiver"
    }
}