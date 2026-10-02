package com.maeumdeungbul.quotes.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.Category
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.ui.appViewModel
import com.maeumdeungbul.quotes.ui.components.LoadingState
import com.maeumdeungbul.quotes.ui.components.QuoteCompactCard
import com.maeumdeungbul.quotes.ui.components.QuoteHeroCard
import com.maeumdeungbul.quotes.ui.components.SectionHeader
import com.maeumdeungbul.quotes.ui.components.ShareQuoteSheet
import com.maeumdeungbul.quotes.ui.components.StatusMessage
import com.maeumdeungbul.quotes.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onQuoteClick: (Long) -> Unit,
    onCategoryClick: (String) -> Unit,
    onStartMeditation: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = appViewModel { HomeViewModel(it.quoteRepository) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var shareTarget by remember { mutableStateOf<Quote?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshDate() }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(key = "date") {
                Text(
                    text = Formatters.fullDate(state.date),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item(key = "today") {
                val shown = state.anotherQuote ?: state.dailyQuote
                when {
                    state.isLoading -> LoadingState()
                    state.isError -> StatusMessage(
                        title = stringResource(R.string.error_load_quotes),
                        actionLabel = stringResource(R.string.action_retry),
                        onAction = viewModel::retry,
                    )
                    shown == null -> StatusMessage(title = stringResource(R.string.error_no_quotes))
                    else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        QuoteHeroCard(
                            quote = shown,
                            label = stringResource(
                                if (state.anotherQuote != null) R.string.home_another_quote_label else R.string.home_today_quote_label,
                            ),
                            onClick = { onQuoteClick(shown.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(shown.id) },
                            onShare = { shareTarget = shown },
                        )
                        Row {
                            TextButton(onClick = viewModel::showAnotherQuote) {
                                Text(stringResource(R.string.home_show_another))
                            }
                            if (state.anotherQuote != null) {
                                TextButton(onClick = viewModel::backToDailyQuote) {
                                    Text(stringResource(R.string.home_back_to_today))
                                }
                            }
                        }
                    }
                }
            }

            item(key = "meditation") { MeditationEntryCard(onStartMeditation) }

            if (state.recommended.isNotEmpty()) {
                item(key = "recommended") {
                    QuoteRow(
                        title = stringResource(R.string.home_recommended),
                        quotes = state.recommended,
                        onQuoteClick = onQuoteClick,
                    )
                }
            }

            if (state.categories.isNotEmpty()) {
                item(key = "categories") { CategorySection(state.categories, onCategoryClick) }
            }

            if (state.recent.isNotEmpty()) {
                item(key = "recent") {
                    QuoteRow(
                        title = stringResource(R.string.home_recent),
                        quotes = state.recent,
                        onQuoteClick = onQuoteClick,
                    )
                }
            }
        }
    }

    shareTarget?.let { quote -> ShareQuoteSheet(quote = quote, onDismiss = { shareTarget = null }) }
}

@Composable
private fun MeditationEntryCard(onStartMeditation: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.home_meditation_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Button(onClick = onStartMeditation) {
                Text(stringResource(R.string.home_start_meditation))
            }
        }
    }
}

@Composable
private fun QuoteRow(title: String, quotes: List<Quote>, onQuoteClick: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(quotes, key = { it.id }) { quote ->
                QuoteCompactCard(quote = quote, onClick = { onQuoteClick(quote.id) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(stringResource(R.string.home_categories))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                AssistChip(
                    onClick = { onCategoryClick(category.id) },
                    label = { Text(category.name) },
                )
            }
        }
    }
}
