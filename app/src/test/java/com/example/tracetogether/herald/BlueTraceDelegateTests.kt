package com.example.tracetogether.herald

import com.example.tracetogether.streetpass.persistence.StreetPassRecord
import com.vmware.herald.sensor.datatype.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BlueTraceDelegateTests {
    companion object {
        private  const val ONE_SECOND = 1000L
    }
    lateinit var subject: BlueTraceDelegate
    lateinit var stubDataPersistence: TestDataPersistence
    val targetIdentifier = TargetIdentifier()

    @Before
    fun setUp() {
        stubDataPersistence = TestDataPersistence()
        subject = BlueTraceDelegate(stubDataPersistence, ONE_SECOND)
    }

    @Test
    fun testSensorDataReceived_saveRecordWithTimeThreshold() {
        val blueTracePayload = BlueTracePayload(
            tempId = "tempId",
            modelC = "modelC",
            txPower = UInt16(20),
            rssi = Int8(-51)
        )

        subject.sensor(
            sensor = SensorType.BLE,
            didMeasure = Proximity(ProximityMeasurementUnit.RSSI, -51.0),
            fromTarget = targetIdentifier,
            withPayload = PayloadData().apply { append(blueTracePayload.data) }
        )

        subject.sensor(
            sensor = SensorType.BLE,
            didMeasure = Proximity(ProximityMeasurementUnit.RSSI, -51.0),
            fromTarget = targetIdentifier,
            withPayload = PayloadData().apply { append(blueTracePayload.data) }
        )

        subject.sensor(
            sensor = SensorType.BLE,
            didMeasure = Proximity(ProximityMeasurementUnit.RSSI, -51.0),
            fromTarget = targetIdentifier,
            withPayload = PayloadData().apply { append(blueTracePayload.data) }
        )

        subject.sensor(
            sensor = SensorType.BLE,
            didMeasure = Proximity(ProximityMeasurementUnit.RSSI, -51.0),
            fromTarget = TargetIdentifier(),
            withPayload = PayloadData().apply { append(blueTracePayload.data) }
        )

        assertEquals(2, stubDataPersistence.saveEncounterCount)

        Thread.sleep(ONE_SECOND)

        subject.sensor(
            sensor = SensorType.BLE,
            didMeasure = Proximity(ProximityMeasurementUnit.RSSI, -51.0),
            fromTarget = targetIdentifier,
            withPayload = PayloadData().apply { append(blueTracePayload.data) }
        )

        assertEquals(3, stubDataPersistence.saveEncounterCount)
    }

    @Test
    fun testSensorDataReceived_clearOutdatedRecentContactEvents() {
        val blueTracePayload = BlueTracePayload(
            tempId = "tempId",
            modelC = "modelC",
            txPower = UInt16(20),
            rssi = Int8(-51)
        )

        subject.sensor(
            sensor = SensorType.BLE,
            didMeasure = Proximity(ProximityMeasurementUnit.RSSI, -51.0),
            fromTarget = targetIdentifier,
            withPayload = PayloadData().apply { append(blueTracePayload.data) }
        )

        Thread.sleep(ONE_SECOND)

        subject.sensor(
            sensor = SensorType.BLE,
            didMeasure = Proximity(ProximityMeasurementUnit.RSSI, -51.0),
            fromTarget = targetIdentifier,
            withPayload = PayloadData().apply { append(blueTracePayload.data) }
        )

        assertEquals(1, subject.recentContactEvents.size)

    }
}

class TestDataPersistence : BlueTraceDataPersistence {
    var saveEncounterCount = 0

    override fun saveEncounter(record: StreetPassRecord) {
        saveEncounterCount += 1
    }
}
