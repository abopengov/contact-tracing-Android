package com.example.tracetogether.util

import com.example.tracetogether.util.Extensions.nextDateTime
import com.example.tracetogether.util.Extensions.splitBy
import com.example.tracetogether.util.Extensions.withinTimePeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class ExtensionsTest {

    @Test
    fun nextDateTime_same_day() {
        val now = LocalDateTime.of(2021, 1, 1, 1, 1)
        val expectedTime = LocalTime.of(2, 2)
        val result = expectedTime.nextDateTime(now)

        assertEquals(expectedTime, result.toLocalTime())
        assertEquals(now.toLocalDate(), result.toLocalDate())
    }

    @Test
    fun nextDateTime_next_day() {
        val now = LocalDateTime.of(2021, 1, 1, 1, 1)
        val expectedTime = LocalTime.of(0, 0)
        val result = expectedTime.nextDateTime(now)

        assertEquals(expectedTime, result.toLocalTime())
        assertEquals(now.toLocalDate().plusDays(1), result.toLocalDate())
    }

    @Test
    fun withinTimePeriodWithNullTimes() {
        assertFalse(LocalTime.now().withinTimePeriod(null, null))
        assertFalse(LocalTime.now().withinTimePeriod(LocalTime.now(), null))
        assertFalse(LocalTime.now().withinTimePeriod(null, LocalTime.now()))
    }

    @Test
    fun withinTimePeriodWithPeriodSameDay() {
        val startTime = LocalTime.of(1, 30)
        val endTime = LocalTime.of(3, 30)

        val beforePeriod = LocalTime.of(1, 0)
        val withinPeriod = LocalTime.of(2, 0)
        val afterPeriod = LocalTime.of(4, 0)

        assertFalse(beforePeriod.withinTimePeriod(startTime, endTime))
        assertFalse(afterPeriod.withinTimePeriod(startTime, endTime))
        assertTrue(withinPeriod.withinTimePeriod(startTime, endTime))
        assertTrue(startTime.withinTimePeriod(startTime, endTime))
        assertFalse(endTime.withinTimePeriod(startTime, endTime))
    }

    @Test
    fun withinTimePeriodWithPeriodAcrossDays() {
        val startTime = LocalTime.of(23, 0)
        val endTime = LocalTime.of(7, 0)

        val beforePeriod = LocalTime.of(22, 0)
        val withinPeriodFirstDay = LocalTime.of(23, 30)
        val withinPeriodSecondDay = LocalTime.of(1,0)
        val afterPeriod = LocalTime.of(8, 0)

        assertFalse(beforePeriod.withinTimePeriod(startTime, endTime))
        assertFalse(afterPeriod.withinTimePeriod(startTime, endTime))
        assertTrue(withinPeriodFirstDay.withinTimePeriod(startTime, endTime))
        assertTrue(withinPeriodSecondDay.withinTimePeriod(startTime, endTime))
    }

    @Test
    fun splitBy_noElementMatchingPredicate() {
        val numbers = listOf(1, 2, 3, 4, 5)

        assertEquals(numbers.splitBy { number -> number == 0}, listOf(numbers))
    }

    @Test
    fun splitBy_withElementMatchiingPredicate() {
        val numbers = listOf(1, 2, 3, 4, 0, 5, 6, 7, 0, 8, 9, 0, 10)

        val splitNumbers = numbers.splitBy { number -> number == 0 }
        assertEquals(splitNumbers.size, 4)
        assertEquals(splitNumbers[0], listOf(1, 2, 3, 4))
        assertEquals(splitNumbers[1], listOf(5, 6, 7))
        assertEquals(splitNumbers[2], listOf(8, 9))
        assertEquals(splitNumbers[3], listOf(10))
    }
}