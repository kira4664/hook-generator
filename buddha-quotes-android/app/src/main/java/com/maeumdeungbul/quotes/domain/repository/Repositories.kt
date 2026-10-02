package com.maeumdeungbul.quotes.domain.repository

import com.maeumdeungbul.quotes.domain.model.BreathingPattern
import com.maeumdeungbul.quotes.domain.model.Category
import com.maeumdeungbul.quotes.domain.model.FontScale
import com.maeumdeungbul.quotes.domain.model.MeditationSession
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.model.QuoteBackground
import com.maeumdeungbul.quotes.domain.model.QuoteFilter
import com.maeumdeungbul.quotes.domain.model.QuoteReport
import com.maeumdeungbul.quotes.domain.model.ThemeMode
import com.maeumdeungbul.quotes.domain.model.UserPreferences
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    /** 해당 날짜의 "오늘의 말씀". 하루 동안 같은 말씀이 유지된다. */
    fun observeDailyQuote(date: LocalDate): Flow<Quote?>
    suspend fun getDailyQuote(date: LocalDate): Quote?
    fun observeQuote(id: Long): Flow<Quote?>
    fun searchQuotes(filter: QuoteFilter): Flow<List<Quote>>
    fun observeCategories(): Flow<List<Category>>
    /** 인물·출처 필터 선택지 */
    fun observeOrigins(): Flow<List<String>>
    fun observeRecommended(date: LocalDate, count: Int): Flow<List<Quote>>
    fun observeRelated(quote: Quote, count: Int): Flow<List<Quote>>
    fun observeRecentlyViewed(limit: Int): Flow<List<Quote>>
    fun observeFavorites(query: String, categoryId: String?): Flow<List<Quote>>
    fun observeFavoriteCategories(): Flow<List<Category>>
    suspend fun randomQuoteId(excludeId: Long?): Long?
    suspend fun toggleFavorite(quoteId: Long)
    suspend fun markViewed(quoteId: Long)
    suspend fun saveReport(report: QuoteReport)
    suspend fun clearFavorites()
    suspend fun clearRecentlyViewed()
}

interface MeditationRepository {
    fun observeSessions(): Flow<List<MeditationSession>>
    suspend fun saveSession(session: MeditationSession): Long
    suspend fun clearSessions()
}

interface PreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setOnboardingDone()
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setFontScale(scale: FontScale)
    suspend fun setQuoteBackground(background: QuoteBackground)
    suspend fun setAmbientSound(soundId: String?)
    suspend fun setBellEnabled(enabled: Boolean)
    suspend fun setBreathingPattern(pattern: BreathingPattern)
    suspend fun setDailyNotification(enabled: Boolean, time: LocalTime)

    suspend fun getSeededContentVersion(): Int
    suspend fun setSeededContentVersion(version: Int)
    suspend fun getLastNotifiedDate(): LocalDate?
    suspend fun setLastNotifiedDate(date: LocalDate)
    /** 사용자 설정을 기본값으로 되돌린다(콘텐츠 시드 버전은 유지). */
    suspend fun resetUserSettings()
}
