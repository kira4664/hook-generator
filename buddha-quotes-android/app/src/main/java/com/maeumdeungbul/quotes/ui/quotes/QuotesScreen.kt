package com.maeumdeungbul.quotes.ui.quotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.ads.AdPlacement
import com.maeumdeungbul.quotes.ads.AdSlot
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.model.QuoteType
import com.maeumdeungbul.quotes.ui.appViewModel
import com.maeumdeungbul.quotes.ui.components.LoadingState
import com.maeumdeungbul.quotes.ui.components.QuoteCompactCard
import com.maeumdeungbul.quotes.ui.components.QuoteListItem
import com.maeumdeungbul.quotes.ui.components.SectionHeader
import com.maeumdeungbul.quotes.ui.components.ShareQuoteSheet
import com.maeumdeungbul.quotes.ui.components.StatusMessage
import com.maeumdeungbul.quotes.ui.components.labelRes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    initialCategoryId: String?,
    onQuoteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    viewModel: QuotesViewModel = appViewModel { QuotesViewModel(it.quoteRepository, initialCategoryId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var shareTarget by remember { mutableStateOf<Quote?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.quotes_title)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { viewModel.randomQuoteId()?.let(onQuoteClick) } }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.quotes_random))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "search") {
                OutlinedTextField(
                    value = viewModel.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.quotes_search_hint)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (viewModel.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear_search))
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    shape = MaterialTheme.shapes.large,
                )
            }

            item(key = "filters") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item(key = "all") {
                            FilterChip(
                                selected = state.filter.categoryId == null,
                                onClick = { viewModel.selectCategory(null) },
                                label = { Text(stringResource(R.string.filter_all)) },
                            )
                        }
                        items(state.categories, key = { it.id }) { category ->
                            FilterChip(
                                selected = state.filter.categoryId == category.id,
                                onClick = { viewModel.selectCategory(category.id) },
                                label = { Text(category.name) },
                            )
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(QuoteType.entries, key = { it.name }) { type ->
                            FilterChip(
                                selected = state.filter.quoteType == type,
                                onClick = { viewModel.selectType(if (state.filter.quoteType == type) null else type) },
                                label = { Text(stringResource(type.labelRes())) },
                            )
                        }
                        item(key = "origin") {
                            OriginFilterChip(
                                origins = state.origins,
                                selected = state.filter.origin,
                                onSelect = viewModel::selectOrigin,
                            )
                        }
                    }
                }
            }

            if (!state.hasActiveFilter && state.recent.isNotEmpty()) {
                item(key = "recent") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(stringResource(R.string.home_recent))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.recent, key = { it.id }) { quote ->
                                QuoteCompactCard(quote = quote, onClick = { onQuoteClick(quote.id) })
                            }
                        }
                    }
                }
            }

            when {
                state.isLoading -> item(key = "loading") { LoadingState() }
                state.isError -> item(key = "error") {
                    StatusMessage(
                        title = stringResource(R.string.error_load_quotes),
                        actionLabel = stringResource(R.string.action_retry),
                        onAction = viewModel::retry,
                    )
                }
                state.quotes.isEmpty() -> item(key = "empty") {
                    StatusMessage(
                        title = stringResource(R.string.search_empty_title),
                        description = stringResource(R.string.search_empty_description),
                        actionLabel = if (state.hasActiveFilter) stringResource(R.string.action_clear_filters) else null,
                        onAction = viewModel::clearFilters,
                    )
                }
                else -> {
                    item(key = "count") {
                        Text(
                            text = stringResource(R.string.quotes_count, state.quotes.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    itemsIndexed(state.quotes, key = { _, quote -> quote.id }) { index, quote ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuoteListItem(
                                quote = quote,
                                onClick = { onQuoteClick(quote.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(quote.id) },
                                onShare = { shareTarget = quote },
                            )
                            // 광고 후보 위치: 목록 중간(광고 SDK 연동 전에는 아무것도 그리지 않음)
                            if ((index + 1) % AD_INTERVAL == 0) AdSlot(AdPlacement.QUOTE_LIST_INLINE)
                        }
                    }
                }
            }
        }
    }

    shareTarget?.let { quote -> ShareQuoteSheet(quote = quote, onDismiss = { shareTarget = null }) }
}

private const val AD_INTERVAL = 10

@Composable
private fun OriginFilterChip(
    origins: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selected != null,
            onClick = { expanded = true },
            label = { Text(selected ?: stringResource(R.string.filter_origin)) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_origin_all)) },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            origins.forEach { origin ->
                DropdownMenuItem(
                    text = { Text(origin) },
                    onClick = {
                        onSelect(origin)
                        expanded = false
                    },
                )
            }
        }
    }
}
