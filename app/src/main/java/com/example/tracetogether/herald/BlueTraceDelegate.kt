package com.example.tracetogether.herald

import com.example.tracetogether.TracerApp
import com.example.tracetogether.streetpass.persistence.StreetPassRecord
import com.vmware.herald.sensor.DefaultSensorDelegate
import com.vmware.herald.sensor.datatype.*
import java.util.*

class BlueTraceDelegate(
    private val blueTraceDataPersistence: BlueTraceDataPersistence,
    private val encounterWindow: Long = 60000
) :
    DefaultSensorDelegate() {

    internal val recentContactEvents = mutableMapOf<String, Long>()

    override fun sensor(
        sensor: SensorType?,
        didMeasure: Proximity?,
        fromTarget: TargetIdentifier?,
        withPayload: PayloadData?
    ) {
        if (didMeasure != null && fromTarget != null && withPayload != null) {
            processPayload(didMeasure, fromTarget, withPayload)
        }
    }

    private fun processPayload(proximity: Proximity, targetIdentifier: TargetIdentifier, payload: PayloadData) {
        val uniqueIdentifier = "${targetIdentifier.value}${payload.hashCode()}"
        val payloadExpiryTime = recentContactEvents[uniqueIdentifier]

        if (payloadExpiryTime != null && payloadExpiryTime > Date().time) {
            return
        }

        clearExpiredContactEvents()

        BlueTracePayload.parse(payload)?.let { blueTracePayload ->
            val record = StreetPassRecord(
                v = 2,
                msg = blueTracePayload.tempId,
                org = TracerApp.ORG,
                modelP = DeviceInfo.model,
                modelC = blueTracePayload.modelC,
                rssi = proximity.rssiValue(),
                txPower = proximity.txPowerValue()
            )

            blueTraceDataPersistence.saveEncounter(record)
            recentContactEvents[uniqueIdentifier] = Date().time + encounterWindow
        }
    }

    private fun clearExpiredContactEvents() {
        val expiredContactEvents = recentContactEvents.entries.filter { it.value <= Date().time }
        recentContactEvents.entries.removeAll(expiredContactEvents)
    }
}

fun Proximity.txPowerValue(): Int? {
    return if (calibration?.unit === CalibrationMeasurementUnit.BLETransmitPower) {
        return calibration?.value?.toInt()
    } else null
}

fun Proximity.rssiValue(): Int {
    return if (unit === ProximityMeasurementUnit.RSSI) value.toInt() else 0
}
