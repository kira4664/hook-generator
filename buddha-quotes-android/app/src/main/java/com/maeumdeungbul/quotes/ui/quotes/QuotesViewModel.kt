package com.maeumdeungbul.quotes.ui.quotes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maeumdeungbul.quotes.domain.model.Category
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.model.QuoteFilter
import com.maeumdeungbul.quotes.domain.model.QuoteType
import com.maeumdeungbul.quotes.domain.repository.QuoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuotesUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val filter: QuoteFilter = QuoteFilter(),
    val quotes: List<Quote> = emptyList(),
    val categories: List<Category> = emptyList(),
    val origins: List<String> = emptyList(),
    val recent: List<Quote> = emptyList(),
) {
    val hasActiveFilter: Boolean
        get() = filter.query.isNotBlank() || filter.categoryId != null || filter.quoteType != null || filter.origin != null
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class QuotesViewModel(
    private val repository: QuoteRepository,
    initialCategoryId: String?,
) : ViewModel() {

    /** 검색창 입력값. TextField 와 동기적으로 맞추기 위해 Compose State 로 보관한다. */
    var query by mutableStateOf("")
        private set

    private val filter = MutableStateFlow(QuoteFilter(categoryId = initialCategoryId))
    private val retryCount = MutableStateFlow(0)

    private val debouncedQuery = snapshotFlow { query }
        .debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS }
        .map { it.trim() }
        .distinctUntilChanged()

    private val results = combine(debouncedQuery, filter, retryCount) { text, current, _ -> current.copy(query = text) }
        .flatMapLatest { activeFilter ->
            repository.searchQuotes(activeFilter).map<List<Quote>, Result<Pair<QuoteFilter, List<Quote>>>> {
                Result.success(activeFilter to it)
            }.catch { emit(Result.failure(it)) }
        }

    val uiState: StateFlow<QuotesUiState> = combine(
        results,
        repository.observeCategories(),
        repository.observeOrigins(),
        repository.observeRecentlyViewed(RECENT_COUNT),
    ) { result, categories, origins, recent ->
        val (activeFilter, quotes) = result.getOrNull() ?: (filter.value to emptyList())
        QuotesUiState(
            isLoading = false,
            isError = result.isFailure,
            filter = activeFilter,
            quotes = quotes,
            categories = categories,
            origins = origins,
            recent = recent,
        )
    }
        .catch { emit(QuotesUiState(isLoading = false, isError = true, filter = filter.value)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuotesUiState(filter = filter.value))

    fun onQueryChange(value: String) {
        query = value
    }

    fun selectCategory(categoryId: String?) = filter.update { it.copy(categoryId = categoryId) }

    fun selectType(type: QuoteType?) = filter.update { it.copy(quoteType = type) }

    fun selectOrigin(origin: String?) = filter.update { it.copy(origin = origin) }

    fun clearFilters() {
        query = ""
        filter.value = QuoteFilter()
    }

    fun toggleFavorite(quoteId: Long) {
        viewModelScope.launch { repository.toggleFavorite(quoteId) }
    }

    suspend fun randomQuoteId(): Long? = repository.randomQuoteId(excludeId = null)

    fun retry() = retryCount.update { it + 1 }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        const val RECENT_COUNT = 10
    }
}
