package com.example.tracetogether.network

interface PrivacyApiService {
    suspend fun getPrivacyPolicy(): PrivacyResponse
}

object PrivacyApi {
    val service: PrivacyApiService by lazy { MfpPrivacyApiService() }
}