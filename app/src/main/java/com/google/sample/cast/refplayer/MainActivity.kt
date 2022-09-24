package com.google.sample.cast.refplayer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.images.WebImage
import com.google.gson.Gson
import com.google.sample.cast.refplayer.cast.ExpandedControlsActivity
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
    CustomPlayerView.OnMainScreenVisibilityListener {
    var mPlayerView: JWPlayerView? = null
    private var mPlayer: JWPlayer? = null
    private var toolbar: Toolbar? = null

    var mScaleFactor = 1.0f
    private val GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD = "com.google.market"
    private val GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW = "com.android.vending"
    private val TAG = "MainActivity::Class"
    private var controls:MyControlPan? = null

    private var mSessionManagerListener: SessionManagerListener<CastSession>? = null
    private var mCastContext: CastContext? = null
    private var mCastSession: CastSession? = null

    var mSelectedMedia: MediaInfo? = null
    var remoteMediaClient: RemoteMediaClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main)

//

//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;

        val metrics = resources.displayMetrics
        val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
        setupActionBar()
        setupCastListener()

        WebView.setWebContentsDebuggingEnabled(true)
        // TODO: Add your license key
        LicenseUtil().setLicenseKey(this, "OwyAxwyK8E//wkcI5SlH3MIBOqrcNP8P3YI3SKFF5zMdhshU")

        mPlayerView = findViewById(R.id.jwplayer)
        mPlayerView?.getPlayerAsync(this, this,
            JWPlayer.PlayerInitializationListener { jwPlayer: JWPlayer? ->
                mPlayer = jwPlayer
                mPlayer!!.setFullscreenHandler(ZoomHandler(mPlayerView!!))
                setupPlayer1()
            })

        val widthDp = resources.displayMetrics.run { widthPixels / density }
        val heightDp = resources.displayMetrics.run { heightPixels / density }

        val halfHeightDp = (heightDp/2).toInt()
        var marginTop = halfHeightDp - 100

        if (marginTop <= 0) {
            marginTop = (heightDp/2).toInt()
        }

        Log.i(TAG, "onZoomUpdate5i: ${widthDp}  ${heightDp} ${halfHeightDp}  ${marginTop} ")

//        if (isGoogleApiAvailable(this)) {
        mCastContext = CastContext.getSharedInstance(applicationContext)
        mCastSession = mCastContext!!.sessionManager.currentCastSession
//        }

//        window.decorView.apply {
//            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
//        }
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    override fun onResume() {
        Log.d(TAG, "onResume() was called")
        mCastContext!!.sessionManager.addSessionManagerListener(
            mSessionManagerListener!!, CastSession::class.java
        )
//        if (mCastSession != null && mCastSession!!.isConnected) {
//            updatePlaybackLocation(REMOTE)
//        } else {
//            updatePlaybackLocation(LOCAL)
//        }
//        if (mQueueMenuItem != null) {
//            mQueueMenuItem.setVisible(
//                mCastSession != null && mCastSession!!.isConnected
//            )
//        }
        super.onResume()
    }


    var positionSeason = 0
    var positionEpisode = 0
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

        var playerConfig = PlayerConfig.Builder()
            .playlist(playlist)
            .autostart(true)
            .uiConfig(
                UiConfig.Builder()
                    .displayAllControls()
                    .hide(UiGroup.CASTING_MENU)
                    .hide(UiGroup.CONTROLBAR)
                    .hide(UiGroup.CENTER_CONTROLS)
                    .hide(UiGroup.SETTINGS_PLAYBACK_SUBMENU)
                    .hide(UiGroup.SETTINGS_QUALITY_SUBMENU)
                    .hide(UiGroup.SETTINGS_AUDIOTRACKS_SUBMENU)
                    .show(UiGroup.PLAYLIST)
                    .build()
            )
//            .stretching(PlayerConfig.STRETCHING_UNIFORM)
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

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        controls = MyControlPan(ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_ActionBar))
        controls!!.layoutParams = params
        mPlayerView!!.addView(controls!!)
        controls!!.bindSettingPan(mPlayer!!, playerConfig!!, this, data, mPlayerView!!, mCastContext!!, this)

        mPlayer!!.setFullscreen(true, true)
//        val controls = MyControls(ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Light))
//        val params = FrameLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        )
//        controls.layoutParams = params
//        mPlayerView!!.addView(controls)
//        controls.bind(mPlayer!!, this)

        val mEpisode = data[0].seasons[positionSeason].episodes[positionEpisode]

        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mEpisode.title)
//      movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mEpisode.)
        movieMetadata.addImage(WebImage(Uri.parse(mEpisode.thumbnail_url)))
