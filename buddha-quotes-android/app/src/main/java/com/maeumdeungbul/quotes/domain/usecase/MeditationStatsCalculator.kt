package com.maeumdeungbul.quotes.domain.usecase

import com.maeumdeungbul.quotes.domain.model.MeditationSession
import com.maeumdeungbul.quotes.domain.model.MeditationStats
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object MeditationStatsCalculator {

    fun calculate(
        sessions: List<MeditationSession>,
        today: LocalDate,
        zone: ZoneId,
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    ): MeditationStats {
        val secondsByDate: Map<LocalDate, Int> = sessions
            .groupBy { it.startedAt.atZone(zone).toLocalDate() }
            .mapValues { (_, list) -> list.sumOf { it.actualSeconds } }

        fun minutesBetween(start: LocalDate, endInclusive: LocalDate): Int =
            secondsByDate.filterKeys { !it.isBefore(start) && !it.isAfter(endInclusive) }
                .values.sum() / 60

        val weekStart = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
        val practiced = secondsByDate.keys

        // 오늘 아직 수행하지 않았더라도 어제까지 이어졌다면 연속 기록을 유지한다.
        var day = if (today in practiced) today else today.minusDays(1)
        var streak = 0
        while (day in practiced) {
            streak++
            day = day.minusDays(1)
        }

        return MeditationStats(
            todayMinutes = (secondsByDate[today] ?: 0) / 60,
            weekMinutes = minutesBetween(weekStart, today),
            monthMinutes = minutesBetween(today.withDayOfMonth(1), today),
            streakDays = streak,
            totalSessions = sessions.size,
            practicedDays = practiced,
        )
    }
}
