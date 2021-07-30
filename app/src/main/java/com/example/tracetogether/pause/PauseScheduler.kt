package com.example.tracetogether.pause

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.tracetogether.Preference
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.Extensions.nextDateTime
import com.example.tracetogether.util.Extensions.withinTimePeriod
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

object PauseScheduler {
    var pauseToggled: (() -> Unit)? = null
    private const val TAG = "PauseScheduler"
    private const val PENDING_START_PAUSE = 50
    private const val PENDING_STOP_PAUSE = 51

    fun togglePause(context: Context) {
        if (withinPauseSchedule(context)) {
            CentralLog.d(TAG, "Paused - Stopping Herald")
            Utils.stopBluetoothMonitoringService(context)
            pauseToggled?.let { it() }
        } else {
            CentralLog.d(TAG, "Resumed - Starting Herald")
            Utils.startBluetoothMonitoringService(context)
            pauseToggled?.let { it() }
        }
    }

    fun withinPauseSchedule(
        context: Context
    ): Boolean = Preference.getPauseScheduled(context) && LocalTime.now().withinTimePeriod(
        Preference.getPauseStartTime(context),
        Preference.getPauseEndTime(context)
    )

    fun schedule(
        context: Context,
        startTime: LocalTime? = Preference.getPauseStartTime(context),
        endTime: LocalTime? = Preference.getPauseEndTime(context)
    ) {
        if (startTime != null && endTime != null) {
            CentralLog.d(TAG, "Scheduling pause for $startTime to $endTime")
            schedulePause(context, PENDING_START_PAUSE, startTime)
            schedulePause(context, PENDING_STOP_PAUSE, endTime)
        }
    }

    fun cancel(context: Context) {
        cancelPause(context, PENDING_START_PAUSE)
        cancelPause(context, PENDING_STOP_PAUSE)
    }

    private fun cancelPause(context: Context, requestCode: Int) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = getAlarmIntent(context, requestCode)
        alarmMgr.cancel(alarmIntent)
    }

    private fun schedulePause(
        context: Context,
        requestCode: Int,
        time: LocalTime
    ) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = getAlarmIntent(context, requestCode)
        val triggerAtMillis =
            time.nextDateTime().atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()

        alarmMgr.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS),
            alarmIntent
        )
    }

    private fun getAlarmIntent(
        context: Context,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, PauseReceiver::class.java)

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }
}