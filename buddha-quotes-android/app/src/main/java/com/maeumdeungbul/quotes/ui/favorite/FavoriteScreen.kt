package com.maeumdeungbul.quotes.ui.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.ui.appViewModel
import com.maeumdeungbul.quotes.ui.components.LoadingState
import com.maeumdeungbul.quotes.ui.components.QuoteListItem
import com.maeumdeungbul.quotes.ui.components.ShareQuoteSheet
import com.maeumdeungbul.quotes.ui.components.StatusMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    onQuoteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoriteViewModel = appViewModel { FavoriteViewModel(it.quoteRepository) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var shareTarget by remember { mutableStateOf<Quote?>(null) }
    val hasNoFavoritesAtAll = !state.isLoading && !state.isFiltering && state.quotes.isEmpty()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.favorite_title)) }) },
    ) { padding ->
        if (hasNoFavoritesAtAll && !state.isError) {
            StatusMessage(
                title = stringResource(R.string.favorite_empty_title),
                description = stringResource(R.string.favorite_empty_description),
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
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
                    placeholder = { Text(stringResource(R.string.favorite_search_hint)) },
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
            item(key = "categories") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item(key = "all") {
                        FilterChip(
                            selected = state.selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text(stringResource(R.string.filter_all)) },
                        )
                    }
                    items(state.categories, key = { it.id }) { category ->
                        FilterChip(
                            selected = state.selectedCategoryId == category.id,
                            onClick = { viewModel.selectCategory(category.id) },
                            label = { Text(category.name) },
                        )
                    }
                }
            }
            when {
                state.isLoading -> item(key = "loading") { LoadingState() }
                state.isError -> item(key = "error") {
                    StatusMessage(title = stringResource(R.string.error_load_quotes))
                }
                state.quotes.isEmpty() -> item(key = "empty") {
                    StatusMessage(
                        title = stringResource(R.string.search_empty_title),
                        description = stringResource(R.string.search_empty_description),
                    )
                }
                else -> items(state.quotes, key = { it.id }) { quote ->
                    QuoteListItem(
                        quote = quote,
                        onClick = { onQuoteClick(quote.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(quote.id) },
                        onShare = { shareTarget = quote },
                    )
                }
            }
        }
    }

    shareTarget?.let { quote -> ShareQuoteSheet(quote = quote, onDismiss = { shareTarget = null }) }
}
