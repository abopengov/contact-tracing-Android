package com.example.tracetogether

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.tracetogether.api.ErrorCode
import com.example.tracetogether.api.Request
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.scheduler.Scheduler
import com.example.tracetogether.services.BluetoothMonitoringService
import com.example.tracetogether.services.BluetoothMonitoringService.Companion.PENDING_BM_UPDATE
import com.example.tracetogether.services.BluetoothMonitoringService.Companion.PENDING_HEALTH_CHECK_CODE
import com.example.tracetogether.services.BluetoothMonitoringService.Companion.PENDING_PURGE_CODE
import com.worklight.wlclient.api.WLClient
import java.text.SimpleDateFormat
import java.util.*


object Utils {

    private const val TAG = "Utils"

    fun getServerURL(): String {
        return WLClient.getInstance().serverUrl.toString()
    }

    fun getRequiredPermissions(): Array<String> {
        return arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun getBatteryOptimizerExemptionIntent(packageName: String): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        return intent
    }

    fun canHandleIntent(batteryExemptionIntent: Intent, packageManager: PackageManager?): Boolean {
        packageManager?.let {
            return batteryExemptionIntent.resolveActivity(packageManager) != null
        }
        return false
    }

    fun getDate(milliSeconds: Long): String {
        val dateFormat = "dd/MM/yyyy HH:mm:ss.SSS"
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun getTime(milliSeconds: Long): String {
        val dateFormat = "h:mm a"
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun startBluetoothMonitoringService(context: Context) {
        if (!Preference.isOnBoarded(context)) {
            CentralLog.d(TAG, "App has not been onboarded. No services started.")
            return
        }

        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_START.index
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun scheduleBMUpdateCheck(context: Context, bmCheckInterval: Long) {

        cancelBMUpdateCheck(context)

        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_UPDATE_BM.index
        )

        Scheduler.scheduleServiceIntent(
            PENDING_BM_UPDATE,
            context,
            intent,
            bmCheckInterval
        )
    }

    fun cancelBMUpdateCheck(context: Context) {
        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_UPDATE_BM.index
        )

        Scheduler.cancelServiceIntent(PENDING_BM_UPDATE, context, intent)
    }

    fun stopBluetoothMonitoringService(context: Context) {
        val intent = Intent(context, BluetoothMonitoringService::class.java)
        intent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_STOP.index
        )
        cancelNextHealthCheck(context)
        context.stopService(intent)
    }

    fun scheduleNextHealthCheck(context: Context, timeInMillis: Long) {
        //cancels any outstanding check schedules.
        cancelNextHealthCheck(context)

        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_SELF_CHECK.index
        )
        //runs every XXX milliseconds - every minute?
        Scheduler.scheduleServiceIntent(
            PENDING_HEALTH_CHECK_CODE,
            context,
            nextIntent,
            timeInMillis
        )
    }

    fun cancelNextHealthCheck(context: Context) {
        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_SELF_CHECK.index
        )
        Scheduler.cancelServiceIntent(PENDING_HEALTH_CHECK_CODE, context, nextIntent)
    }

    fun scheduleRepeatingPurge(context: Context, intervalMillis: Long) {
        val nextIntent = Intent(context, BluetoothMonitoringService::class.java)
        nextIntent.putExtra(
            BluetoothMonitoringService.COMMAND_KEY,
            BluetoothMonitoringService.Command.ACTION_PURGE.index
        )

        Scheduler.scheduleRepeatingServiceIntent(
            PENDING_PURGE_CODE,
            context,
            nextIntent,
            intervalMillis
        )
    }

    fun hideKeyboardFrom(
        context: Context,
        view: View
    ) {
        val imm = context.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    //Checks if the application is registered in MFP
    suspend fun checkIfAppRegistered(): Boolean {
        var isRegistered = true
        val request =
            Request.runRequest("/adapters/smsOtpService/phone/isRegistered", Request.GET)
        if (request.errorCode == ErrorCode.APPLICATION_DOES_NOT_EXIST) {
            isRegistered = false
        }
        return isRegistered
    }

    fun buildWrongVersionDialog(context: Context, msg: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage(msg)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()
    }

    fun restartApp(context: Context, errorType: Int, errorMsg: String? = null) {
        val intent = Intent(
            context,
            RestartActivity::class.java
        )
        errorMsg?.let {
            intent.putExtra("error_msg", errorMsg)
        }
        intent.putExtra("error_type", errorType)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent)
    }

    fun restartAppWithNoContext(errorType: Int, errorMsg: String? = null) {
        val intent = Intent(
            TracerApp.AppContext,
            RestartActivity::class.java
        )
        errorMsg?.let {
            intent.putExtra("error_msg", errorMsg)
        }
        intent.putExtra("error_type", errorType)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TracerApp.AppContext.startActivity(intent)
    }

    fun getVersionSuffix(): String {
        val serverUrl = getServerURL()

        return when {
            serverUrl.contains("stg") -> " S"
            serverUrl.contains("dev") -> " D"
            else -> ""
        }
    }

}
