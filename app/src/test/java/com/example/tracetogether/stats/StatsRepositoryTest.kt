package com.example.tracetogether.stats

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.tracetogether.network.MunicipalityStats
import com.example.tracetogether.network.ProvinceDaily
import com.example.tracetogether.network.StatsApiService
import com.example.tracetogether.network.ZoneStats
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class StatsRepositoryTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var statsApiService: StatsApiService

    @Mock
    private lateinit var provinceDailyCache: StatsCache<ProvinceDaily>

    @Mock
    private lateinit var zoneStatsCache: StatsCache<ZoneStats>

    @Mock
    private lateinit var municipalityStatsCache: StatsCache<MunicipalityStats>

    private lateinit var subject: StatsRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        subject = StatsRepository(
            context,
            statsApiService,
            provinceDailyCache,
            zoneStatsCache,
            municipalityStatsCache
        )
    }

    @Test
    fun getProvinceDaily_withExpiredCache() = runBlocking {
        val expectedData = listOf(
            ProvinceDaily(
                date = Date(),
                casesReported = 1,
                variantReported = 1,
                cumulativeCases = 1,
                vaccineDosesGivenToday = 1,
                cumulativeVaccineDoses = 1,
                activeCasesReported = 1,
                peopleFullyVaccinated = 1
            )
        )

        `when`(provinceDailyCache.isExpired()).doReturn(true)
        `when`(statsApiService.getProvinceDaily()).doReturn(expectedData)

        val provinceDaily = subject.getProvinceDaily()

        verify(statsApiService).getProvinceDaily()
        verify(provinceDailyCache).set(expectedData)

        assertEquals(expectedData, provinceDaily)
    }

    @Test
    fun getProvinceDaily_withCache() = runBlocking {
        val expectedData = listOf(
            ProvinceDaily(
                date = Date(),
                casesReported = 1,
                variantReported = 1,
                cumulativeCases = 1,
                vaccineDosesGivenToday = 1,
                cumulativeVaccineDoses = 1,
                activeCasesReported = 1,
                peopleFullyVaccinated = 1
            )
        )

        `when`(provinceDailyCache.isExpired()).doReturn(false)
        `when`(provinceDailyCache.get()).doReturn(expectedData)

        val provinceDaily = subject.getProvinceDaily()

        verify(statsApiService, never()).getProvinceDaily()
        verify(provinceDailyCache, never()).set(expectedData)

        assertEquals(expectedData, provinceDaily)
    }

    @Test
    fun getProvinceDaily_withNetworkError_andCache() = runBlocking {
        val expectedData = listOf(
            ProvinceDaily(
                date = Date(),
                casesReported = 1,
                variantReported = 1,
                cumulativeCases = 1,
                vaccineDosesGivenToday = 1,
                cumulativeVaccineDoses = 1,
                activeCasesReported = 1,
                peopleFullyVaccinated = 1
            )
        )

        `when`(provinceDailyCache.isExpired()).doReturn(true)
        `when`(provinceDailyCache.get()).doReturn(expectedData)
        `when`(statsApiService.getProvinceDaily()).doThrow(RuntimeException())

        val provinceDaily = subject.getProvinceDaily()

        assertEquals(expectedData, provinceDaily)
    }

    @Test
    fun getProvinceDaily_withNetworkError_andNoCache() = runBlocking {
        `when`(provinceDailyCache.isExpired()).doReturn(true)
        `when`(provinceDailyCache.get()).doReturn(null)
        `when`(statsApiService.getProvinceDaily()).doThrow(RuntimeException())

        val exception = assertThrows(RuntimeException::class.java) {
            runBlocking {
                subject.getProvinceDaily()
            }
        }

        assertNotNull(exception)
    }

    @Test
    fun getZoneStats_withCache() = runBlocking {
        val expectedData = listOf(
            ZoneStats(
                zone = "zone",
                zoneCode = "zoneCode",
                totalCases = 1,
                activeCases = 1,
                activeCasesPer100Thousand = 1.0
            )
        )

        `when`(zoneStatsCache.isExpired()).doReturn(false)
        `when`(zoneStatsCache.get()).doReturn(expectedData)

        val zoneStats = subject.getZoneStats()

        verify(statsApiService, never()).getZoneStats()
        verify(zoneStatsCache, never()).set(expectedData)

        assertEquals(expectedData, zoneStats)
    }

    @Test
    fun getZoneStats_withNetworkError_andCache() = runBlocking {
        val expectedData = listOf(
            ZoneStats(
                zone = "zone",
                zoneCode = "zoneCode",
                totalCases = 1,
                activeCases = 1,
                activeCasesPer100Thousand = 1.0
            )
        )

        `when`(zoneStatsCache.isExpired()).doReturn(true)
        `when`(zoneStatsCache.get()).doReturn(expectedData)
        `when`(statsApiService.getZoneStats()).doThrow(RuntimeException())

        val zoneStats = subject.getZoneStats()

        assertEquals(expectedData, zoneStats)
    }

    @Test
    fun getZoneStats_withNetworkError_andNoCache() = runBlocking {
        `when`(zoneStatsCache.isExpired()).doReturn(true)
        `when`(zoneStatsCache.get()).doReturn(null)
        `when`(statsApiService.getZoneStats()).doThrow(RuntimeException())

        val exception = assertThrows(RuntimeException::class.java) {
            runBlocking {
                subject.getZoneStats()
            }
        }

        assertNotNull(exception)
    }

    @Test
    fun getMunicipalityStats_withCache() = runBlocking {
        val expectedData = listOf(
            MunicipalityStats("municipality", 1.0)
        )

        `when`(municipalityStatsCache.isExpired()).doReturn(false)
        `when`(municipalityStatsCache.get()).doReturn(expectedData)

        val municipalityStats = subject.getMunicipalityStats()

        verify(statsApiService, never()).getMunicipalityStats()
        verify(municipalityStatsCache, never()).set(expectedData)

        assertEquals(expectedData, municipalityStats)
    }

    @Test
    fun getMunicipalityStats_withNetworkError_andCache() = runBlocking {
        val expectedData = listOf(
            MunicipalityStats("municipality", 1.0)
        )

        `when`(municipalityStatsCache.isExpired()).doReturn(true)
        `when`(municipalityStatsCache.get()).doReturn(expectedData)
        `when`(statsApiService.getMunicipalityStats()).doThrow(RuntimeException())

        val municipalityStats = subject.getMunicipalityStats()

        assertEquals(expectedData, municipalityStats)
    }

    @Test
    fun getMunicipalityStats_withNetworkError_andNoCache() = runBlocking {
        `when`(municipalityStatsCache.isExpired()).doReturn(true)
        `when`(municipalityStatsCache.get()).doReturn(null)
        `when`(statsApiService.getMunicipalityStats()).doThrow(RuntimeException())

        val exception = assertThrows(RuntimeException::class.java) {
            runBlocking {
                subject.getMunicipalityStats()
            }
        }

        assertNotNull(exception)
    }
}