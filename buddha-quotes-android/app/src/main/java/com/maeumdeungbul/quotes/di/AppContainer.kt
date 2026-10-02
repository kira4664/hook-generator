package com.maeumdeungbul.quotes.di

import android.content.Context
import android.os.SystemClock
import com.maeumdeungbul.quotes.ads.AdsProvider
import com.maeumdeungbul.quotes.ads.NoOpAdsProvider
import com.maeumdeungbul.quotes.data.datasource.AssetContentDataSource
import com.maeumdeungbul.quotes.data.datasource.ContentSeeder
import com.maeumdeungbul.quotes.data.local.MaeumDatabase
import com.maeumdeungbul.quotes.data.local.preferences.DataStorePreferencesRepository
import com.maeumdeungbul.quotes.data.local.preferences.userPreferencesDataStore
import com.maeumdeungbul.quotes.data.repository.OfflineMeditationRepository
import com.maeumdeungbul.quotes.data.repository.OfflineQuoteRepository
import com.maeumdeungbul.quotes.domain.repository.MeditationRepository
import com.maeumdeungbul.quotes.domain.repository.PreferencesRepository
import com.maeumdeungbul.quotes.domain.repository.QuoteRepository
import com.maeumdeungbul.quotes.domain.usecase.MeditationSessionManager
import com.maeumdeungbul.quotes.domain.usecase.MeditationTimer
import com.maeumdeungbul.quotes.notification.DailyQuoteScheduler
import com.maeumdeungbul.quotes.playback.Media3AmbientSoundPlayer
import com.maeumdeungbul.quotes.playback.SoundPoolBellPlayer
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json

/**
 * 수동 의존성 주입 컨테이너. Application 에서 하나만 만든다.
 * 인터페이스(domain/repository) 기준으로 연결되어 있어 필요 시 Hilt 로 옮기기 쉽다.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    /** 화면 생명주기와 무관하게 끝까지 실행되어야 하는 작업(기록 저장 등)용. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** 미디어 컨트롤러 등 메인 스레드가 필요한 앱 범위 작업용. */
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val database by lazy { MaeumDatabase.build(appContext) }

    private val json = Json { ignoreUnknownKeys = true }

    val contentDataSource by lazy { AssetContentDataSource(appContext.assets, json) }

    val preferencesRepository: PreferencesRepository by lazy {
        DataStorePreferencesRepository(appContext.userPreferencesDataStore)
    }

    private val contentSeeder by lazy { ContentSeeder(database, contentDataSource, preferencesRepository) }

    val quoteRepository: QuoteRepository by lazy { OfflineQuoteRepository(database, contentSeeder) }

    val meditationRepository: MeditationRepository by lazy {
        OfflineMeditationRepository(database.meditationSessionDao())
    }

    val dailyQuoteScheduler by lazy { DailyQuoteScheduler(appContext) }

    val meditationSessionManager by lazy {
        MeditationSessionManager(
            scope = mainScope,
            timer = MeditationTimer(mainScope, SystemClock::elapsedRealtime),
            ambientPlayer = Media3AmbientSoundPlayer(appContext),
            bellPlayer = SoundPoolBellPlayer(appContext),
            repository = meditationRepository,
            now = Instant::now,
        )
    }

    val adsProvider: AdsProvider = NoOpAdsProvider
}
