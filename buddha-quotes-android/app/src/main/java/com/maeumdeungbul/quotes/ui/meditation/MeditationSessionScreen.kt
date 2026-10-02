package com.maeumdeungbul.quotes.ui.meditation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.usecase.TimerState
import com.maeumdeungbul.quotes.ui.appViewModel
import com.maeumdeungbul.quotes.util.Formatters

/** 명상 진행 화면. 집중을 위해 광고·하단 탭을 표시하지 않는다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationSessionScreen(
    onFinished: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MeditationSessionViewModel = appViewModel { MeditationSessionViewModel(it.meditationSessionManager) },
) {
    val timer by viewModel.timerState.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    var showStopDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(result) {
        if (result != null) onFinished()
    }
    LaunchedEffect(timer, result) {
        // 진행 중인 명상이 없는 상태로 들어온 경우(예: 앱 프로세스 재시작) 이전 화면으로 돌아간다.
        if (timer is TimerState.Idle && result == null) onExit()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    KeepScreenOn()
    BackHandler { showStopDialog = true }

    val (total, remaining, paused) = when (val current = timer) {
        is TimerState.Running -> Triple(current.totalMillis, current.remainingMillis, false)
        is TimerState.Paused -> Triple(current.totalMillis, current.remainingMillis, true)
        is TimerState.Finished -> Triple(current.totalMillis, 0L, false)
        TimerState.Idle -> Triple(1L, 0L, false)
    }
    val progress = if (total > 0) 1f - remaining.toFloat() / total else 0f
    val remainingText = Formatters.clock(remaining)
    val remainingDescription = stringResource(R.string.meditation_remaining_description, remainingText)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { showStopDialog = true }) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.meditation_stop))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = stringResource(if (paused) R.string.meditation_paused else R.string.meditation_focus),
                style = MaterialTheme.typography.titleLarge,
            )

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
                BreathingGlow(animate = !paused)
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(280.dp),
                    strokeWidth = 6.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = remainingText,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.semantics { contentDescription = remainingDescription },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { if (paused) viewModel.resume() else viewModel.pause() }) {
                    Text(stringResource(if (paused) R.string.meditation_resume else R.string.meditation_pause))
                }
                OutlinedButton(onClick = { showStopDialog = true }) {
                    Text(stringResource(R.string.meditation_stop))
                }
            }
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text(stringResource(R.string.meditation_stop_title)) },
            text = { Text(stringResource(R.string.meditation_stop_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showStopDialog = false
                    viewModel.stop()
                }) { Text(stringResource(R.string.meditation_stop)) }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) { Text(stringResource(R.string.meditation_continue)) }
            },
        )
    }
}

/** 타이머 뒤에서 천천히 커졌다 작아지는 빛. */
@Composable
private fun BreathingGlow(animate: Boolean) {
    val color = MaterialTheme.colorScheme.primaryContainer
    val transition = rememberInfiniteTransition(label = "glow")
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 5000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(
        Modifier
            .size(280.dp)
            .graphicsLayer {
                val scale = if (animate) pulse else 0.88f
                scaleX = scale
                scaleY = scale
            }
            .drawBehind { drawCircle(color) },
    )
}

/** 진행 중에는 화면이 꺼지지 않게 한다. */
@Composable
fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
