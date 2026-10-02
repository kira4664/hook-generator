package com.maeumdeungbul.quotes.playback

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.maeumdeungbul.quotes.MainActivity

/**
 * 명상 배경음 재생 서비스(Media3).
 * 재생 중에는 Media3 가 미디어 알림과 함께 포그라운드 서비스로 전환하므로,
 * 화면이 꺼지거나 앱이 백그라운드로 가도 배경음과 타이머(같은 프로세스)가 유지된다.
 */
class MeditationPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        player.repeatMode = Player.REPEAT_MODE_ONE

        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(SoundResolvingCallback(packageName))
            .setSessionActivity(openApp)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    /** 컨트롤러는 mediaId(사운드 id)만 보내고, 실제 음원 URI 는 서비스가 앱 내부 리소스로 해석한다. */
    private class SoundResolvingCallback(private val packageName: String) : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> {
            val resolved = mediaItems.mapNotNull { item ->
                val rawRes = MeditationSound.fromId(item.mediaId)?.rawRes ?: return@mapNotNull null
                item.buildUpon()
                    .setUri(Uri.parse("android.resource://$packageName/$rawRes"))
                    .build()
            }.toMutableList()
            return Futures.immediateFuture(resolved)
        }
    }
}
