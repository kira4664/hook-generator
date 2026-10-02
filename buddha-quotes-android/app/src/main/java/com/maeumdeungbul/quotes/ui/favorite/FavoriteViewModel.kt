package com.maeumdeungbul.quotes.ui.favorite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maeumdeungbul.quotes.domain.model.Category
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.repository.QuoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoriteUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val quotes: List<Quote> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val isFiltering: Boolean = false,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class FavoriteViewModel(
    private val repository: QuoteRepository,
) : ViewModel() {

    var query by mutableStateOf("")
        private set

    private val categoryId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FavoriteUiState> = combine(
        snapshotFlow { query }.debounce { if (it.isBlank()) 0L else 300L },
        categoryId,
    ) { text, category -> text.trim() to category }
        .flatMapLatest { (text, category) ->
            combine(
                repository.observeFavorites(text, category),
                repository.observeFavoriteCategories(),
            ) { quotes, categories ->
                FavoriteUiState(
                    isLoading = false,
                    quotes = quotes,
                    categories = categories,
                    // 선택한 카테고리의 말씀을 모두 해제하면 칩 목록에서 사라지므로 선택도 해제된 것으로 본다.
                    selectedCategoryId = category?.takeIf { id -> categories.any { it.id == id } },
                    isFiltering = text.isNotEmpty() || category != null,
                )
            }.catch { emit(FavoriteUiState(isLoading = false, isError = true)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoriteUiState())

    fun onQueryChange(value: String) {
        query = value
    }

    fun selectCategory(id: String?) {
        categoryId.value = id
    }

    fun toggleFavorite(quoteId: Long) {
        viewModelScope.launch { repository.toggleFavorite(quoteId) }
    }
}
