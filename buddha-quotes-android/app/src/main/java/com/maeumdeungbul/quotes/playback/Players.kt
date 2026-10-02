package com.maeumdeungbul.quotes.playback

import android.content.ComponentName
import android.content.Context
import android.media.SoundPool
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.maeumdeungbul.quotes.R
import com.maeumdeungbul.quotes.domain.usecase.AmbientSoundPlayer
import com.maeumdeungbul.quotes.domain.usecase.BellPlayer

/** [MeditationPlaybackService] 에 MediaController 로 연결해 배경음을 반복 재생한다. 메인 스레드에서 호출한다. */
class Media3AmbientSoundPlayer(context: Context) : AmbientSoundPlayer {

    private val appContext = context.applicationContext
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var pendingAction: ((MediaController) -> Unit)? = null

    override fun play(soundId: String) = withController { controller ->
        controller.setMediaItem(MediaItem.Builder().setMediaId(soundId).build())
        controller.repeatMode = Player.REPEAT_MODE_ONE
        controller.prepare()
        controller.play()
    }

    override fun pause() {
        controller?.pause()
    }

    override fun resume() {
        controller?.play()
    }

    override fun stop() {
        pendingAction = null
        controller?.run {
            stop()
            clearMediaItems()
        }
        controllerFuture?.let(MediaController::releaseFuture)
        controllerFuture = null
        controller = null
    }

    private fun withController(action: (MediaController) -> Unit) {
        controller?.let {
            action(it)
            return
        }
        pendingAction = action
        if (controllerFuture != null) return

        val token = SessionToken(appContext, ComponentName(appContext, MeditationPlaybackService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                val connected = runCatching { future.get() }.getOrNull()
                if (connected == null || controllerFuture !== future) {
                    if (controllerFuture === future) controllerFuture = null
                    return@addListener
                }
                controller = connected
                pendingAction?.invoke(connected)
                pendingAction = null
            },
            ContextCompat.getMainExecutor(appContext),
        )
    }
}

/** 시작·종료 종소리. 짧은 효과음이라 SoundPool 로 지연 없이 재생한다. */
class SoundPoolBellPlayer(context: Context) : BellPlayer {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    @Volatile
    private var loaded = false
    private var bellSoundId = 0

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (sampleId == bellSoundId && status == 0) loaded = true
        }
        bellSoundId = soundPool.load(context.applicationContext, R.raw.bell_strike, 1)
    }

    override fun ring() {
        if (loaded) soundPool.play(bellSoundId, 0.8f, 0.8f, 1, 0, 1f)
    }
}
