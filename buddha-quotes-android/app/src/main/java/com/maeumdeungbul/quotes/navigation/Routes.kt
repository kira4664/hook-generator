package com.maeumdeungbul.quotes.navigation

import kotlinx.serialization.Serializable

// 타입 안전 Navigation Route. 인자가 필요한 화면(QuoteDetailRoute 등)은 해당 Phase에서 추가한다.

@Serializable
data object HomeRoute

@Serializable
data object QuotesRoute

@Serializable
data object MeditationRoute

@Serializable
data object FavoriteRoute

@Serializable
data object SettingsRoute
