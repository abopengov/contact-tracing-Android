package com.example.tracetogether.herald

import com.example.tracetogether.TracerApp
import com.example.tracetogether.streetpass.persistence.StreetPassRecordStorage
import com.vmware.herald.sensor.SensorArray
import com.vmware.herald.sensor.ble.BLESensorConfiguration
import com.vmware.herald.sensor.data.SensorLoggerLevel
import com.vmware.herald.sensor.datatype.TimeInterval

object HeraldManager {
    private var sensorArray: SensorArray? = null

    fun start() {
        if (sensorArray == null) {
            val streetPassRecordStorage = StreetPassRecordStorage(TracerApp.AppContext)
            val sendDistance = !FairEfficacyInstrumentation.enabled
            val blueTracePayloadDataSupplier = BlueTracePayloadDataSupplier(ConcreteTempIdProvider(), sendDistance)

            BLESensorConfiguration.logLevel = SensorLoggerLevel.off

            sensorArray = SensorArray(TracerApp.AppContext, blueTracePayloadDataSupplier).also {
                it.add(BlueTraceDelegate(StreetPassDataPersistence(streetPassRecordStorage)))

                if (FairEfficacyInstrumentation.enabled) {
                    FairEfficacyInstrumentation.setupLogging(it)
                    BLESensorConfiguration.logLevel = SensorLoggerLevel.debug
                }

                BLESensorConfiguration.payloadDataUpdateTimeInterval = TimeInterval(60*5L)

                it.start()
            }
        }
    }

    fun stop() {
        sensorArray?.stop()
        sensorArray = null
    }
}