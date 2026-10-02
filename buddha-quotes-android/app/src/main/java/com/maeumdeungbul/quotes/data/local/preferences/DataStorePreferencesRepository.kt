package com.maeumdeungbul.quotes.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.maeumdeungbul.quotes.domain.model.BreathingPattern
import com.maeumdeungbul.quotes.domain.model.FontScale
import com.maeumdeungbul.quotes.domain.model.QuoteBackground
import com.maeumdeungbul.quotes.domain.model.ThemeMode
import com.maeumdeungbul.quotes.domain.model.UserPreferences
import com.maeumdeungbul.quotes.domain.repository.PreferencesRepository
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class DataStorePreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SCALE = stringPreferencesKey("font_scale")
        val QUOTE_BACKGROUND = stringPreferencesKey("quote_background")
        val AMBIENT_SOUND = stringPreferencesKey("ambient_sound")
        val BELL_ENABLED = booleanPreferencesKey("bell_enabled")
        val BREATHING_PATTERN = stringPreferencesKey("breathing_pattern")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val NOTIFICATION_MINUTE_OF_DAY = intPreferencesKey("notification_minute_of_day")
        val SEEDED_CONTENT_VERSION = intPreferencesKey("seeded_content_version")
        val LAST_NOTIFIED_DATE = stringPreferencesKey("last_notified_date")
    }

    private val safeData: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    override val userPreferences: Flow<UserPreferences> = safeData.map { prefs ->
        val defaults = UserPreferences()
        UserPreferences(
            onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: defaults.onboardingDone,
            themeMode = prefs[Keys.THEME_MODE].toEnumOrNull<ThemeMode>() ?: defaults.themeMode,
            fontScale = prefs[Keys.FONT_SCALE].toEnumOrNull<FontScale>() ?: defaults.fontScale,
            quoteBackground = prefs[Keys.QUOTE_BACKGROUND].toEnumOrNull<QuoteBackground>() ?: defaults.quoteBackground,
            ambientSoundId = prefs[Keys.AMBIENT_SOUND],
            bellEnabled = prefs[Keys.BELL_ENABLED] ?: defaults.bellEnabled,
            breathingPattern = BreathingPattern.fromId(prefs[Keys.BREATHING_PATTERN]),
            dailyNotificationEnabled = prefs[Keys.NOTIFICATION_ENABLED] ?: defaults.dailyNotificationEnabled,
            dailyNotificationTime = prefs[Keys.NOTIFICATION_MINUTE_OF_DAY]
                ?.let { LocalTime.of(it / 60 % 24, it % 60) }
                ?: defaults.dailyNotificationTime,
        )
    }

    override suspend fun setOnboardingDone() {
        dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setFontScale(scale: FontScale) {
        dataStore.edit { it[Keys.FONT_SCALE] = scale.name }
    }

    override suspend fun setQuoteBackground(background: QuoteBackground) {
        dataStore.edit { it[Keys.QUOTE_BACKGROUND] = background.name }
    }

    override suspend fun setAmbientSound(soundId: String?) {
        dataStore.edit { prefs ->
            if (soundId == null) {
                prefs.remove(Keys.AMBIENT_SOUND)
            } else {
                prefs[Keys.AMBIENT_SOUND] = soundId
            }
        }
    }

    override suspend fun setBellEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BELL_ENABLED] = enabled }
    }

    override suspend fun setBreathingPattern(pattern: BreathingPattern) {
        dataStore.edit { it[Keys.BREATHING_PATTERN] = pattern.id }
    }

    override suspend fun setDailyNotification(enabled: Boolean, time: LocalTime) {
        dataStore.edit {
            it[Keys.NOTIFICATION_ENABLED] = enabled
            it[Keys.NOTIFICATION_MINUTE_OF_DAY] = time.hour * 60 + time.minute
        }
    }

    override suspend fun getSeededContentVersion(): Int =
        safeData.first()[Keys.SEEDED_CONTENT_VERSION] ?: 0

    override suspend fun setSeededContentVersion(version: Int) {
        dataStore.edit { it[Keys.SEEDED_CONTENT_VERSION] = version }
    }

    override suspend fun getLastNotifiedDate(): LocalDate? =
        safeData.first()[Keys.LAST_NOTIFIED_DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    override suspend fun setLastNotifiedDate(date: LocalDate) {
        dataStore.edit { it[Keys.LAST_NOTIFIED_DATE] = date.toString() }
    }

    override suspend fun resetUserSettings() {
        dataStore.edit { prefs ->
            val seededVersion = prefs[Keys.SEEDED_CONTENT_VERSION]
            prefs.clear()
            prefs[Keys.ONBOARDING_DONE] = true
            if (seededVersion != null) prefs[Keys.SEEDED_CONTENT_VERSION] = seededVersion
        }
    }
}

private inline fun <reified T : Enum<T>> String?.toEnumOrNull(): T? =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }
