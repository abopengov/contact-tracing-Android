package com.example.tracetogether.network

import com.squareup.moshi.Json

data class ZoneStats(
    @Json(name = "zone")
    val zone: String?,
    @Json(name = "zone_code")
    val zoneCode: String?,
    @Json(name = "total_cases")
    val totalCases: Long?,
    @Json(name = "active_cases")
    val activeCases: Long?,
    @Json(name = "active_cases_per_100_thousand")
    val activeCasesPer100Thousand: Double?
)
