package com.maeumdeungbul.quotes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maeumdeungbul.quotes.data.datasource.AssetContentDataSource
import com.maeumdeungbul.quotes.data.model.LicenseJson
import com.maeumdeungbul.quotes.domain.model.FontScale
import com.maeumdeungbul.quotes.domain.model.QuoteBackground
import com.maeumdeungbul.quotes.domain.model.ThemeMode
import com.maeumdeungbul.quotes.domain.model.UserPreferences
import com.maeumdeungbul.quotes.domain.repository.MeditationRepository
import com.maeumdeungbul.quotes.domain.repository.PreferencesRepository
import com.maeumdeungbul.quotes.domain.repository.QuoteRepository
import com.maeumdeungbul.quotes.notification.DailyQuoteScheduler
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ResetTarget { FAVORITES, RECENT, MEDITATION, ALL }

class SettingsViewModel(
    private val preferences: PreferencesRepository,
    private val quoteRepository: QuoteRepository,
    private val meditationRepository: MeditationRepository,
    private val scheduler: DailyQuoteScheduler,
) : ViewModel() {

    val preferencesState: StateFlow<UserPreferences?> = preferences.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setDailyNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val time = preferences.userPreferences.first().dailyNotificationTime
            preferences.setDailyNotification(enabled, time)
            if (enabled) scheduler.schedule(time) else scheduler.cancel()
        }
    }

    fun setDailyNotificationTime(time: LocalTime) {
        viewModelScope.launch {
            val enabled = preferences.userPreferences.first().dailyNotificationEnabled
            preferences.setDailyNotification(enabled, time)
            if (enabled) scheduler.schedule(time)
        }
    }

    fun setFontScale(scale: FontScale) {
        viewModelScope.launch { preferences.setFontScale(scale) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setQuoteBackground(background: QuoteBackground) {
        viewModelScope.launch { preferences.setQuoteBackground(background) }
    }

    fun setAmbientSound(soundId: String?) {
        viewModelScope.launch { preferences.setAmbientSound(soundId) }
    }

    fun setBellEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setBellEnabled(enabled) }
    }

    fun reset(target: ResetTarget) {
        viewModelScope.launch {
            when (target) {
                ResetTarget.FAVORITES -> quoteRepository.clearFavorites()
                ResetTarget.RECENT -> quoteRepository.clearRecentlyViewed()
                ResetTarget.MEDITATION -> meditationRepository.clearSessions()
                ResetTarget.ALL -> {
                    quoteRepository.clearFavorites()
                    quoteRepository.clearRecentlyViewed()
                    meditationRepository.clearSessions()
                    preferences.resetUserSettings()
                    scheduler.cancel()
                }
            }
        }
    }
}

class LicensesViewModel(dataSource: AssetContentDataSource) : ViewModel() {
    private val _libraries = MutableStateFlow<List<LicenseJson>>(emptyList())
    val libraries: StateFlow<List<LicenseJson>> = _libraries.asStateFlow()

    init {
        viewModelScope.launch {
            _libraries.value = runCatching { dataSource.loadLicenses().libraries }.getOrDefault(emptyList())
        }
    }
}
