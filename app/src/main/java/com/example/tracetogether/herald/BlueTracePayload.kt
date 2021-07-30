package com.example.tracetogether.herald

import io.heraldprox.herald.sensor.datatype.*
import io.heraldprox.herald.sensor.payload.extended.ConcreteExtendedDataV1

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

        append(UInt16(innerData.value?.size ?: 0))
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
                val tempIdLength = data.uint16(7) ?: return null
                val tempId =
                    data.string(7, Data.StringLengthEncodingOption.UINT16)?.value ?: return null

                var modelC = ""
                var rssi = Int8(0)
                var txPower = UInt16(0)

                val extendedData = ConcreteExtendedDataV1(
                    PayloadData(
                        data.subdata(9 + tempIdLength.value)?.value ?: ByteArray(
                            0
                        )
                    )
                )
                extendedData.sections.forEach { section ->
                    when (section.code.value) {
                        0x40 -> section.data.int8(0)?.let { rssi = it }
                        0x41 -> section.data.uint16(0)?.let { txPower = it }
                        0x42 -> section.data.value?.let { modelC = String(it) }
                    }
                }

                return BlueTracePayload(
                    tempId = tempId,
                    modelC = modelC,
                    txPower = txPower,
                    rssi = rssi
                )
            } else null
        }
    }
}