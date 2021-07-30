package com.example.tracetogether.network

import com.squareup.moshi.Json

data class HomeStats(
    @Json(name = "title")
    val title: String?,
    @Json(name = "value")
    val value: String?,
    @Json(name = "type")
    val type: Type?
) {
    enum class Type {
        STATS,
        CASES,
        VACCINATIONS,
        MAP,
        DASHBOARD
    }
}
