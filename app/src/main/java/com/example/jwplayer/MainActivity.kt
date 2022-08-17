package com.example.jwplayer

import android.content.Context
import android.content.pm.PackageManager
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
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.configuration.UiConfig
import com.jwplayer.pub.api.events.EventType
import com.jwplayer.pub.api.events.FullscreenEvent
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents
import com.jwplayer.pub.api.license.LicenseUtil
import com.jwplayer.pub.api.media.captions.Caption
import com.jwplayer.pub.api.media.captions.CaptionType
import com.jwplayer.pub.api.media.playlists.PlaylistItem
import com.jwplayer.pub.view.JWPlayerView


class MainActivity : AppCompatActivity(), VideoPlayerEvents.OnFullscreenListener,
    View.OnTouchListener {
    var mPlayerView: JWPlayerView? = null
    private var mPlayer: JWPlayer? = null
    var mScaleGestureDetector: ScaleGestureDetector? = null
    var mScaleFactor = 1.0f
    private var mCastContext: CastContext? = null
    private val GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD = "com.google.market"
    private val GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW = "com.android.vending"

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
                mPlayer!!.setFullscreenHandler(FullScreenHandlerNoRotation(mPlayerView!!))
                setupPlayer1()
            })
        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        mPlayerView!!.setOnTouchListener(this)
        mPlayerView!!.setOnClickListener {
            Log.i("fsdhgfshgfdshsd555", "onScale: ${MainActivity().mScaleFactor}")
        }
        Log.i("fsdhgfshgfdshsd565", "onScale: ${isGoogleApiAvailable(this)}")

        if (isGoogleApiAvailable(this)) {
            mCastContext = CastContext.getSharedInstance(applicationContext)
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

    private fun setupPlayer1() {
        // Handle hiding/showing of ActionBar
        mPlayer!!.addListener(EventType.FULLSCREEN, this@MainActivity)
//        mPlayer!!.setPlaylistItemCallbackListener { playlistItemDecision, playlistItem, i ->
//            Log.i("TAGvv", "setupPlayer9: ${ i}")
//        }

        // Keep the screen on during playback
        KeepScreenOnHandler(mPlayer!!, window)

        val data = Gson().fromJson(AppConstant.data, ModelClass::class.java) as ModelClass
        val playlist: MutableList<PlaylistItem> = ArrayList()
        for (episode in data[0].episodes) {
            Log.i("TAGvv", "setupPlayer1: ${episode.media}")
            val caption = Caption.Builder()
                .file("file:///android_asset/press-play-captions.vtt")
                .kind(CaptionType.CAPTIONS)
                .label("en")
                .isDefault(true)
                .build()
            val captionList: MutableList<Caption> = ArrayList()
            captionList.add(caption)

            val pi = PlaylistItem.Builder()
                .description(episode.description)
                .file(episode.media)
                .image(episode.original_thumbnail_file)
                .tracks(captionList)
                .title(episode.title)
                .build()

            playlist.add(pi)
        }


        val playerConfig = PlayerConfig.Builder()
            .playlist(playlist)
            .uiConfig(UiConfig.Builder()

                .displayAllControls()
                .hide(UiGroup.SETTINGS_MENU)
//                .show(UiGroup.SETTINGS_AUDIOTRACKS_SUBMENU)
                .hide(UiGroup.SETTINGS_PLAYBACK_SUBMENU)
                .hide(UiGroup.SETTINGS_QUALITY_SUBMENU)
                .build())

            .build()

        mPlayer!!.setup(playerConfig)

        // Load a media source
        /*val config = PlayerConfig.Builder()
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
        mPlayer!!.setup(config)*/
        val controls = MyControlPan(ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_ActionBar))
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        controls.layoutParams = params
        mPlayerView!!.addView(controls)
        controls.bindSettingPan(mPlayer!!, UiGroup.SETTINGS_MENU,this)

//        val controls = MyControls(ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Light))
//        val params = FrameLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        )
//        controls.layoutParams = params
//        mPlayerView!!.addView(controls)
//        controls.bind(mPlayer!!, this)
    }

    class ScaleListener : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector?): Boolean {
            MainActivity().mScaleFactor *= scaleGestureDetector?.scaleFactor!!
            MainActivity().mPlayerView?.scaleX = MainActivity().mScaleFactor
            MainActivity().mPlayerView?.scaleY = MainActivity().mScaleFactor
            Log.i("fsdhgfshgfdshsd1", "onScale: ${MainActivity().mScaleFactor}")
            return true
        }

        override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
            Log.i("fsdhgfshgfdshsd2", "onScale: ${p0?.scaleFactor}")

            return false
        }

        override fun onScaleEnd(p0: ScaleGestureDetector?) {
            Log.i("fsdhgfshgfdshsd3", "onScale: ${p0?.scaleFactor}")

//            TODO("Not yet implemented")
        }

    }

    // Without the Google API's Chromecast won't work
    private fun isGoogleApiAvailable(context: Context): Boolean {
        val isOldPlayStoreInstalled: Boolean = doesPackageExist(GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD)
        val isNewPlayStoreInstalled: Boolean = doesPackageExist(GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW)
        val isPlaystoreInstalled = isNewPlayStoreInstalled || isOldPlayStoreInstalled
        val isGoogleApiAvailable = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        return isPlaystoreInstalled && isGoogleApiAvailable
    }

    private fun doesPackageExist(targetPackage: String): Boolean {
        try {
            packageManager.getPackageInfo(targetPackage, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    override fun onFullscreen(fullscreenEvent: FullscreenEvent) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            val isCasting = if (mCastContext != null) mCastContext!!
                .castState == CastState.CONNECTED else false
            if (fullscreenEvent.fullscreen && !isCasting) {
                actionBar.hide()
            } else {
                actionBar.show()
            }
        }
    }

    private fun setupPlayer() {
        // Handle hiding/showing of ActionBar
        mPlayer!!.addListener(EventType.FULLSCREEN, this)

        // Keep the screen on during playback

        // Keep the screen on during playback
        KeepScreenOnHandler(mPlayer!!, window)

        // Load a media source

        // Load a media source
        val pi = PlaylistItem.Builder()
            .file("https://d3rlna7iyyu8wu.cloudfront.net/skip_armstrong/skip_armstrong_multi_language_subs.m3u8")
            .title("Press Play")
            .image("https://content.jwplatform.com/thumbs/1sc0kL2N.jpg")
            .description("Press play with JW Player")
            .build()

        val playlist = ArrayList<PlaylistItem>()
        playlist.add(pi)

        val playerConfig = PlayerConfig.Builder()
            .playlist(playlist)
            .build()

        mPlayer!!.setup(playerConfig)
        val controls = MyControls(ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Light))
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        controls.layoutParams = params
        mPlayerView!!.addView(controls)
        controls.bind(mPlayer!!, this)
    }

    private fun setupPlayer2() {
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
}



