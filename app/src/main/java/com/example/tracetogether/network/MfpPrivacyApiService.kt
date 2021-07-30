package com.example.tracetogether.network

import com.example.tracetogether.api.Request
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class MfpPrivacyApiService : PrivacyApiService {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, DataJsonAdapter().nullSafe())
        .build()

    override suspend fun getPrivacyPolicy(): PrivacyResponse {
        val privacyResponse = Request.runRequest(
            "/adapters/applicationDataAdapter/getPrivacyPolicy",
            Request.GET
        )

        if (privacyResponse.isSuccess()) {
            return withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    checkNotNull(privacyResponse.text?.let { text ->
                        val adapter = moshi.adapter(PrivacyResponse::class.java)
                        adapter.fromJson(text)
                    }) { "No data available from server" }
                }.getOrThrow()
            }
        } else {
            throw Exception(privacyResponse.error)
        }
    }
}