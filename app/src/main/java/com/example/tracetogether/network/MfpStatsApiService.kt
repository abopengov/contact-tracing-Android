package com.example.tracetogether.network

import com.example.tracetogether.api.Request
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type
import java.util.*

class MfpStatsApiService : StatsApiService {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, DataJsonAdapter().nullSafe())
        .add(HomeStats.Type::class.java, EnumJsonAdapter.create(HomeStats.Type::class.java)
                .withUnknownFallback(HomeStats.Type.STATS))
        .build()

    override suspend fun getProvinceDaily(): List<ProvinceDaily> {
        val statsResponse = Request.runRequest(
            "/adapters/statsAdapter/statistics/province_daily",
            Request.GET,
            parseResponse = false
        )

        if (statsResponse.isSuccess()) {
            return withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    checkNotNull(statsResponse.text?.let { text ->
                        val type: Type = Types.newParameterizedType(
                            List::class.java,
                            ProvinceDaily::class.java
                        )

                        val adapter = moshi.adapter<List<ProvinceDaily>>(type)
                        adapter.fromJson(text)
                    }) { "No data available from server" }
                }.getOrThrow()
            }
        } else {
            throw Exception(statsResponse.error)
        }
    }

    override suspend fun getZoneStats(): List<ZoneStats> {
        val statsResponse = Request.runRequest(
            "/adapters/statsAdapter/statistics/zone_latest",
            Request.GET,
            parseResponse = false
        )

        if (statsResponse.isSuccess()) {
            return withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    checkNotNull(statsResponse.text?.let { text ->
                        val type: Type = Types.newParameterizedType(
                            List::class.java,
                            ZoneStats::class.java
                        )

                        val adapter = moshi.adapter<List<ZoneStats>>(type)
                        adapter.fromJson(text)
                    }) { "No data available from server" }
                }.getOrThrow()
            }
        } else {
            throw Exception(statsResponse.error)
        }
    }

    override suspend fun getMunicipalityStats(): List<MunicipalityStats> {
        val statsResponse = Request.runRequest(
            "/adapters/statsAdapter/statistics/municipality_latest",
            Request.GET,
            parseResponse = false
        )

        if (statsResponse.isSuccess()) {
            return withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    checkNotNull(statsResponse.text?.let { text ->
                        val type: Type = Types.newParameterizedType(
                            List::class.java,
                            MunicipalityStats::class.java
                        )

                        val adapter = moshi.adapter<List<MunicipalityStats>>(type)
                        adapter.fromJson(text)
                    }) { "No data available from server" }
                }.getOrThrow()
            }
        } else {
            throw Exception(statsResponse.error)
        }
    }

    override suspend fun getHomeStats(): List<HomeStats> {
        val statsResponse = Request.runRequest(
                "/adapters/statsAdapter/statistics/home_stats",
                Request.GET,
                parseResponse = false
        )

        if (statsResponse.isSuccess()) {
            return withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    checkNotNull(statsResponse.text?.let { text ->
                        val type: Type = Types.newParameterizedType(
                                List::class.java,
                                HomeStats::class.java
                        )

                        val adapter = moshi.adapter<List<HomeStats>>(type)
                        adapter.fromJson(text)
                    }) { "No data available from server" }
                }.getOrThrow()
            }
        } else {
            throw Exception(statsResponse.error)
        }
    }
}