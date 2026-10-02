package com.maeumdeungbul.quotes.domain.usecase

import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

object DailySchedule {

    /** [now] 이후 처음 돌아오는 [time]. 같은 시각이면 다음 날로 넘긴다. */
    fun nextTrigger(now: ZonedDateTime, time: LocalTime): ZonedDateTime {
        val today = now.toLocalDate().atTime(time).atZone(now.zone)
        return if (today.isAfter(now)) today else now.toLocalDate().plusDays(1).atTime(time).atZone(now.zone)
    }

    fun delayUntilNext(now: ZonedDateTime, time: LocalTime): Duration =
        Duration.between(now, nextTrigger(now, time))
}
