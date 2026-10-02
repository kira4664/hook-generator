package com.maeumdeungbul.quotes.domain

import com.maeumdeungbul.quotes.domain.model.MeditationSession
import com.maeumdeungbul.quotes.domain.model.MeditationType
import com.maeumdeungbul.quotes.domain.repository.MeditationRepository
import com.maeumdeungbul.quotes.domain.usecase.AmbientSoundPlayer
import com.maeumdeungbul.quotes.domain.usecase.MeditationResult
import com.maeumdeungbul.quotes.domain.usecase.MeditationSessionManager
import com.maeumdeungbul.quotes.domain.usecase.MeditationTimer
import com.maeumdeungbul.quotes.domain.usecase.TimerState
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MeditationSessionManagerTest {

    private var clock = 0L
    private val scope = CoroutineScope(Dispatchers.Unconfined + Job())
    private val timer = MeditationTimer(scope, elapsedRealtime = { clock }, tickMillis = Long.MAX_VALUE / 4)
    private val ambient = FakeAmbient()
    private var bellCount = 0
    private val repository = FakeRepository()
    private val manager = MeditationSessionManager(
        scope = scope,
        timer = timer,
        ambientPlayer = ambient,
        bellPlayer = { bellCount++ },
        repository = repository,
        now = { Instant.parse("2026-10-02T07:00:00Z") },
    )

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun completedSession_isRecordedWithBellAndSound() {
        manager.start(minutes = 10, soundId = "bell", bellEnabled = true)
        assertEquals(listOf("play:bell"), ambient.calls)
        assertEquals(1, bellCount)

        clock += 10 * 60_000L + 500
        manager.refresh()

        assertEquals(MeditationResult(actualSeconds = 600, completed = true, recorded = true), manager.result.value)
        assertEquals(2, bellCount) // 시작 + 종료
        assertEquals(listOf("play:bell", "stop"), ambient.calls)
        assertEquals(1, repository.saved.size)
        val saved = repository.saved.single()
        assertEquals(600, saved.actualSeconds)
        assertEquals(MeditationType.TIMER, saved.type)
        assertTrue(saved.completed)
        assertEquals(TimerState.Idle, manager.timerState.value)
        assertTrue(!manager.isActive)
    }

    @Test
    fun shortSession_isNotRecorded() {
        manager.start(minutes = 5, soundId = null, bellEnabled = false)
        clock += 30_000
        manager.stop()

        assertEquals(MeditationResult(actualSeconds = 30, completed = false, recorded = false), manager.result.value)
        assertTrue(repository.saved.isEmpty())
        assertEquals(0, bellCount)
    }

    @Test
    fun earlyStopAfterAMinute_isRecordedAsIncomplete() {
        manager.start(minutes = 10, soundId = null, bellEnabled = true)
        clock += 125_000
        manager.pause()
        assertEquals(listOf("pause"), ambient.calls)
        manager.stop()

        val saved = repository.saved.single()
        assertEquals(125, saved.actualSeconds)
        assertEquals(false, saved.completed)
        assertEquals(1, bellCount) // 중간 종료에는 종료 종을 울리지 않는다
    }

    @Test
    fun consumeResult_clearsIt() {
        manager.start(minutes = 1, soundId = null, bellEnabled = false)
        clock += 61_000
        manager.refresh()
        manager.consumeResult()
        assertEquals(null, manager.result.value)
    }

    private class FakeAmbient : AmbientSoundPlayer {
        val calls = mutableListOf<String>()
        override fun play(soundId: String) { calls += "play:$soundId" }
        override fun pause() { calls += "pause" }
        override fun resume() { calls += "resume" }
        override fun stop() { calls += "stop" }
    }

    private class FakeRepository : MeditationRepository {
        val saved = mutableListOf<MeditationSession>()
        override fun observeSessions(): Flow<List<MeditationSession>> = flowOf(saved)
        override suspend fun saveSession(session: MeditationSession): Long {
            saved += session
            return saved.size.toLong()
        }
        override suspend fun clearSessions() = saved.clear()
    }
}
