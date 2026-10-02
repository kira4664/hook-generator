package com.maeumdeungbul.quotes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.maeumdeungbul.quotes.ui.favorite.FavoriteScreen
import com.maeumdeungbul.quotes.ui.home.HomeScreen
import com.maeumdeungbul.quotes.ui.meditation.MeditationScreen
import com.maeumdeungbul.quotes.ui.quotes.QuotesScreen
import com.maeumdeungbul.quotes.ui.settings.SettingsScreen
import kotlin.reflect.KClass

@Composable
fun MaeumNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onStartMeditation = { navController.navigateToTopLevel(TopLevelDestination.MEDITATION) },
            )
        }
        composable<QuotesRoute> { QuotesScreen() }
        composable<MeditationRoute> { MeditationScreen() }
        composable<FavoriteRoute> { FavoriteScreen() }
        composable<SettingsRoute> { SettingsScreen() }
    }
}

/** 탭 전환: 시작 화면까지 pop 하면서 각 탭의 상태(스크롤 등)를 저장·복원한다. */
fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavDestination?.isRouteInHierarchy(route: KClass<*>): Boolean =
    this?.hierarchy?.any { it.hasRoute(route) } ?: false
