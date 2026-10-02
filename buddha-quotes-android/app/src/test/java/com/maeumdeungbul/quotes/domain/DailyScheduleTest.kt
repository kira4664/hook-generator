package com.maeumdeungbul.quotes.domain

import com.maeumdeungbul.quotes.domain.usecase.DailySchedule
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyScheduleTest {

    private val zone = ZoneId.of("Asia/Seoul")
    private val seven = LocalTime.of(7, 0)

    @Test
    fun beforeTargetTime_schedulesToday() {
        val now = ZonedDateTime.of(2026, 10, 2, 6, 30, 0, 0, zone)
        assertEquals(Duration.ofMinutes(30), DailySchedule.delayUntilNext(now, seven))
    }

    @Test
    fun afterTargetTime_schedulesTomorrow() {
        val now = ZonedDateTime.of(2026, 10, 2, 7, 30, 0, 0, zone)
        assertEquals(Duration.ofHours(23).plusMinutes(30), DailySchedule.delayUntilNext(now, seven))
    }

    @Test
    fun exactlyAtTargetTime_schedulesTomorrow() {
        val now = ZonedDateTime.of(2026, 10, 2, 7, 0, 0, 0, zone)
        assertEquals(Duration.ofHours(24), DailySchedule.delayUntilNext(now, seven))
    }

    @Test
    fun handlesDaylightSavingTransition() {
        val berlin = ZoneId.of("Europe/Berlin")
        // 2026-10-25 03:00 에 서머타임 종료(하루가 25시간)
        val now = ZonedDateTime.of(2026, 10, 24, 8, 0, 0, 0, berlin)
        assertEquals(Duration.ofHours(24), DailySchedule.delayUntilNext(now, LocalTime.of(7, 0)))
    }
}
