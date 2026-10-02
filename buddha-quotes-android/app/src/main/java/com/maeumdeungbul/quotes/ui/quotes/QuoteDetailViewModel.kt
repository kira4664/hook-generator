package com.maeumdeungbul.quotes.ui.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maeumdeungbul.quotes.domain.model.FontScale
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.model.QuoteReport
import com.maeumdeungbul.quotes.domain.model.ReportReason
import com.maeumdeungbul.quotes.domain.repository.PreferencesRepository
import com.maeumdeungbul.quotes.domain.repository.QuoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuoteDetailUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val quote: Quote? = null,
    val related: List<Quote> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteDetailViewModel(
    private val quoteId: Long,
    private val repository: QuoteRepository,
    private val preferences: PreferencesRepository,
) : ViewModel() {

    private val retryCount = MutableStateFlow(0)

    val uiState: StateFlow<QuoteDetailUiState> = retryCount
        .flatMapLatest {
            repository.observeQuote(quoteId).flatMapLatest { quote ->
                if (quote == null) {
                    flowOf(QuoteDetailUiState(isLoading = false))
                } else {
                    repository.observeRelated(quote, RELATED_COUNT).map { related ->
                        QuoteDetailUiState(isLoading = false, quote = quote, related = related)
                    }
                }
            }.catch { emit(QuoteDetailUiState(isLoading = false, isError = true)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuoteDetailUiState())

    init {
        viewModelScope.launch { runCatching { repository.markViewed(quoteId) } }
    }

    fun toggleFavorite() {
        viewModelScope.launch { repository.toggleFavorite(quoteId) }
    }

    fun setFontScale(scale: FontScale) {
        viewModelScope.launch { preferences.setFontScale(scale) }
    }

    fun report(reason: ReportReason, memo: String) {
        viewModelScope.launch { repository.saveReport(QuoteReport(quoteId, reason, memo)) }
    }

    fun retry() = retryCount.update { it + 1 }

    private companion object {
        const val RELATED_COUNT = 5
    }
}