//      movieMetadata.addImage(WebImage(Uri.parse(mEpisode.getImage(1))))

        mSelectedMedia = MediaInfo.Builder(mEpisode.media)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("videos/mp4")
            .setMetadata(movieMetadata)
            .setStreamDuration((mPlayer!!.duration * 1000).toLong())
            .build()

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
       /* if (actionBar != null) {
//            val isCasting = if (mCastContext != null) mCastContext!!
//                .castState == CastState.CONNECTED else false
            if (fullscreenEvent.fullscreen) {
                actionBar!!.hide()
            } else {
                actionBar.show()
            }
        }*/
    }

   /* private fun setupPlayer() {
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
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.expanded_controller, menu)
        CastButtonFactory.setUpMediaRouteButton(
            applicationContext, menu,
            R.id.media_route_menu_item
        )
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setupActionBar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.aspect_ratio_menu -> {
                val isFull = mPlayer!!.fullscreen
                if (isFull) {
                    item.setIcon(R.drawable.ic_aspect_ratio)
                } else {
                    item.setIcon(R.drawable.ic_full_screen)
                }

                controls!!.onZoomUpdatePan(isFull)
                mPlayer!!.setFullscreen(!isFull, true)
            }
        }
        return true
    }


    private fun setupCastListener() {
        mSessionManagerListener = object : SessionManagerListener<CastSession> {
            override fun onSessionEnded(session: CastSession, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
                onApplicationConnected(session)
            }

            override fun onSessionResumeFailed(session: CastSession, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionStarted(session: CastSession, sessionId: String) {
                onApplicationConnected(session)
            }

            override fun onSessionStartFailed(session: CastSession, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionStarting(session: CastSession) {}
            override fun onSessionEnding(session: CastSession) {}
            override fun onSessionResuming(session: CastSession, sessionId: String) {}
            override fun onSessionSuspended(session: CastSession, reason: Int) {}
            private fun onApplicationConnected(castSession: CastSession) {
                mCastSession = castSession
                if (null != mSelectedMedia) {
//                    if (mPlaybackState == PlaybackState.PLAYING) {
//                        mVideoView.pause()
                        loadRemoteMedia(0, true) //mSeekbar.getProgress()
//                        return
//                    } else {
//                        mPlaybackState = PlaybackState.IDLE
//                        updatePlaybackLocation(REMOTE)
//                    }
                }
//                updatePlayButton(mPlaybackState)
                    invalidateOptionsMenu()

            }

            private fun onApplicationDisconnected() {
//                updatePlaybackLocation(LOCAL)
//                mPlaybackState = PlaybackState.IDLE
//                mLocation = LOCAL
//                updatePlayButton(mPlaybackState)
                invalidateOptionsMenu()
            }
        }
    }

    private fun loadRemoteMedia(position: Int, autoPlay: Boolean) {
        Log.d("TAG", "Playing GOLD...")
        if (mCastSession == null) {
            return
        }
        val remoteMediaClient = mCastSession!!.remoteMediaClient ?: return
        remoteMediaClient.registerCallback(object : RemoteMediaClient.Callback() {
            override fun onStatusUpdated() {
                val intent = Intent(this@MainActivity, ExpandedControlsActivity::class.java)
                startActivity(intent)
                remoteMediaClient.unregisterCallback(this)
            }
        })
        remoteMediaClient.load(
            MediaLoadRequestData.Builder()
                .setMediaInfo(mSelectedMedia)
                .setAutoplay(autoPlay)
                .setCurrentTime(position.toLong()).build()
        )
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() was called")
//        if (mLocation == LOCAL) {
//            if (mSeekbarTimer != null) {
//                mSeekbarTimer.cancel()
//                mSeekbarTimer = null
//            }
//            if (mControllersTimer != null) {
//                mControllersTimer.cancel()
//            }
//            // since we are playing locally, we need to stop the playback of
//            // video (if user is not watching, pause it!)
//            mVideoView.pause()
//            mPlaybackState = PlaybackState.PAUSED
//            updatePlayButton(PlaybackState.PAUSED)
//        }
        mCastContext!!.sessionManager.removeSessionManagerListener(
            mSessionManagerListener!!, CastSession::class.java
        )
    }

    override fun onVisible(isVisible: Boolean) {
        if (isVisible) toolbar!!.visibility = View.VISIBLE else toolbar!!.visibility = View.GONE
    }

    override fun onZoom(isVisible: Boolean) {
//        TODO("Not yet implemented")
    }

    override fun onToolInfo(title: String) {
        toolbar!!.title = title
    }
}



