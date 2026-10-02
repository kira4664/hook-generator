package com.maeumdeungbul.quotes.domain.model

import java.time.Instant
import java.time.LocalDate

enum class MeditationType { TIMER, BREATHING }

data class MeditationSession(
    val id: Long = 0,
    val startedAt: Instant,
    val plannedSeconds: Int,
    val actualSeconds: Int,
    val type: MeditationType,
    val soundId: String?,
    val completed: Boolean,
)

data class MeditationStats(
    val todayMinutes: Int = 0,
    val weekMinutes: Int = 0,
    val monthMinutes: Int = 0,
    val streakDays: Int = 0,
    val totalSessions: Int = 0,
    val practicedDays: Set<LocalDate> = emptySet(),
)

/** 명상 타이머 기본 선택지(분). */
val MeditationDurationPresets: List<Int> = listOf(1, 3, 5, 10, 15, 20, 30)

const val MEDITATION_MAX_CUSTOM_MINUTES = 120

/** 이보다 짧은 명상은 기록하지 않는다. */
const val MEDITATION_MIN_RECORD_SECONDS = 60

enum class BreathPhase { INHALE, HOLD, EXHALE, HOLD_EMPTY }

data class BreathStep(val phase: BreathPhase, val seconds: Int)

enum class BreathingPattern(
    val id: String,
    val inhaleSeconds: Int,
    val holdSeconds: Int,
    val exhaleSeconds: Int,
    val holdEmptySeconds: Int = 0,
) {
    /** 들이마시기 4초 · 멈추기 2초 · 내쉬기 6초 */
    CALM("calm", 4, 2, 6),
    /** 4 · 4 · 4 · 4 */
    BOX("box", 4, 4, 4, 4),
    /** 4 · 7 · 8 */
    RELAX("relax", 4, 7, 8);

    val steps: List<BreathStep>
        get() = listOf(
            BreathStep(BreathPhase.INHALE, inhaleSeconds),
            BreathStep(BreathPhase.HOLD, holdSeconds),
            BreathStep(BreathPhase.EXHALE, exhaleSeconds),
            BreathStep(BreathPhase.HOLD_EMPTY, holdEmptySeconds),
        ).filter { it.seconds > 0 }

    val cycleSeconds: Int
        get() = inhaleSeconds + holdSeconds + exhaleSeconds + holdEmptySeconds

    companion object {
        fun fromId(id: String?): BreathingPattern = entries.firstOrNull { it.id == id } ?: CALM
    }
}
