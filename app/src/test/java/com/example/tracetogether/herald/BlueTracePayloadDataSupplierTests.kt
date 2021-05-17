package com.example.tracetogether.herald

import com.vmware.herald.sensor.datatype.Data
import com.vmware.herald.sensor.datatype.Int8
import com.vmware.herald.sensor.datatype.PayloadTimestamp
import com.vmware.herald.sensor.datatype.UInt16
import org.junit.Assert.*
import org.junit.Test

class BlueTracePayloadDataSupplierTests {

    val subject = BlueTracePayloadDataSupplier(TestTempIdProvider())

    @Test
    fun payloadWithNoDevice() {
        val payload = subject.payload(PayloadTimestamp(), null)

        val blueTracePayload = BlueTracePayload.parse(payload)

        assertNotNull(blueTracePayload)
        assertEquals(Int8(0), blueTracePayload?.rssi)
        assertEquals(UInt16(0), blueTracePayload?.txPower)
        assertTrue(blueTracePayload?.tempId?.isNotEmpty()!!)
    }

    @Test
    fun payloadsWithVariableLength() {
        val blueTracePayload1 = BlueTracePayload(
            tempId = "tempId",
            modelC = "modelC",
            txPower = UInt16(20),
            rssi = Int8(-51)
        )

        val blueTracePayload2 = BlueTracePayload(
            tempId = "tempId",
            modelC = "modelThatIsLong",
            txPower = UInt16(20),
            rssi = Int8(-51)
        )

        val data = Data().apply {
            append(blueTracePayload1.data)
            append(blueTracePayload2.data)
        }

        val payloads = subject.payload(data)

        assertEquals(2, payloads.size)
    }
}

private class TestTempIdProvider : TempIdProvider {
    override val tempId: String
        get() = "some-temp-id"

    override fun fetchNewTempId() {
    }
}