package com.example.tracetogether.herald

import com.example.tracetogether.TracerApp
import com.example.tracetogether.streetpass.persistence.StreetPassRecordStorage
import io.heraldprox.herald.sensor.SensorArray
import io.heraldprox.herald.sensor.ble.BLESensorConfiguration
import io.heraldprox.herald.sensor.data.SensorLoggerLevel
import io.heraldprox.herald.sensor.datatype.TimeInterval

interface HeraldManager {
    fun start()
    fun stop()
}

object HeraldManagerImpl: HeraldManager {
    private val PAYLOAD_UPDATE_FREQUENCY = TimeInterval(60 * 5L)

    private var sensorArray: SensorArray? = null

    override fun start() {
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

                BLESensorConfiguration.payloadDataUpdateTimeInterval = PAYLOAD_UPDATE_FREQUENCY

                it.start()
            }
        }
    }

    override fun stop() {
        sensorArray?.stop()
        sensorArray = null
    }
}