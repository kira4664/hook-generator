package com.maeumdeungbul.quotes.domain.usecase

import com.maeumdeungbul.quotes.domain.model.MEDITATION_MIN_RECORD_SECONDS
import com.maeumdeungbul.quotes.domain.model.MeditationSession
import com.maeumdeungbul.quotes.domain.model.MeditationType
import com.maeumdeungbul.quotes.domain.repository.MeditationRepository
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 배경음(반복 재생). 구현: playback/Media3AmbientSoundPlayer */
interface AmbientSoundPlayer {
    fun play(soundId: String)
    fun pause()
    fun resume()
    fun stop()
}

/** 시작·종료 종소리. 구현: playback/SoundPoolBellPlayer */
fun interface BellPlayer {
    fun ring()
}

data class MeditationResult(
    val actualSeconds: Int,
    val completed: Boolean,
    /** [MEDITATION_MIN_RECORD_SECONDS] 이상이라 기록에 저장되었는지 */
    val recorded: Boolean,
)

/**
 * 진행 중인 명상 한 회차를 관리한다(타이머 + 배경음 + 종소리 + 기록 저장).
 * Application 범위에서 하나만 존재하므로 화면 회전·화면 이동·백그라운드 전환에도 상태가 유지된다.
 */
class MeditationSessionManager(
    private val scope: CoroutineScope,
    private val timer: MeditationTimer,
    private val ambientPlayer: AmbientSoundPlayer,
    private val bellPlayer: BellPlayer,
    private val repository: MeditationRepository,
    private val now: () -> Instant,
) {
    private data class Active(
        val plannedSeconds: Int,
        val soundId: String?,
        val bellEnabled: Boolean,
        val startedAt: Instant,
    )

    private var active: Active? = null

    val timerState: StateFlow<TimerState> = timer.state

    private val _result = MutableStateFlow<MeditationResult?>(null)
    /** 방금 끝난 명상의 결과. 완료 화면이 읽은 뒤 [consumeResult] 한다. */
    val result: StateFlow<MeditationResult?> = _result.asStateFlow()

    val isActive: Boolean
        get() = active != null

    init {
        scope.launch {
            timer.state.collect { state ->
                if (state is TimerState.Finished) onFinished(state)
            }
        }
    }

    fun start(minutes: Int, soundId: String?, bellEnabled: Boolean) {
        require(minutes > 0) { "minutes must be positive" }
        if (active != null) return
        _result.value = null
        active = Active(minutes * 60, soundId, bellEnabled, now())
        if (bellEnabled) bellPlayer.ring()
        soundId?.let(ambientPlayer::play)
        timer.start(minutes * 60_000L)
    }

    fun pause() {
        timer.pause()
        ambientPlayer.pause()
    }

    fun resume() {
        timer.resume()
        ambientPlayer.resume()
    }

    fun stop() = timer.stop()

    /** 화면 복귀 시 즉시 남은 시간을 다시 계산한다. */
    fun refresh() = timer.tick()

    fun consumeResult() {
        _result.value = null
    }

    private suspend fun onFinished(state: TimerState.Finished) {
        val session = active ?: return
        active = null
        ambientPlayer.stop()
        if (session.bellEnabled && state.completed) bellPlayer.ring()

        val actualSeconds = (state.elapsedMillis / 1000L).toInt()
        val recorded = actualSeconds >= MEDITATION_MIN_RECORD_SECONDS
        if (recorded) {
            repository.saveSession(
                MeditationSession(
                    startedAt = session.startedAt,
                    plannedSeconds = session.plannedSeconds,
                    actualSeconds = actualSeconds,
                    type = MeditationType.TIMER,
                    soundId = session.soundId,
                    completed = state.completed,
                ),
            )
        }
        _result.value = MeditationResult(actualSeconds, state.completed, recorded)
        timer.reset()
    }
}
