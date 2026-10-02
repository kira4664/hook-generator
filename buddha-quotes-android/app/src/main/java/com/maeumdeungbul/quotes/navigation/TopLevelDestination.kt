package com.maeumdeungbul.quotes.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.maeumdeungbul.quotes.R
import kotlin.reflect.KClass

sealed interface NavIcon {
    data class Vector(val imageVector: ImageVector) : NavIcon
    data class Resource(@DrawableRes val resId: Int) : NavIcon
}

/**
 * Bottom Navigation 탭 정의. 탭 구성(예: 2차 업데이트의 달력 탭)은 이 enum 만 수정하면 된다.
 */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    @StringRes val labelRes: Int,
    val icon: NavIcon,
) {
    HOME(HomeRoute, HomeRoute::class, R.string.nav_home, NavIcon.Vector(Icons.Filled.Home)),
    QUOTES(QuotesRoute(), QuotesRoute::class, R.string.nav_quotes, NavIcon.Resource(R.drawable.ic_nav_quotes)),
    MEDITATION(MeditationRoute, MeditationRoute::class, R.string.nav_meditation, NavIcon.Resource(R.drawable.ic_nav_meditation)),
    FAVORITE(FavoriteRoute, FavoriteRoute::class, R.string.nav_favorite, NavIcon.Vector(Icons.Filled.Favorite)),
    SETTINGS(SettingsRoute, SettingsRoute::class, R.string.nav_settings, NavIcon.Vector(Icons.Filled.Settings)),
}
