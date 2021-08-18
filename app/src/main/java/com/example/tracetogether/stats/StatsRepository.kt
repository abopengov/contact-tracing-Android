package com.example.tracetogether.stats

import android.content.Context
import com.example.tracetogether.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatsRepository(
        private val context: Context,
        private val statsService: StatsApiService = StatsApi.service,
        private val provinceDailyCache: StatsCache<ProvinceDaily> = StatsCache(context, ProvinceDaily::class.java, "province_daily.json"),
        private val zoneStatsCache: StatsCache<ZoneStats> = StatsCache(context, ZoneStats::class.java, "zone_stats.json"),
        private val municipalityStatsCache: StatsCache<MunicipalityStats> = StatsCache(context, MunicipalityStats::class.java, "municipality_stats.json"),
        private val homeStatsCache: StatsCache<HomeStats> = StatsCache(context, HomeStats::class.java, "home_stats.json")
) {
    suspend fun getProvinceDaily(): List<ProvinceDaily> = withContext(Dispatchers.IO) {
        return@withContext if (provinceDailyCache.isExpired()) {
            kotlin.runCatching {
                statsService.getProvinceDaily().also {
                    provinceDailyCache.set(it)
                }
            }.getOrElse { throwable ->
                provinceDailyCache.get() ?: throw throwable
            }
        } else {
            requireNotNull(provinceDailyCache.get())
        }
    }

    suspend fun getZoneStats(): List<ZoneStats> = withContext(Dispatchers.IO) {
        return@withContext if (zoneStatsCache.isExpired()) {
            kotlin.runCatching {
                statsService.getZoneStats().also {
                    zoneStatsCache.set(it)
                }
            }.getOrElse { throwable ->
                zoneStatsCache.get() ?: throw throwable
            }
        } else {
            requireNotNull(zoneStatsCache.get())
        }
    }

    suspend fun getMunicipalityStats(): List<MunicipalityStats> = withContext(Dispatchers.IO) {
        return@withContext if (municipalityStatsCache.isExpired()) {
            kotlin.runCatching {
                statsService.getMunicipalityStats().also {
                    municipalityStatsCache.set(it)
                }
            }.getOrElse { throwable ->
                municipalityStatsCache.get() ?: throw throwable
            }
        } else {
            requireNotNull(municipalityStatsCache.get())
        }
    }

    suspend fun getHomeStats(): List<HomeStats> = withContext(Dispatchers.IO) {
        return@withContext if (homeStatsCache.isExpired()) {
            kotlin.runCatching {
                statsService.getHomeStats().also {
                    homeStatsCache.set(it)
                }
            }.getOrElse { throwable ->
                homeStatsCache.get() ?: throw throwable
            }
        } else {
            requireNotNull(homeStatsCache.get())
        }
    }
}

