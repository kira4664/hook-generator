package com.maeumdeungbul.quotes.ui.meditation

import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.BreathPhase
import com.maeumdeungbul.quotes.domain.model.BreathingPattern
import com.maeumdeungbul.quotes.domain.model.MEDITATION_MIN_RECORD_SECONDS
import com.maeumdeungbul.quotes.ui.appViewModel
import com.maeumdeungbul.quotes.util.Formatters
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val MIN_SCALE = 0.55f
private const val MAX_SCALE = 1f

@StringRes
private fun BreathPhase.labelRes(): Int = when (this) {
    BreathPhase.INHALE -> R.string.breath_inhale
    BreathPhase.HOLD -> R.string.breath_hold
    BreathPhase.EXHALE -> R.string.breath_exhale
    BreathPhase.HOLD_EMPTY -> R.string.breath_hold
}

@StringRes
fun BreathingPattern.labelRes(): Int = when (this) {
    BreathingPattern.CALM -> R.string.breathing_pattern_calm
    BreathingPattern.BOX -> R.string.breathing_pattern_box
    BreathingPattern.RELAX -> R.string.breathing_pattern_relax
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BreathingViewModel = appViewModel {
        BreathingViewModel(it.preferencesRepository, it.meditationRepository, it.applicationScope)
    },
) {
    val pattern by viewModel.pattern.collectAsStateWithLifecycle()
    val running = viewModel.running
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val recordedMessage = stringResource(R.string.breathing_recorded)
    // 시스템 "애니메이션 제거" 설정을 존중한다.
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    val scale = remember { Animatable(MIN_SCALE) }
    var phase by remember { mutableStateOf<BreathPhase?>(null) }
    var secondsLeft by remember { mutableIntStateOf(0) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }

    LaunchedEffect(running, pattern) {
        if (!running) {
            phase = null
            scale.animateTo(MIN_SCALE, tween(600))
            return@LaunchedEffect
        }
        while (isActive) {
            for (step in pattern.steps) {
                phase = step.phase
                coroutineScope {
                    launch {
                        val target = when (step.phase) {
                            BreathPhase.INHALE -> MAX_SCALE
                            BreathPhase.EXHALE -> MIN_SCALE
                            BreathPhase.HOLD, BreathPhase.HOLD_EMPTY -> scale.value
                        }
                        if (reduceMotion) {
                            scale.snapTo(target)
                        } else {
                            scale.animateTo(target, tween(step.seconds * 1000, easing = FastOutSlowInEasing))
                        }
                    }
                    for (second in step.seconds downTo 1) {
                        secondsLeft = second
                        delay(1000)
                    }
                }
            }
        }
    }
    LaunchedEffect(running) {
        while (running) {
            elapsedMillis = viewModel.elapsedMillis()
            delay(1000)
        }
        elapsedMillis = 0L
    }
    if (running) KeepScreenOn()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.breathing_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BreathingPattern.entries, key = { it.id }) { option ->
                    FilterChip(
                        selected = option == pattern,
                        onClick = { viewModel.selectPattern(option) },
                        enabled = !running,
                        label = { Text(stringResource(option.labelRes())) },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                val circleColor = MaterialTheme.colorScheme.primaryContainer
                Box(
                    Modifier
                        .size(280.dp)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                        .drawBehind { drawCircle(circleColor) },
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                ) {
                    val currentPhase = phase
                    Text(
                        text = if (currentPhase == null) stringResource(R.string.breathing_ready) else stringResource(currentPhase.labelRes()),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    if (currentPhase != null) {
                        Text(
                            text = stringResource(R.string.seconds_format, secondsLeft),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Text(
                text = if (running) Formatters.clock(elapsedMillis) else stringResource(R.string.breathing_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = {
                    if (running) {
                        val seconds = viewModel.stop()
                        if (seconds >= MEDITATION_MIN_RECORD_SECONDS) {
                            scope.launch { snackbarHostState.showSnackbar(recordedMessage.format(seconds / 60)) }
                        }
                    } else {
                        viewModel.start()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(stringResource(if (running) R.string.breathing_stop else R.string.breathing_start))
            }
        }
    }
}
