package com.example.tracetogether.stats

import android.app.Application
import androidx.lifecycle.*
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.network.MunicipalityStats
import com.example.tracetogether.network.ProvinceDaily
import com.example.tracetogether.network.ZoneStats
import kotlinx.coroutines.launch
import java.util.*

enum class ChartPeriod {
    LAST_30_DAYS, THREE_MONTHS, SIX_MONTHS
}

enum class Trend {
    TRENDING_UP, TRENDING_DOWN, TRENDING_NONE;

    companion object {
        fun getTrend(previous: Long?, current: Long?): Trend {
            return when {
                previous == null || current == null -> {
                    TRENDING_NONE
                }
                current > previous -> {
                    TRENDING_UP
                }
                current < previous -> {
                    TRENDING_DOWN
                }
                else -> {
                    TRENDING_NONE
                }
            }
        }

    }
}

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "StatsViewModel"
    }

    private val statsRepository = StatsRepository(application)

    private val mutableProvinceDaily = MutableLiveData<List<ProvinceDaily>>()

    val newCasesToday: LiveData<Long?> =
            Transformations.map(mutableProvinceDaily) { provinceDaily ->
                provinceDaily.last().casesReported
            }

    val newVariantCases: LiveData<Long?> =
            Transformations.map(mutableProvinceDaily) { provinceDaily ->
                provinceDaily.last().variantReported
            }

    val vaccinesGivenToday: LiveData<Long?> =
            Transformations.map(mutableProvinceDaily) { provinceDaily ->
                provinceDaily.last().vaccineDosesGivenToday
            }

    val vaccinesAdministered: LiveData<Long?> =
            Transformations.map(mutableProvinceDaily) { provinceDaily ->
                provinceDaily.last().cumulativeVaccineDoses
            }

    val fullyVaccinated: LiveData<Long?> =
            Transformations.map(mutableProvinceDaily) { provinceDaily ->
                provinceDaily.last().peopleFullyVaccinated
            }

    val latestDate: LiveData<Date?> =
            Transformations.map(mutableProvinceDaily) { provinceDaily -> provinceDaily.last().date }

    private val mutableNetworkError = MutableLiveData<Boolean>()

    val networkError = mutableNetworkError

    private val mutableChartPeriod = MutableLiveData<ChartPeriod>(ChartPeriod.LAST_30_DAYS)

    val dailyActiveCasesReported: LiveData<List<Long?>> =
            Transformations.switchMap(mutableChartPeriod) { chartPeriod ->
                val limit = when (chartPeriod) {
                    ChartPeriod.LAST_30_DAYS -> 30
                    ChartPeriod.THREE_MONTHS -> 90
                    ChartPeriod.SIX_MONTHS -> 180
                }

                Transformations.map(mutableProvinceDaily) { provinceDaily ->
                    provinceDaily
                            .takeLast(limit)
                            .map { it.activeCasesReported?.toLong() }
                }
            }

    val dailyCumulativeVaccineDoses: LiveData<List<Long?>> =
            Transformations.switchMap(mutableChartPeriod) { chartPeriod ->
                val limit = when (chartPeriod) {
                    ChartPeriod.LAST_30_DAYS -> 30
                    ChartPeriod.THREE_MONTHS -> 90
                    ChartPeriod.SIX_MONTHS -> 180
                }

                Transformations.map(mutableProvinceDaily) { provinceDaily ->
                    provinceDaily
                            .takeLast(limit)
                            .map { it.cumulativeVaccineDoses }
                }
            }

    val newCasesTrend: LiveData<Trend> =
            Transformations.map(mutableProvinceDaily) { provinceDaily ->
                var trend = Trend.TRENDING_NONE

                if (provinceDaily.size >= 2) {
                    val lastTwo = provinceDaily.takeLast(2).map { it.casesReported }
                    trend = Trend.getTrend(lastTwo.first(), lastTwo.last())
                }
                trend
            }

    val newVariantCasesTrend: LiveData<Trend> =
            Transformations.map(mutableProvinceDaily) { provinceDaily ->
                var trend = Trend.TRENDING_NONE

                if (provinceDaily.size >= 2) {
                    val lastTwo = provinceDaily.takeLast(2).map { it.variantReported }
                    trend = Trend.getTrend(lastTwo.first(), lastTwo.last())
                }

                trend
            }

    val vaccinesGivenTrend: LiveData<Trend> =
            Transformations.map(mutableProvinceDaily) { provinceDaily ->
                var trend = Trend.TRENDING_NONE

                if (provinceDaily.size >= 2) {
                    val lastTwo = provinceDaily.takeLast(2).map { it.vaccineDosesGivenToday }
                    trend = Trend.getTrend(lastTwo.first(), lastTwo.last())
                }
                trend
            }

    private val mutableCasesByZone = MutableLiveData<List<ZoneStats>>()
    val casesByZone: LiveData<List<ZoneStats>> = mutableCasesByZone

    private val mutableMunicipalityStats = MutableLiveData<List<MunicipalityStats>>()
    val municipalityStats: LiveData<List<MunicipalityStats>> = mutableMunicipalityStats

    fun getProvinceDaily() {
        viewModelScope.launch {
            try {
                mutableProvinceDaily.value = statsRepository.getProvinceDaily().sortedBy { it.date }
            } catch (ex: Exception) {
                CentralLog.e(TAG, "Failed to get province daily: " + ex.message)
                mutableNetworkError.value = true
                return@launch
            }
        }
    }

    fun getZoneLatest() {
        viewModelScope.launch {
            try {
                mutableCasesByZone.value = statsRepository.getZoneStats().sortedBy { it.zone }
            } catch (ex: Exception) {
                CentralLog.e(TAG, "Failed to get zone latest: " + ex.message)
                mutableNetworkError.value = true
                return@launch
            }
        }
    }

    fun getMunicipalityStats() {
        viewModelScope.launch {
            try {
                mutableMunicipalityStats.value = statsRepository.getMunicipalityStats()
            } catch (ex: Exception) {
                CentralLog.e(TAG, "Failed to get municipality stats: " + ex.message)
                mutableNetworkError.value = true
                return@launch
            }
        }
    }

    fun setChartPeriod(chartPeriod: ChartPeriod) {
        mutableChartPeriod.value = chartPeriod
    }
}
