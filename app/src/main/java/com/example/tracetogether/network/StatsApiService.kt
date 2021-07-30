package com.example.tracetogether.network

interface StatsApiService {
    suspend fun getProvinceDaily(): List<ProvinceDaily>
    suspend fun getZoneStats(): List<ZoneStats>
    suspend fun getMunicipalityStats(): List<MunicipalityStats>
    suspend fun getHomeStats(): List<HomeStats>
}

object StatsApi {
    val service: StatsApiService by lazy { MfpStatsApiService() }
}
