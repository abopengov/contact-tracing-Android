package com.example.tracetogether.herald

import io.heraldprox.herald.sensor.datatype.Data
import io.heraldprox.herald.sensor.datatype.UInt16
import io.heraldprox.herald.sensor.datatype.UInt8

class HeraldEnvelopeHeader(
    val protocolAndVersion: UInt8,
    val countryCode: UInt16,
    val stateCode: UInt16
) {
    val data: Data = Data().apply {
        append(protocolAndVersion)
        append(countryCode)
        append(stateCode)
    }
}