package com.maeumdeungbul.quotes.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maeumdeungbul.quotes.navigation.MaeumNavHost
import com.maeumdeungbul.quotes.navigation.NavIcon
import com.maeumdeungbul.quotes.navigation.QuoteDetailRoute
import com.maeumdeungbul.quotes.navigation.TopLevelDestination
import com.maeumdeungbul.quotes.navigation.isRouteInHierarchy
import com.maeumdeungbul.quotes.navigation.navigateToTopLevel

/**
 * @param pendingQuoteId 알림을 눌러 들어온 경우 열어야 할 말씀 id
 * @param onPendingQuoteHandled 이동을 마친 뒤 호출
 */
@Composable
fun MaeumAppRoot(
    pendingQuoteId: Long?,
    onPendingQuoteHandled: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    LaunchedEffect(pendingQuoteId) {
        if (pendingQuoteId != null) {
            navController.navigate(QuoteDetailRoute(pendingQuoteId)) { launchSingleTop = true }
            onPendingQuoteHandled()
        }
    }

    // 상세·명상 진행 화면 등 탭이 아닌 화면에서는 Bottom Bar 를 숨긴다.
    val showBottomBar = currentDestination == null ||
        TopLevelDestination.entries.any { currentDestination.isRouteInHierarchy(it.routeClass) }

    Scaffold(
        // 상단 inset 은 각 화면의 TopAppBar 가 처리한다.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                MaeumBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = navController::navigateToTopLevel,
                )
            }
        },
    ) { innerPadding ->
        MaeumNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

@Composable
private fun MaeumBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentDestination.isRouteInHierarchy(destination.routeClass)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    // 라벨 텍스트가 TalkBack 에 읽히므로 아이콘 설명은 중복하지 않는다.
                    when (val icon = destination.icon) {
                        is NavIcon.Vector -> Icon(icon.imageVector, contentDescription = null)
                        is NavIcon.Resource -> Icon(painterResource(icon.resId), contentDescription = null)
                    }
                },
                label = { Text(stringResource(destination.labelRes)) },
            )
        }
    }
}
