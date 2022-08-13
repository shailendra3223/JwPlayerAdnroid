package com.example.jwplayer

import android.view.Window
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.OnPlayListener
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.OnPauseListener
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.OnCompleteListener
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents.OnAdPlayListener
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents.OnAdPauseListener
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents.OnAdCompleteListener
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents.OnAdSkippedListener
import com.jwplayer.pub.api.events.listeners.AdvertisingEvents.OnAdErrorListener
import android.view.WindowManager
import com.jwplayer.pub.api.events.*

/**
 * Sets the FLAG_KEEP_SCREEN_ON flag during playback - disables it when playback is stopped
 */
class KeepScreenOnHandler(player: JWPlayer, window: Window) : OnPlayListener, OnPauseListener,
    OnCompleteListener, VideoPlayerEvents.OnErrorListener, OnAdPlayListener, OnAdPauseListener,
    OnAdCompleteListener, OnAdSkippedListener, OnAdErrorListener {
    /**
     * The application window
     */
    private val mWindow: Window
    private fun updateWakeLock(enable: Boolean) {
        if (enable) {
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onError(errorEvent: ErrorEvent) {
        updateWakeLock(false)
    }

    override fun onAdPlay(adPlayEvent: AdPlayEvent) {
        updateWakeLock(true)
    }

    override fun onAdPause(adPauseEvent: AdPauseEvent) {
        updateWakeLock(false)
    }

    override fun onAdComplete(adCompleteEvent: AdCompleteEvent) {
        updateWakeLock(false)
    }

    override fun onAdSkipped(adSkippedEvent: AdSkippedEvent) {
        updateWakeLock(false)
    }

    override fun onAdError(adErrorEvent: AdErrorEvent) {
        updateWakeLock(false)
    }

    override fun onComplete(completeEvent: CompleteEvent) {
        updateWakeLock(false)
    }

    override fun onPause(pauseEvent: PauseEvent) {
        updateWakeLock(false)
    }

    override fun onPlay(playEvent: PlayEvent) {
        updateWakeLock(true)
    }

    init {
        player.addListener(EventType.PLAY, this)
        player.addListener(EventType.PAUSE, this)
        player.addListener(EventType.COMPLETE, this)
        player.addListener(EventType.ERROR, this)
        player.addListener(EventType.AD_PLAY, this)
        player.addListener(EventType.AD_PAUSE, this)
        player.addListener(EventType.AD_COMPLETE, this)
        player.addListener(EventType.AD_ERROR, this)
        mWindow = window
    }
}