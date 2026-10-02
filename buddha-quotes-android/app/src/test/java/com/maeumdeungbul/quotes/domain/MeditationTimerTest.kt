package com.maeumdeungbul.quotes.domain

import com.maeumdeungbul.quotes.domain.usecase.MeditationTimer
import com.maeumdeungbul.quotes.domain.usecase.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MeditationTimerTest {

    private var clock = 1_000L
    // ticker 는 실제로 돌지 않도록 아주 긴 주기를 주고, tick() 을 직접 호출해 시간을 진행한다.
    private val scope = CoroutineScope(Job())
    private val timer = MeditationTimer(scope, elapsedRealtime = { clock }, tickMillis = Long.MAX_VALUE / 4)

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun countsDownByClock() {
        timer.start(60_000)
        clock += 15_000
        timer.tick()
        assertEquals(TimerState.Running(60_000, 45_000), timer.state.value)
    }

    @Test
    fun pauseFreezesRemainingTime() {
        timer.start(60_000)
        clock += 10_000
        timer.pause()
        clock += 100_000
        timer.tick()
        assertEquals(TimerState.Paused(60_000, 50_000), timer.state.value)
        timer.resume()
        clock += 20_000
        timer.tick()
        assertEquals(TimerState.Running(60_000, 30_000), timer.state.value)
    }

    @Test
    fun finishesWhenTimeIsUp() {
        timer.start(60_000)
        clock += 61_000
        timer.tick()
        assertEquals(TimerState.Finished(60_000, 60_000, completed = true), timer.state.value)
    }

    @Test
    fun stopEarly_reportsElapsedAndNotCompleted() {
        timer.start(600_000)
        clock += 125_000
        timer.stop()
        val state = timer.state.value
        assertTrue(state is TimerState.Finished)
        state as TimerState.Finished
        assertEquals(125_000L, state.elapsedMillis)
        assertEquals(false, state.completed)
    }

    @Test
    fun resetReturnsToIdle() {
        timer.start(60_000)
        timer.reset()
        assertEquals(TimerState.Idle, timer.state.value)
    }
}
