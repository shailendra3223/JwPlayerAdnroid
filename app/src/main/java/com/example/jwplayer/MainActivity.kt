package com.example.jwplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.configuration.UiConfig
import com.jwplayer.pub.api.events.EventType
import com.jwplayer.pub.api.events.FullscreenEvent
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents
import com.jwplayer.pub.api.license.LicenseUtil
import com.jwplayer.pub.view.JWPlayerView

class MainActivity : AppCompatActivity() , VideoPlayerEvents.OnFullscreenListener {
    private var mPlayerView: JWPlayerView? = null
    private var mPlayer: JWPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WebView.setWebContentsDebuggingEnabled(true)
        // TODO: Add your license key
        LicenseUtil().setLicenseKey(this, "OwyAxwyK8E//wkcI5SlH3MIBOqrcNP8P3YI3SKFF5zMdhshU")
        mPlayerView = findViewById(R.id.jwplayer)
        mPlayerView?.getPlayerAsync(this, this,
            JWPlayer.PlayerInitializationListener { jwPlayer: JWPlayer? ->
                mPlayer = jwPlayer
                setupPlayer()
            })
    }

    private fun setupPlayer() {
        // Handle hiding/showing of ActionBar
        mPlayer!!.addListener(EventType.FULLSCREEN, this@MainActivity)

        // Keep the screen on during playback
        KeepScreenOnHandler(mPlayer!!, window)

        // Load a media source
        val config = PlayerConfig.Builder()
            .playlistUrl("https://cdn.jwplayer.com/v2/playlists/3jBCQ2MI?format=json")
            .uiConfig(
                UiConfig.Builder()
                    .displayAllControls()
                    .hide(UiGroup.NEXT_UP)
                    .build()
            )
            .build()
        // Call setup before binding the ViewModels because setup updates the ViewModels
        mPlayer!!.setup(config)

        // We create a MyControls ViewGroup in which we can control the positioning of the Views
        val controls = MyControls(ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Light))
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        controls.setLayoutParams(params)
        mPlayerView!!.addView(controls)
        controls.bind(mPlayer!!, this)
    }

    override fun onFullscreen(fullscreenEvent: FullscreenEvent) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            if (fullscreenEvent.fullscreen) {
                actionBar.hide()
            } else {
                actionBar.show()
            }
        }
    }
}