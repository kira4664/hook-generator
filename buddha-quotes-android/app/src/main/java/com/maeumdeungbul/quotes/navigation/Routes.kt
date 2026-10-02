package com.maeumdeungbul.quotes.navigation

import kotlinx.serialization.Serializable

// 타입 안전 Navigation Route.
// 2차 확장 예정: CalendarRoute(불교 달력), LearnRoute / LearnArticleRoute(id)(불교 이야기)

// ── 하단 탭 ──
@Serializable
data object HomeRoute

@Serializable
data class QuotesRoute(val categoryId: String? = null)

@Serializable
data object MeditationRoute

@Serializable
data object FavoriteRoute

@Serializable
data object SettingsRoute

// ── 상세 화면 ──
@Serializable
data class QuoteDetailRoute(val quoteId: Long)

@Serializable
data object MeditationSessionRoute

@Serializable
data object MeditationCompleteRoute

@Serializable
data object BreathingRoute

@Serializable
data object MeditationHistoryRoute

@Serializable
data object LicensesRoute
