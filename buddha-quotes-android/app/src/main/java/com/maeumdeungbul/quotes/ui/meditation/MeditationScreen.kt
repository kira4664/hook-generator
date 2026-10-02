package com.maeumdeungbul.quotes.ui.meditation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.MEDITATION_MAX_CUSTOM_MINUTES
import com.maeumdeungbul.quotes.domain.model.MeditationDurationPresets
import com.maeumdeungbul.quotes.playback.MeditationSound
import com.maeumdeungbul.quotes.ui.appViewModel
import com.maeumdeungbul.quotes.ui.components.SectionHeader
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MeditationScreen(
    onStartSession: () -> Unit,
    onOpenBreathing: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MeditationViewModel = appViewModel {
        MeditationViewModel(it.preferencesRepository, it.meditationRepository, it.meditationSessionManager)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCustomDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.meditation_title)) },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.meditation_history))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (state.sessionInProgress) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.meditation_in_progress),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        FilledTonalButton(onClick = onStartSession) { Text(stringResource(R.string.meditation_resume_session)) }
                    }
                }
            }

            StatsSummary(todayMinutes = state.stats.todayMinutes, streakDays = state.stats.streakDays, onClick = onOpenHistory)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(stringResource(R.string.meditation_duration))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MeditationDurationPresets.forEach { minutes ->
                        FilterChip(
                            selected = !state.isCustomDuration && state.selectedMinutes == minutes,
                            onClick = { viewModel.selectDuration(minutes, custom = false) },
                            label = { Text(stringResource(R.string.minutes_format, minutes)) },
                        )
                    }
                    FilterChip(
                        selected = state.isCustomDuration,
                        onClick = { showCustomDialog = true },
                        label = {
                            Text(
                                if (state.isCustomDuration) {
                                    stringResource(R.string.meditation_custom_selected, state.selectedMinutes)
                                } else {
                                    stringResource(R.string.meditation_custom)
                                },
                            )
                        },
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(stringResource(R.string.meditation_sound))
                SoundChips(selectedId = state.soundId, onSelect = viewModel::selectSound)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = state.bellEnabled,
                            role = Role.Switch,
                            onValueChange = viewModel::setBellEnabled,
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.meditation_bell), modifier = Modifier.weight(1f))
                    Switch(checked = state.bellEnabled, onCheckedChange = null)
                }
            }

            Button(
                onClick = {
                    viewModel.startSession()
                    onStartSession()
                },
                enabled = !state.sessionInProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(stringResource(R.string.meditation_start), style = MaterialTheme.typography.titleMedium)
            }

            Card(
                onClick = onOpenBreathing,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(stringResource(R.string.breathing_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.breathing_entry_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomDurationDialog(
            initialMinutes = state.selectedMinutes,
            onConfirm = { minutes ->
                viewModel.selectDuration(minutes, custom = true)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false },
        )
    }
}

@Composable
private fun StatsSummary(todayMinutes: Int, streakDays: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            StatItem(stringResource(R.string.stats_today), stringResource(R.string.minutes_format, todayMinutes))
            StatItem(stringResource(R.string.stats_streak), stringResource(R.string.days_format, streakDays))
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoundChips(selectedId: String?, onSelect: (String?) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedId == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.sound_silent)) },
        )
        MeditationSound.available.forEach { sound ->
            FilterChip(
                selected = selectedId == sound.id,
                onClick = { onSelect(sound.id) },
                label = { Text(stringResource(sound.labelRes)) },
            )
        }
    }
}

@Composable
private fun CustomDurationDialog(initialMinutes: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var value by remember { mutableFloatStateOf(initialMinutes.toFloat()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.meditation_custom_title)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.minutes_format, value.roundToInt()),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 1f..MEDITATION_MAX_CUSTOM_MINUTES.toFloat(),
                    steps = MEDITATION_MAX_CUSTOM_MINUTES - 2,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value.roundToInt().coerceIn(1, MEDITATION_MAX_CUSTOM_MINUTES)) }) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
