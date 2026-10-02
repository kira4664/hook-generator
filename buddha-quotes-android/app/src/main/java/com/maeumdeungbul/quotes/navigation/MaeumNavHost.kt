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
import androidx.navigation.toRoute
import com.maeumdeungbul.quotes.ui.favorite.FavoriteScreen
import com.maeumdeungbul.quotes.ui.home.HomeScreen
import com.maeumdeungbul.quotes.ui.meditation.BreathingScreen
import com.maeumdeungbul.quotes.ui.meditation.MeditationCompleteScreen
import com.maeumdeungbul.quotes.ui.meditation.MeditationHistoryScreen
import com.maeumdeungbul.quotes.ui.meditation.MeditationScreen
import com.maeumdeungbul.quotes.ui.meditation.MeditationSessionScreen
import com.maeumdeungbul.quotes.ui.quotes.QuoteDetailScreen
import com.maeumdeungbul.quotes.ui.quotes.QuotesScreen
import com.maeumdeungbul.quotes.ui.settings.LicensesScreen
import com.maeumdeungbul.quotes.ui.settings.SettingsScreen
import kotlin.reflect.KClass

@Composable
fun MaeumNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val openQuote: (Long) -> Unit = { id -> navController.navigate(QuoteDetailRoute(id)) }
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onQuoteClick = openQuote,
                onCategoryClick = { categoryId -> navController.navigate(QuotesRoute(categoryId)) },
                onStartMeditation = { navController.navigateToTopLevel(TopLevelDestination.MEDITATION) },
            )
        }
        composable<QuotesRoute> { entry ->
            val route = entry.toRoute<QuotesRoute>()
            QuotesScreen(
                initialCategoryId = route.categoryId,
                onQuoteClick = openQuote,
                // 홈의 카테고리에서 들어온 경우에만 뒤로가기 버튼을 보여 준다.
                onBack = if (route.categoryId != null) back else null,
            )
        }
        composable<QuoteDetailRoute> { entry ->
            QuoteDetailScreen(
                quoteId = entry.toRoute<QuoteDetailRoute>().quoteId,
                onBack = back,
                onQuoteClick = openQuote,
            )
        }
        composable<FavoriteRoute> { FavoriteScreen(onQuoteClick = openQuote) }

        composable<MeditationRoute> {
            MeditationScreen(
                onStartSession = { navController.navigate(MeditationSessionRoute) { launchSingleTop = true } },
                onOpenBreathing = { navController.navigate(BreathingRoute) },
                onOpenHistory = { navController.navigate(MeditationHistoryRoute) },
            )
        }
        composable<MeditationSessionRoute> {
            MeditationSessionScreen(
                onFinished = {
                    navController.navigate(MeditationCompleteRoute) {
                        popUpTo<MeditationSessionRoute> { inclusive = true }
                    }
                },
                onExit = back,
            )
        }
        composable<MeditationCompleteRoute> {
            MeditationCompleteScreen(
                onOpenHistory = {
                    navController.navigate(MeditationHistoryRoute) {
                        popUpTo<MeditationCompleteRoute> { inclusive = true }
                    }
                },
                onDone = back,
            )
        }
        composable<BreathingRoute> { BreathingScreen(onBack = back) }
        composable<MeditationHistoryRoute> { MeditationHistoryScreen(onBack = back) }

        composable<SettingsRoute> { SettingsScreen(onOpenLicenses = { navController.navigate(LicensesRoute) }) }
        composable<LicensesRoute> { LicensesScreen(onBack = back) }
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
