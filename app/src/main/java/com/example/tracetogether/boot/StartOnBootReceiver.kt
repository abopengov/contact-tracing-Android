package com.example.tracetogether.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tracetogether.Preference
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.pause.PauseScheduler

class StartOnBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            CentralLog.d("StartOnBootReceiver", "boot completed received")

            try {
                //can i try a scheduled service start here?
                CentralLog.d("StartOnBootReceiver", "Attempting to start service")
                Utils.startBluetoothMonitoringService(context)

                if (Preference.getPauseScheduled(context)) {
                    PauseScheduler.schedule(context)
                }
            } catch (e: Throwable) {
                CentralLog.e("StartOnBootReceiver", e.localizedMessage)
                e.printStackTrace()
            }

        }
    }
}
