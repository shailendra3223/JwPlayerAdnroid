package com.example.jwplayer

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
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


class MainActivity : AppCompatActivity() , VideoPlayerEvents.OnFullscreenListener,
View.OnTouchListener{
     var mPlayerView: JWPlayerView? = null
    private var mPlayer: JWPlayer? = null
     var mScaleGestureDetector: ScaleGestureDetector? = null
     var mScaleFactor = 1.0f
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
        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        mPlayerView!!.setOnTouchListener(this)
        mPlayerView!!.setOnClickListener {
            Log.i("fsdhgfshgfdshsd555", "onScale: ${MainActivity().mScaleFactor}")
        }
//        mPlayerView!!.onTouchEvent(MotionEvent.ACTION_MASK)
    }


    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        Log.i("fsdhgfshgfdshsd4", "onScale: ${MainActivity().mScaleFactor}")

        if (event!!.action == MotionEvent.ACTION_MASK) {
            return mScaleGestureDetector!!.onTouchEvent(event)
        }
        return false
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
            .displayTitle(true)
//            .file()
//            .stretching(PlayerConfig.STRETCHING_FILL)
            .build()
        mPlayer!!.setup(config)
        val controls = MyControls(ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Light))
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        controls.layoutParams = params
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

    class ScaleListener : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector?): Boolean {
            MainActivity().mScaleFactor *= scaleGestureDetector?.scaleFactor!!
            MainActivity().mPlayerView?.scaleX = MainActivity().mScaleFactor
            MainActivity().mPlayerView?.scaleY = MainActivity().mScaleFactor
            Log.i("fsdhgfshgfdshsd1", "onScale: ${MainActivity().mScaleFactor}")
            return  true
        }

        override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
            Log.i("fsdhgfshgfdshsd2", "onScale: ${p0?.scaleFactor}")

            return false
        }

        override fun onScaleEnd(p0: ScaleGestureDetector?) {
            Log.i("fsdhgfshgfdshsd3", "onScale: ${p0?.scaleFactor}")

            TODO("Not yet implemented")
        }

    }
}



