package com.example.tracetogether.network

import android.content.Context
import com.example.tracetogether.model.GuidanceTile
import com.example.tracetogether.more.MoreLink

interface UrlsApiService {
    suspend fun retrieveAndCacheUrls(context: Context)
    suspend fun getMoreLinks(context: Context): List<MoreLink>
    suspend fun getGuidanceTile(context: Context): GuidanceTile
}

object UrlsApi {
    val service: UrlsApiService by lazy { MfpUrlsApiService() }
}