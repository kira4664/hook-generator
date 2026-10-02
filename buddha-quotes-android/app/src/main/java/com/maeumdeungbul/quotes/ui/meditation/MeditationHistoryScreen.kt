package com.maeumdeungbul.quotes.ui.meditation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.MeditationSession
import com.maeumdeungbul.quotes.domain.model.MeditationStats
import com.maeumdeungbul.quotes.domain.model.MeditationType
import com.maeumdeungbul.quotes.ui.appViewModel
import com.maeumdeungbul.quotes.ui.components.LoadingState
import com.maeumdeungbul.quotes.ui.components.SectionHeader
import com.maeumdeungbul.quotes.ui.components.StatusMessage
import com.maeumdeungbul.quotes.util.Formatters
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MeditationHistoryViewModel = appViewModel { MeditationHistoryViewModel(it.meditationRepository) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.meditation_history)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "stats") { StatsGrid(state.stats) }
            item(key = "calendar") {
                MonthCalendar(
                    month = state.month,
                    practicedDays = state.stats.practicedDays,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth,
                )
            }
            item(key = "recent_header") { SectionHeader(stringResource(R.string.history_recent)) }
            if (state.recentSessions.isEmpty()) {
                item(key = "empty") {
                    StatusMessage(
                        title = stringResource(R.string.history_empty_title),
                        description = stringResource(R.string.history_empty_description),
                    )
                }
            } else {
                items(state.recentSessions, key = { it.id }) { session -> SessionRow(session) }
            }
        }
    }
}

@Composable
private fun StatsGrid(stats: MeditationStats) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                StatItem(stringResource(R.string.stats_today), stringResource(R.string.minutes_format, stats.todayMinutes), Modifier.weight(1f))
                StatItem(stringResource(R.string.stats_week), stringResource(R.string.minutes_format, stats.weekMinutes), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth()) {
                StatItem(stringResource(R.string.stats_month), stringResource(R.string.minutes_format, stats.monthMinutes), Modifier.weight(1f))
                StatItem(stringResource(R.string.stats_streak), stringResource(R.string.days_format, stats.streakDays), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MonthCalendar(
    month: YearMonth,
    practicedDays: Set<LocalDate>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val today = LocalDate.now()
    val practicedLabel = stringResource(R.string.history_day_practiced)
    val weekdays = listOf("일", "월", "화", "수", "목", "금", "토")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.history_previous_month))
            }
            Text(
                text = Formatters.yearMonth(month.atDay(1)),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onNext, enabled = month < YearMonth.now()) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.history_next_month))
            }
        }
        Row(Modifier.fillMaxWidth()) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        // 일요일 시작 달력. DayOfWeek: 월=1 … 일=7
        val leadingBlanks = month.atDay(1).dayOfWeek.value % 7
        val cells = List(leadingBlanks) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (date != null) {
                            val practiced = date in practicedDays
                            val description = if (practiced) {
                                "${Formatters.shortDate(date)}, $practicedLabel"
                            } else {
                                Formatters.shortDate(date)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .then(
                                        if (practiced) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier,
                                    )
                                    .then(
                                        if (date == today) {
                                            Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        } else {
                                            Modifier
                                        },
                                    )
                                    .clearAndSetSemantics { contentDescription = description },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (practiced) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
                repeat(7 - week.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun SessionRow(session: MeditationSession) {
    val date = session.startedAt.atZone(ZoneId.systemDefault()).toLocalDate()
    val minutes = session.actualSeconds / 60
    ListItem(
        headlineContent = {
            Text(
                stringResource(
                    if (session.type == MeditationType.BREATHING) R.string.history_type_breathing else R.string.history_type_timer,
                ),
            )
        },
        supportingContent = { Text(Formatters.shortDate(date)) },
        trailingContent = { Text(stringResource(R.string.minutes_format, minutes)) },
    )
}
