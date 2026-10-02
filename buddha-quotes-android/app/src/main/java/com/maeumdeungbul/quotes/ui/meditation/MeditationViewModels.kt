package com.maeumdeungbul.quotes.ui.meditation

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maeumdeungbul.quotes.domain.model.BreathingPattern
import com.maeumdeungbul.quotes.domain.model.MEDITATION_MIN_RECORD_SECONDS
import com.maeumdeungbul.quotes.domain.model.MeditationSession
import com.maeumdeungbul.quotes.domain.model.MeditationStats
import com.maeumdeungbul.quotes.domain.model.MeditationType
import com.maeumdeungbul.quotes.domain.repository.MeditationRepository
import com.maeumdeungbul.quotes.domain.repository.PreferencesRepository
import com.maeumdeungbul.quotes.domain.usecase.MeditationResult
import com.maeumdeungbul.quotes.domain.usecase.MeditationSessionManager
import com.maeumdeungbul.quotes.domain.usecase.MeditationStatsCalculator
import com.maeumdeungbul.quotes.domain.usecase.TimerState
import com.maeumdeungbul.quotes.playback.MeditationSound
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private fun MeditationRepository.observeStats(): Flow<MeditationStats> =
    observeSessions()
        .map { MeditationStatsCalculator.calculate(it, LocalDate.now(), ZoneId.systemDefault()) }
        .catch { emit(MeditationStats()) }

// ───────────────────────── 명상 홈 ─────────────────────────

data class MeditationUiState(
    val selectedMinutes: Int = DEFAULT_MINUTES,
    val isCustomDuration: Boolean = false,
    val soundId: String? = null,
    val bellEnabled: Boolean = true,
    val stats: MeditationStats = MeditationStats(),
    val sessionInProgress: Boolean = false,
) {
    companion object {
        const val DEFAULT_MINUTES = 10
    }
}

class MeditationViewModel(
    private val preferences: PreferencesRepository,
    meditationRepository: MeditationRepository,
    private val sessionManager: MeditationSessionManager,
) : ViewModel() {

    private val duration = MutableStateFlow(MeditationUiState.DEFAULT_MINUTES to false)

    val uiState: StateFlow<MeditationUiState> = combine(
        duration,
        preferences.userPreferences,
        meditationRepository.observeStats(),
        sessionManager.timerState,
    ) { (minutes, custom), prefs, stats, timer ->
        MeditationUiState(
            selectedMinutes = minutes,
            isCustomDuration = custom,
            soundId = MeditationSound.fromId(prefs.ambientSoundId)?.id,
            bellEnabled = prefs.bellEnabled,
            stats = stats,
            sessionInProgress = timer is TimerState.Running || timer is TimerState.Paused,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MeditationUiState())

    fun selectDuration(minutes: Int, custom: Boolean) {
        duration.value = minutes to custom
    }

    fun selectSound(soundId: String?) {
        viewModelScope.launch { preferences.setAmbientSound(soundId) }
    }

    fun setBellEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setBellEnabled(enabled) }
    }

    fun startSession() {
        if (sessionManager.isActive) return
        val state = uiState.value
        sessionManager.start(state.selectedMinutes, state.soundId, state.bellEnabled)
    }
}

// ───────────────────────── 명상 진행 ─────────────────────────

class MeditationSessionViewModel(
    private val sessionManager: MeditationSessionManager,
) : ViewModel() {
    val timerState: StateFlow<TimerState> = sessionManager.timerState
    val result: StateFlow<MeditationResult?> = sessionManager.result

    fun pause() = sessionManager.pause()
    fun resume() = sessionManager.resume()
    fun stop() = sessionManager.stop()
    fun refresh() = sessionManager.refresh()
}

// ───────────────────────── 명상 완료 ─────────────────────────

class MeditationCompleteViewModel(
    private val sessionManager: MeditationSessionManager,
    meditationRepository: MeditationRepository,
) : ViewModel() {
    /** 화면에 들어온 순간의 결과를 고정해 둔다(이후 consume 되어도 화면 유지). */
    val result: MeditationResult? = sessionManager.result.value

    val stats: StateFlow<MeditationStats> = meditationRepository.observeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MeditationStats())

    override fun onCleared() {
        sessionManager.consumeResult()
    }
}

// ───────────────────────── 호흡 명상 ─────────────────────────

class BreathingViewModel(
    private val preferences: PreferencesRepository,
    private val meditationRepository: MeditationRepository,
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    val pattern: StateFlow<BreathingPattern> = preferences.userPreferences
        .map { it.breathingPattern }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BreathingPattern.CALM)

    var running by mutableStateOf(false)
        private set

    private var startedAt: Instant? = null
    private var startedElapsed = 0L

    fun selectPattern(pattern: BreathingPattern) {
        viewModelScope.launch { preferences.setBreathingPattern(pattern) }
    }

    fun elapsedMillis(): Long = if (running) SystemClock.elapsedRealtime() - startedElapsed else 0L

    fun start() {
        if (running) return
        startedAt = Instant.now()
        startedElapsed = SystemClock.elapsedRealtime()
        running = true
    }

    /** 종료하고 수행한 시간(초)을 돌려준다. 1분 이상이면 기록에 저장한다. */
    fun stop(): Int {
        if (!running) return 0
        running = false
        val seconds = ((SystemClock.elapsedRealtime() - startedElapsed) / 1000L).toInt()
        val start = startedAt ?: return seconds
        startedAt = null
        if (seconds >= MEDITATION_MIN_RECORD_SECONDS) {
            // 화면을 벗어나도 저장이 끝나도록 앱 범위에서 실행한다.
            applicationScope.launch {
                meditationRepository.saveSession(
                    MeditationSession(
                        startedAt = start,
                        plannedSeconds = seconds,
                        actualSeconds = seconds,
                        type = MeditationType.BREATHING,
                        soundId = null,
                        completed = true,
                    ),
                )
            }
        }
        return seconds
    }

    override fun onCleared() {
        stop()
    }
}

// ───────────────────────── 명상 기록 ─────────────────────────

data class MeditationHistoryUiState(
    val isLoading: Boolean = true,
    val stats: MeditationStats = MeditationStats(),
    val month: YearMonth = YearMonth.now(),
    val recentSessions: List<MeditationSession> = emptyList(),
)

class MeditationHistoryViewModel(
    meditationRepository: MeditationRepository,
) : ViewModel() {

    private val month = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<MeditationHistoryUiState> = combine(
        meditationRepository.observeSessions(),
        month,
    ) { sessions, selectedMonth ->
        MeditationHistoryUiState(
            isLoading = false,
            stats = MeditationStatsCalculator.calculate(sessions, LocalDate.now(), ZoneId.systemDefault()),
            month = selectedMonth,
            recentSessions = sessions.take(RECENT_SESSIONS),
        )
    }
        .catch { emit(MeditationHistoryUiState(isLoading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MeditationHistoryUiState())

    fun previousMonth() = month.update { it.minusMonths(1) }

    fun nextMonth() = month.update { current -> if (current < YearMonth.now()) current.plusMonths(1) else current }

    private companion object {
        const val RECENT_SESSIONS = 20
    }
}
