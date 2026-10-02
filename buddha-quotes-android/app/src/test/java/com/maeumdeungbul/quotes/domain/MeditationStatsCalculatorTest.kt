package com.maeumdeungbul.quotes.domain

import com.maeumdeungbul.quotes.domain.model.MeditationSession
import com.maeumdeungbul.quotes.domain.model.MeditationType
import com.maeumdeungbul.quotes.domain.usecase.MeditationStatsCalculator
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class MeditationStatsCalculatorTest {

    private val zone = ZoneId.of("Asia/Seoul")
    // 2026-10-02 은 금요일
    private val today = LocalDate.of(2026, 10, 2)

    private fun session(date: LocalDate, minutes: Int, hour: Int = 7) = MeditationSession(
        startedAt = date.atTime(LocalTime.of(hour, 0)).atZone(zone).toInstant(),
        plannedSeconds = minutes * 60,
        actualSeconds = minutes * 60,
        type = MeditationType.TIMER,
        soundId = null,
        completed = true,
    )

    @Test
    fun sumsTodayWeekAndMonth() {
        val sessions = listOf(
            session(today, 10),
            session(today, 5, hour = 21),
            session(today.minusDays(4), 20), // 9/28 월요일: 이번 주, 지난 달
            session(today.minusDays(1), 10), // 10/1 목요일
            session(today.minusDays(7), 30), // 지난 주
        )
        val stats = MeditationStatsCalculator.calculate(sessions, today, zone)
        assertEquals(15, stats.todayMinutes)
        assertEquals(45, stats.weekMinutes)
        assertEquals(25, stats.monthMinutes)
        assertEquals(5, stats.totalSessions)
    }

    @Test
    fun streakCountsConsecutiveDays_andSurvivesUntilTodayIsPracticed() {
        val sessions = (1L..5L).map { session(today.minusDays(it), 5) } + session(today.minusDays(8), 5)
        assertEquals(5, MeditationStatsCalculator.calculate(sessions, today, zone).streakDays)
        assertEquals(6, MeditationStatsCalculator.calculate(sessions + session(today, 1), today, zone).streakDays)
    }

    @Test
    fun streakBreaksAfterMissedDay() {
        val sessions = listOf(session(today.minusDays(2), 10), session(today.minusDays(3), 10))
        assertEquals(0, MeditationStatsCalculator.calculate(sessions, today, zone).streakDays)
    }

    @Test
    fun sessionsAreGroupedByLocalDate() {
        // 한국 시간 00:30 은 UTC 로는 전날이지만 오늘로 집계되어야 한다.
        val lateNight = session(today, 10, hour = 0).copy(
            startedAt = today.atTime(0, 30).atZone(zone).toInstant(),
        )
        assertEquals(10, MeditationStatsCalculator.calculate(listOf(lateNight), today, zone).todayMinutes)
    }
}
