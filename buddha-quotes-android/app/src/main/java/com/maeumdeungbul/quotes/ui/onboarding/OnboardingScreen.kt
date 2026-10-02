package com.maeumdeungbul.quotes.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.ui.components.NotificationDeniedDialog
import com.maeumdeungbul.quotes.ui.components.rememberNotificationPermissionRequest
import kotlinx.coroutines.launch

private data class OnboardingPage(
    @StringRes val title: Int,
    @StringRes val body: Int,
    val icon: Int,
)

private val pages = listOf(
    OnboardingPage(R.string.onboarding_title_1, R.string.onboarding_body_1, R.drawable.ic_nav_quotes),
    OnboardingPage(R.string.onboarding_title_2, R.string.onboarding_body_2, R.drawable.ic_nav_meditation),
    OnboardingPage(R.string.onboarding_title_3, R.string.onboarding_body_3, R.drawable.ic_notification),
)

/**
 * 최초 실행 시 3페이지 안내. 알림 권한은 마지막 페이지에서 사용자가 "알림 받기"를 누를 때만 요청한다.
 * [onFinish] 의 인자는 매일 알림을 켤지 여부.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: (enableNotifications: Boolean) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    var showDenied by remember { mutableStateOf(false) }
    val requestPermission = rememberNotificationPermissionRequest { granted ->
        if (granted) onFinish(true) else showDenied = true
    }
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!isLastPage) {
                    TextButton(onClick = { onFinish(false) }) { Text(stringResource(R.string.onboarding_skip)) }
                } else {
                    Spacer(Modifier.height(48.dp))
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { index ->
                PageContent(pages[index])
            }
            PageIndicator(count = pages.size, current = pagerState.currentPage)
            Spacer(Modifier.height(24.dp))
            if (isLastPage) {
                Button(onClick = requestPermission, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.onboarding_enable_notifications))
                }
                OutlinedButton(onClick = { onFinish(false) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.onboarding_start))
                }
            } else {
                Button(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.onboarding_next))
                }
            }
        }
    }

    if (showDenied) {
        NotificationDeniedDialog(onDismiss = {
            showDenied = false
            onFinish(false)
        })
    }
}

@Composable
private fun PageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(page.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(72.dp),
            )
        }
        Text(
            text = stringResource(page.title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(page.body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int) {
    val description = stringResource(R.string.onboarding_page_indicator, current + 1, count)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        repeat(count) { index ->
            Box(
                Modifier
                    .size(if (index == current) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        }
    }
}
