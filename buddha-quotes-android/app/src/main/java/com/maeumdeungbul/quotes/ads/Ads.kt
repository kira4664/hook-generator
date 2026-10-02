package com.maeumdeungbul.quotes.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * 광고 확장 지점. 현재는 광고 SDK 가 없으므로 [NoOpAdsProvider] 만 사용한다.
 *
 * AdMob 을 도입할 때:
 * 1. play-services-ads 의존성과 UMP(동의 관리)를 추가하고 AdMobAdsProvider 를 구현한다.
 * 2. 광고 단위 ID 는 소스에 쓰지 않는다. debug 는 Google 공식 문서의 테스트 ID,
 *    release 는 Gradle property(CI secret)로 BuildConfig 에 주입한다.
 * 3. 명상 진행 화면에는 [AdSlot] 을 두지 않는다(정책·사용자 경험).
 */
enum class AdPlacement { QUOTE_LIST_INLINE, QUOTE_DETAIL_BOTTOM, MEDITATION_COMPLETE }

interface AdsProvider {
    @Composable
    fun Banner(placement: AdPlacement, modifier: Modifier)
}

object NoOpAdsProvider : AdsProvider {
    @Composable
    override fun Banner(placement: AdPlacement, modifier: Modifier) = Unit
}

val LocalAdsProvider = staticCompositionLocalOf<AdsProvider> { NoOpAdsProvider }

@Composable
fun AdSlot(placement: AdPlacement, modifier: Modifier = Modifier) {
    LocalAdsProvider.current.Banner(placement, modifier)
}
