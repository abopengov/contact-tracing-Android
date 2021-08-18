package com.example.tracetogether.network

import com.squareup.moshi.Json

data class PrivacyResponse(
    @Json(name = "privacyStatement")
    val privacyStatement: String
)
