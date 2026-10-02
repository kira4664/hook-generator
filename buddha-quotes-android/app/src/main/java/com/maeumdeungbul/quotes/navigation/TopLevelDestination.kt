package com.maeumdeungbul.quotes.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.maeumdeungbul.quotes.R

sealed interface NavIcon {
    data class Vector(val imageVector: ImageVector) : NavIcon
    data class Resource(@DrawableRes val resId: Int) : NavIcon
}

/**
 * Bottom Navigation 탭 정의. 탭 구성(예: 2차 업데이트의 달력 탭)은 이 enum 만 수정하면 된다.
 */
enum class TopLevelDestination(
    val route: Any,
    @StringRes val labelRes: Int,
    val icon: NavIcon,
) {
    HOME(HomeRoute, labelRes = R.string.nav_home, icon = NavIcon.Vector(Icons.Filled.Home)),
    QUOTES(QuotesRoute, labelRes = R.string.nav_quotes, icon = NavIcon.Resource(R.drawable.ic_nav_quotes)),
    MEDITATION(MeditationRoute, labelRes = R.string.nav_meditation, icon = NavIcon.Resource(R.drawable.ic_nav_meditation)),
    FAVORITE(FavoriteRoute, labelRes = R.string.nav_favorite, icon = NavIcon.Vector(Icons.Filled.Favorite)),
    SETTINGS(SettingsRoute, labelRes = R.string.nav_settings, icon = NavIcon.Vector(Icons.Filled.Settings)),
}
