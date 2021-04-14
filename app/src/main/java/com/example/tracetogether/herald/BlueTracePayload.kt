package com.example.tracetogether.herald

import com.vmware.herald.sensor.datatype.*
import com.vmware.herald.sensor.payload.extended.ConcreteExtendedDataV1

class BlueTracePayload(
    val tempId: String,
    val modelC: String,
    val txPower: UInt16,
    val rssi: Int8
) {
    val data: Data = Data().apply {
        append(heraldEnvelopeHeader.data)

        val innerData = Data().apply {
            append(tempId, Data.StringLengthEncodingOption.UINT16)

            val extendedData = ConcreteExtendedDataV1().apply {
                addSection(UInt8(0x40), rssi)
                addSection(UInt8(0x41), txPower)
                addSection(UInt8(0x42), modelC)
            }

            append(extendedData.payload())
        }

        append(UInt16(innerData.value.size))
        append(innerData)
    }

    companion object {
        private val heraldEnvelopeHeader: HeraldEnvelopeHeader = HeraldEnvelopeHeader(
            protocolAndVersion = UInt8(0x91),
            countryCode = UInt16(124),
            stateCode = UInt16(48)
        )

        fun parse(data: Data): BlueTracePayload? {
            val payloadHeaderData = data.subdata(0, 5)

            return if (heraldEnvelopeHeader.data == payloadHeaderData) {
                val tempIdLength = data.uint16(7)
                val tempId = data.string(7, Data.StringLengthEncodingOption.UINT16).value

                var modelC = ""
                var rssi = Int8(0)
                var txPower = UInt16(0)

                val extendedData = ConcreteExtendedDataV1(PayloadData(data.subdata(9 + tempIdLength.value).value))
                extendedData.sections.forEach { section ->
                    when(section.code.value) {
                        0x40 -> rssi = section.data.int8(0)
                        0x41 -> txPower = section.data.uint16(0)
                        0x42 -> modelC = String(section.data.value)
                    }
                }

                return BlueTracePayload(
                    tempId = tempId,
                    modelC = modelC,
                    rssi = rssi,
                    txPower = txPower
                )
            } else null
        }
    }
}