package com.maeumdeungbul.quotes.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val fullDate = DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN)
    private val shortDate = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
    private val yearMonth = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
    private val time = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)

    fun fullDate(date: LocalDate): String = date.format(fullDate)
    fun shortDate(date: LocalDate): String = date.format(shortDate)
    fun yearMonth(date: LocalDate): String = date.format(yearMonth)

    /** 예: 오전 7:00 */
    fun time(value: LocalTime): String = value.format(time)

    /** 남은 시간 표시. 예: 08:43, 1:05:00 */
    fun clock(millis: Long): String {
        val totalSeconds = (millis + 999) / 1000
        val hours = totalSeconds / 3600
        val minutes = totalSeconds % 3600 / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
        }
    }
}
