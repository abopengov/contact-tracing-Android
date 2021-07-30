package com.example.tracetogether.herald

import io.heraldprox.herald.sensor.Device
import io.heraldprox.herald.sensor.ble.BLEDevice
import io.heraldprox.herald.sensor.datatype.*
import io.heraldprox.herald.sensor.payload.DefaultPayloadDataSupplier

class BlueTracePayloadDataSupplier(
        private val tempIdProvider: TempIdProvider,
        private val sendDistance: Boolean = true
) : DefaultPayloadDataSupplier() {

    override fun payload(payloadTimestamp: PayloadTimestamp, device: Device?): PayloadData {
        val tempId = tempIdProvider.tempId
        val payloadData = PayloadData()

        if (tempId?.isNotEmpty() == true) {
            val txPower = if (sendDistance) (device as? BLEDevice)?.txPower()?.value ?: 0 else 0
            val rssi = if (sendDistance) (device as? BLEDevice)?.rssi()?.value?.toInt() ?: 0 else 0

            val blueTracePayload = BlueTracePayload(
                    tempId = tempId,
                    modelC = DeviceInfo.model,
                    txPower = UInt16(txPower),
                    rssi = Int8(rssi)
            )

            payloadData.append(blueTracePayload.data)
        } else {
            tempIdProvider.fetchNewTempId()
        }

        return payloadData
    }

    override fun payload(data: Data): MutableList<PayloadData> {
        val payloads: MutableList<PayloadData> = ArrayList()
        var index = 0

        do {
            val payload = nextPayload(index, data)
            if (payload != null) {
                payloads.add(payload)
                index += payload.value?.size ?: 0
            }
        } while (payload != null)

        return payloads
    }

    private fun nextPayload(
            index: Int,
            data: Data
    ): PayloadData? {
        return data.uint16(index + 5)?.let { innerPayloadLength ->
            data.subdata(index, innerPayloadLength.value + 7)?.let {
                PayloadData(it.value)
            }
        }
    }
}
