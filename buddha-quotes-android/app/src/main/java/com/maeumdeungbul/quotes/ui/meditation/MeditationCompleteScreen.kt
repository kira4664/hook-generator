package com.maeumdeungbul.quotes.ui.meditation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.ads.AdPlacement
import com.maeumdeungbul.quotes.ads.AdSlot
import com.maeumdeungbul.quotes.ui.appViewModel

@Composable
fun MeditationCompleteScreen(
    onOpenHistory: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MeditationCompleteViewModel = appViewModel {
        MeditationCompleteViewModel(it.meditationSessionManager, it.meditationRepository)
    },
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val result = viewModel.result
    BackHandler(onBack = onDone)

    val title = when {
        result == null -> stringResource(R.string.meditation_complete_generic)
        !result.recorded -> stringResource(R.string.meditation_complete_too_short)
        result.completed -> stringResource(R.string.meditation_complete_title, result.actualSeconds / 60)
        else -> stringResource(R.string.meditation_complete_partial, result.actualSeconds / 60)
    }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_nav_meditation),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
            )
            Text(text = title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                text = stringResource(R.string.meditation_complete_today_total, stats.todayMinutes, stats.streakDays),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_confirm))
            }
            OutlinedButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.meditation_history))
            }
            // 광고 후보 위치: 명상 완료 후
            AdSlot(AdPlacement.MEDITATION_COMPLETE)
        }
    }
}
