package com.maeumdeungbul.quotes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maeumdeungbul.quotes.domain.model.Category
import com.maeumdeungbul.quotes.domain.model.Quote
import com.maeumdeungbul.quotes.domain.repository.QuoteRepository
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val dailyQuote: Quote? = null,
    /** "다른 말씀 보기"로 띄운 말씀. 오늘의 말씀은 바뀌지 않는다. */
    val anotherQuote: Quote? = null,
    val recommended: List<Quote> = emptyList(),
    val categories: List<Category> = emptyList(),
    val recent: List<Quote> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: QuoteRepository,
) : ViewModel() {

    private val date = MutableStateFlow(LocalDate.now())
    private val anotherQuoteId = MutableStateFlow<Long?>(null)
    private val retryCount = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(date, retryCount) { day, _ -> day }
        .flatMapLatest { day ->
            combine(
                repository.observeDailyQuote(day),
                anotherQuoteId.flatMapLatest { id -> if (id == null) flowOf(null) else repository.observeQuote(id) },
                repository.observeRecommended(day, RECOMMENDED_COUNT),
                repository.observeCategories(),
                repository.observeRecentlyViewed(RECENT_COUNT),
            ) { daily, another, recommended, categories, recent ->
                HomeUiState(
                    isLoading = false,
                    date = day,
                    dailyQuote = daily,
                    anotherQuote = another,
                    recommended = recommended,
                    categories = categories,
                    recent = recent,
                )
            }.catch { emit(HomeUiState(isLoading = false, isError = true, date = day)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** 화면이 다시 보일 때 호출. 자정이 지났다면 새 오늘의 말씀으로 바뀐다. */
    fun refreshDate() {
        val today = LocalDate.now()
        if (date.value != today) {
            anotherQuoteId.value = null
            date.value = today
        }
    }

    fun showAnotherQuote() {
        viewModelScope.launch {
            val current = uiState.value.anotherQuote?.id ?: uiState.value.dailyQuote?.id
            repository.randomQuoteId(excludeId = current)?.let { anotherQuoteId.value = it }
        }
    }

    fun backToDailyQuote() {
        anotherQuoteId.value = null
    }

    fun toggleFavorite(quoteId: Long) {
        viewModelScope.launch { repository.toggleFavorite(quoteId) }
    }

    fun retry() {
        retryCount.update { it + 1 }
    }

    private companion object {
        const val RECOMMENDED_COUNT = 5
        const val RECENT_COUNT = 5
    }
}
