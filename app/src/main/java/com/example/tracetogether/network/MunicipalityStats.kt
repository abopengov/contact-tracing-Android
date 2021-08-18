package com.example.tracetogether.network

import com.squareup.moshi.Json

data class MunicipalityStats(
    @Json(name = "municipality")
    val municipality: String?,
    @Json(name = "active_cases")
    val activeCases: Double?
)
