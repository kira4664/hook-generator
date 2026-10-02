package com.maeumdeungbul.quotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maeumdeungbul.quotes.domain.model.UserPreferences
import com.maeumdeungbul.quotes.domain.repository.PreferencesRepository
import com.maeumdeungbul.quotes.notification.DailyQuoteScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(val preferences: UserPreferences) : MainUiState
}

class MainViewModel(
    private val preferences: PreferencesRepository,
    private val scheduler: DailyQuoteScheduler,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = preferences.userPreferences
        .map<UserPreferences, MainUiState> { MainUiState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState.Loading)

    fun completeOnboarding(enableNotifications: Boolean) {
        viewModelScope.launch {
            if (enableNotifications) {
                val time = UserPreferences.DEFAULT_NOTIFICATION_TIME
                preferences.setDailyNotification(true, time)
                scheduler.schedule(time)
            }
            preferences.setOnboardingDone()
        }
    }
}
