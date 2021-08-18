package com.example.tracetogether.herald

import io.heraldprox.herald.sensor.datatype.Int8
import io.heraldprox.herald.sensor.datatype.UInt16
import junit.framework.TestCase
import org.junit.Test

class BlueTracePayloadTests : TestCase() {
    @Test
    fun testCreateParse() {
        val tempId = "myid"
        val modelC = "Playbook"
        val txPower = UInt16(2)
        val rssi = Int8(-2)

        val blueTracePayload = BlueTracePayload(
            tempId = tempId,
            modelC = modelC,
            txPower = txPower,
            rssi = rssi
        )

        val parsedBlueTracePayload = BlueTracePayload.parse(blueTracePayload.data)

        assertEquals(blueTracePayload.tempId, parsedBlueTracePayload?.tempId)
        assertEquals(blueTracePayload.modelC, parsedBlueTracePayload?.modelC)
        assertEquals(blueTracePayload.txPower, parsedBlueTracePayload?.txPower)
        assertEquals(blueTracePayload.rssi, parsedBlueTracePayload?.rssi)

    }
}