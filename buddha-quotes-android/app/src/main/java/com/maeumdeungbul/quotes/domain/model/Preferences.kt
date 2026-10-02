package com.maeumdeungbul.quotes.domain.model

import java.time.LocalTime

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** 말씀 본문 글자 크기. 시스템 글자 크기 설정 위에 추가로 곱해진다. */
enum class FontScale(val multiplier: Float) {
    SMALL(0.85f),
    DEFAULT(1f),
    LARGE(1.2f),
    EXTRA_LARGE(1.4f),
}

/** 말씀 카드 배경 테마. 실제 그림은 ui/components/QuoteBackgroundArt 에서 관리한다. */
enum class QuoteBackground { LOTUS, MOUNTAIN, FOREST, TEMPLE, SKY, SUNSET, WATER, MINIMAL }

data class UserPreferences(
    val onboardingDone: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: FontScale = FontScale.DEFAULT,
    val quoteBackground: QuoteBackground = QuoteBackground.LOTUS,
    /** 명상 배경음 id. null 이면 무음. */
    val ambientSoundId: String? = null,
    val bellEnabled: Boolean = true,
    val breathingPattern: BreathingPattern = BreathingPattern.CALM,
    val dailyNotificationEnabled: Boolean = false,
    val dailyNotificationTime: LocalTime = DEFAULT_NOTIFICATION_TIME,
) {
    companion object {
        val DEFAULT_NOTIFICATION_TIME: LocalTime = LocalTime.of(7, 0)
    }
}
