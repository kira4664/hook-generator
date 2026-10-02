package com.maeumdeungbul.quotes.domain.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface TimerState {
    data object Idle : TimerState
    data class Running(val totalMillis: Long, val remainingMillis: Long) : TimerState
    data class Paused(val totalMillis: Long, val remainingMillis: Long) : TimerState
    data class Finished(val totalMillis: Long, val elapsedMillis: Long, val completed: Boolean) : TimerState
}

/**
 * 명상 카운트다운 타이머.
 * tick 횟수가 아니라 단조 시계([elapsedRealtime], Android 에서는 SystemClock.elapsedRealtime)로
 * 남은 시간을 계산하므로, 지연·화면 꺼짐이 있어도 시간이 밀리지 않는다.
 */
class MeditationTimer(
    private val scope: CoroutineScope,
    private val elapsedRealtime: () -> Long,
    private val tickMillis: Long = 200L,
) {
    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var totalMillis = 0L
    private var remainingAtResume = 0L
    private var resumedAt = 0L
    private var tickJob: Job? = null

    fun start(durationMillis: Long) {
        require(durationMillis > 0) { "durationMillis must be positive" }
        totalMillis = durationMillis
        remainingAtResume = durationMillis
        resumedAt = elapsedRealtime()
        _state.value = TimerState.Running(totalMillis, durationMillis)
        launchTicker()
    }

    fun pause() {
        if (_state.value !is TimerState.Running) return
        stopTicker()
        val remaining = currentRemaining()
        remainingAtResume = remaining
        _state.value = TimerState.Paused(totalMillis, remaining)
    }

    fun resume() {
        val paused = _state.value as? TimerState.Paused ?: return
        remainingAtResume = paused.remainingMillis
        resumedAt = elapsedRealtime()
        _state.value = TimerState.Running(totalMillis, paused.remainingMillis)
        launchTicker()
    }

    /** 사용자가 중간에 종료. */
    fun stop() {
        when (val current = _state.value) {
            is TimerState.Running -> finish(remaining = currentRemaining())
            is TimerState.Paused -> finish(remaining = current.remainingMillis)
            else -> Unit
        }
    }

    fun reset() {
        stopTicker()
        _state.value = TimerState.Idle
    }

    /** 시계 기준으로 상태를 갱신한다. ticker 가 주기적으로 호출하며, 화면 복귀 시 즉시 호출해도 된다. */
    fun tick() {
        if (_state.value !is TimerState.Running) return
        val remaining = currentRemaining()
        if (remaining <= 0L) finish(remaining = 0L) else _state.value = TimerState.Running(totalMillis, remaining)
    }

    private fun currentRemaining(): Long =
        (remainingAtResume - (elapsedRealtime() - resumedAt)).coerceAtLeast(0L)

    private fun finish(remaining: Long) {
        stopTicker()
        _state.value = TimerState.Finished(
            totalMillis = totalMillis,
            elapsedMillis = totalMillis - remaining,
            completed = remaining <= 0L,
        )
    }

    private fun launchTicker() {
        stopTicker()
        tickJob = scope.launch {
            while (isActive) {
                delay(tickMillis)
                tick()
            }
        }
    }

    private fun stopTicker() {
        tickJob?.cancel()
        tickJob = null
    }
}
