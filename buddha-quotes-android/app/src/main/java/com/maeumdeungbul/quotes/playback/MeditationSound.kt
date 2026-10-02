package com.maeumdeungbul.quotes.playback

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.maeumdeungbul.quotes.R

/**
 * 명상 배경음 목록.
 * 저작권 문제가 없는 음원(직접 녹음 또는 라이선스가 명확한 것)만 res/raw 에 넣고 [rawRes] 를 연결한다.
 * [rawRes] 가 null 인 항목은 음원이 준비되지 않은 것으로 보고 화면에 표시하지 않는다.
 *
 * 현재 포함된 음원: bell_interval.ogg — 앱에서 직접 합성한 싱잉볼 종소리(30초마다 한 번 울림).
 */
enum class MeditationSound(
    val id: String,
    @StringRes val labelRes: Int,
    @RawRes val rawRes: Int?,
) {
    BELL("bell", R.string.sound_bell, R.raw.bell_interval),
    RAIN("rain", R.string.sound_rain, null),
    VALLEY("valley", R.string.sound_valley, null),
    FOREST("forest", R.string.sound_forest, null),
    BIRDS("birds", R.string.sound_birds, null),
    ;

    companion object {
        val available: List<MeditationSound>
            get() = entries.filter { it.rawRes != null }

        fun fromId(id: String?): MeditationSound? = available.firstOrNull { it.id == id }
    }
}
