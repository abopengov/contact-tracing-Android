package com.example.tracetogether.herald

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.app.ActivityCompat
import com.example.tracetogether.BuildConfig
import com.example.tracetogether.Preference
import com.example.tracetogether.TracerApp
import com.vmware.herald.sensor.SensorArray
import com.vmware.herald.sensor.data.*
import com.vmware.herald.sensor.datatype.Base64
import com.vmware.herald.sensor.datatype.Int32
import com.vmware.herald.sensor.datatype.Int8
import com.vmware.herald.sensor.datatype.PayloadData
import java.util.*

object FairEfficacyInstrumentation {
    private val logger: SensorLogger =
        ConcreteSensorLogger("Sensor", "Data.FairEfficacyInstrumentation")
    private const val permissionRequestCode = 1294839287

    var enabled: Boolean = BuildConfig.TEST_EFFICACY_ENABLED

    val fixedTempId: String = generatePayloadData().base64EncodedString()

    init {
        if (enabled) {
            // Add UserId
            Preference.putUUID(TracerApp.AppContext, UUID.randomUUID().toString())
            BatteryLog(TracerApp.AppContext, "battery.csv")
        }
    }

    fun requestPermissions(activity: Activity) {
        // Check and request permissions
        val requiredPermissions = mutableListOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }

        val requiredPermissionsArray =
            requiredPermissions.toTypedArray()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(requiredPermissionsArray, permissionRequestCode)
        } else {
            ActivityCompat.requestPermissions(
                activity,
                requiredPermissionsArray,
                permissionRequestCode
            )
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionRequestCode) {
            var permissionsGranted = true
            for (i in permissions.indices) {
                val permission = permissions[i]
                if (grantResults[i] != PERMISSION_GRANTED) {
                    logger.fault("Permission denied (permission=$permission)")
                    permissionsGranted = false
                } else {
                    logger.info("Permission granted (permission=$permission)")
                }
            }
            if (!permissionsGranted) {
                logger.fault(
                    "Application does not have all required permissions to start (permissions=" + Arrays.asList(
                        permissions
                    ).toString() + ")"
                )
            }
        }
    }

    fun setupLogging(sensorArray: SensorArray) {
        val payloadDataFormatter = PayloadDataFormatter {
            when {
                it.value.isEmpty() -> {
                    ""
                }
                it.value.size <= 10 -> {
                    Base64.encode(it.value)
                }
                else -> {
                    val subdata = it.subdata(10, it.value.size - 10)
                    val suffix = if (subdata?.value != null) subdata.value else ByteArray(0)
                    val base64EncodedString = Base64.encode(suffix)
                    base64EncodedString.substring(
                        0,
                        6.coerceAtMost(base64EncodedString.length)
                    )
                }
            }
        }

        sensorArray.add(ContactLog(TracerApp.AppContext, "contacts.csv", payloadDataFormatter))
        sensorArray.add(
            DetectionLog(
                TracerApp.AppContext,
                "detection.csv",
                sensorArray.payloadData(),
                payloadDataFormatter
            )
        )

        logger.info(
            "DEVICE (payloadPrefix={},description={})",
            sensorArray.payloadData().shortName(),
            Build.MODEL + " (Android " + Build.VERSION.SDK_INT + ")"
        )
    }

    private fun generatePayloadData(): PayloadData {
        // Get device specific identifier
        val text = Build.MODEL + ":" + Build.BRAND
        val identifier = Int32(text.hashCode().toLong())
        val payloadData = PayloadData()

        for (i in 1..15) {
            payloadData.append(identifier)
        }
        payloadData.append(Int8(0))

        return payloadData
    }
}