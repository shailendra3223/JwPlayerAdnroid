package com.google.sample.cast.refplayer

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.CastDevice
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.gson.Gson
import com.google.sample.cast.refplayer.databinding.ActivityVideoBinding
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.configuration.UiConfig
import com.jwplayer.pub.api.events.EventType
import com.jwplayer.pub.api.events.FullscreenEvent
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents
import com.jwplayer.pub.api.license.LicenseUtil
import com.jwplayer.pub.api.media.captions.Caption
import com.jwplayer.pub.api.media.captions.CaptionType
import com.jwplayer.pub.api.media.playlists.PlaylistItem

class VideoActivity : AppCompatActivity(), VideoPlayerEvents.OnFullscreenListener {

    var binding: ActivityVideoBinding? = null
    private var mPlayer: JWPlayer? = null
    private val TAG = "VideoActivity::Class"
    private var mAscpectMenuItem: MenuItem? = null
    private var customPlayerView: CustomPlayerViewModel? = null
    private val mSelectedMedia: MediaInfo? = null
    private val mControllersVisible = false
    private val mDuration = 0
    private var mCastContext: CastContext? = null
    private var mCastSession: CastSession? = null
    private var mSessionManagerListener: SessionManagerListener<CastSession>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video)

//        setupControlsCallbacks()
        setupCastListener()
        setupActionBar()

        mCastContext = CastContext.getSharedInstance(this)
        mCastSession = mCastContext!!.sessionManager.currentCastSession


        LicenseUtil().setLicenseKey(this, "OwyAxwyK8E//wkcI5SlH3MIBOqrcNP8P3YI3SKFF5zMdhshU")
        binding!!.jwplayer.getPlayerAsync(this, this) { jwPlayer: JWPlayer? ->
            mPlayer = jwPlayer
            mPlayer!!.setFullscreenHandler(ZoomHandler(binding!!.jwplayer))
            setupPlayer1()
        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    var positionSeason = 0
    var positionEpisode = 0
    private fun setupPlayer1() {

        // Handle hiding/showing of ActionBar
        mPlayer!!.addListener(EventType.FULLSCREEN, this@VideoActivity)
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
//                    .displayAllControls()
                    .hideAllControls()
                    .build()
            )
            .stretching(PlayerConfig.STRETCHING_UNIFORM)
            .build()

        mPlayer!!.setup(playerConfig)
        customPlayerView = CustomPlayerViewModel(mPlayer!!)

        bindSettingPan(data)
    }


    private fun bindSettingPan(data: ModelClass) {


//        binding!!.jwplayer.touc
/*
        mCastContext = castContext;
//        CastButtonFactory.setUpMediaRouteButton(this, mediaRouteButton!!)
        mCastContext!!.addCastStateListener(this)
        mCastContext!!.addAppVisibilityListener(this)
        mCastSession = mCastContext!!.sessionManager.currentCastSession
        mCastContext!!.sessionManager.addSessionManagerListener(this, CastSession::class.java)*/

//        cast = customPlayerView!!.player.getViewModelForUiGroup(UiGroup.CASTING_MENU) as CastingMenuViewModel

        val mEpisode = data[0].seasons[positionSeason].episodes[positionEpisode]
//        val jsonObj: JSONObject = VideoProvider().parseUrl(mEpisode.media)
//        val jsonObj = VideoProvider.buildMedia(mEpisode.media) as JSONObject


        customPlayerView!!.isFirstFrame.observe(this) { mJWPlayer: JWPlayer ->
            binding!!.tvTitle.text = mJWPlayer.playlistItem.mTitle
        }

        customPlayerView!!.printTime.observe(this) {
            binding!!.tvTime.text = it
        }

        customPlayerView!!.isVisibility.observe(this) {
            sec3Timer(customPlayerView!!)
        }

        binding!!.tvVideoSetting.setOnClickListener { v: View? ->
            sec3Timer(customPlayerView!!)
//            customPlayerView!!.isVisibility.value = false
//            customPlayerView!!.disableTouch.value = true
//            customPlayerView!!.player.pause()
            visibilityComponents(ConstraintLayout.GONE)
//            AlertDialogPlayer(customPlayerView!!, 1001)
        }

        binding!!.tvSubtitle.setOnClickListener { v: View? ->
            sec3Timer(customPlayerView!!)
//            customPlayerView!!.isVisibility.value = false
//            customPlayerView!!.disableTouch.value = true
//            customPlayerView!!.player.pause()
            visibilityComponents(ConstraintLayout.GONE)
//            AlertDialogPlayer(customPlayerView!!, 1003)
        }


        binding!!.ivZoomInOut.setOnClickListener {
            sec3Timer(customPlayerView!!)
            val widthDp = resources.displayMetrics.run { widthPixels / density }
            val heightDp = resources.displayMetrics.run { heightPixels / density }

            if (customPlayerView!!.player.fullscreen) {
                binding!!.ivZoomInOut.setImageDrawable(
                    AppCompatResources.getDrawable(
                        this,
                        R.drawable.ic_aspect_ratio
                    )
                )
//                binding!!.tvVideoSetting.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                    setMargins(dpFormat(12), dpFormat(12), dpFormat(12), dpFormat(12))
//                }
            } else {
                binding!!.ivZoomInOut!!.setImageDrawable(
                    AppCompatResources.getDrawable(
                        this,
                        R.drawable.ic_full_screen
                    )
                )
            }
            customPlayerView!!.toggleFullscreen()
        }

        binding!!.tvEpisode.setOnClickListener {
            sec3Timer(customPlayerView!!)
            visibilityComponents(ConstraintLayout.GONE)
//            customPlayerView!!.isVisibility.value = false
//            customPlayerView!!.disableTouch.value = true
//            AlertDialogPlayList(customPlayerView!!, data[positionSeason].seasons, playerConfig)
        }

//        mediaRouteButton!!.setOnClickListener {
//            CastButtonFactory.setUpMediaRouteButton(context, mediaRouteButton!!)
//        }

        binding!!.ivChromeCast!!.setOnClickListener {

            //CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
            sec3Timer(customPlayerView!!)
            customPlayerView!!.isVisibility.value = false
            customPlayerView!!.disableTouch.value = true

            val router: MediaRouter = MediaRouter.getInstance(this)
            val mRoutes: List<MediaRouter.RouteInfo> = router.routes
            val isCastRoutes = ArrayList<MediaRouter.RouteInfo>()
            val devices = ArrayList<CastDevice>()

            for (routeInfo in mRoutes) {
                val device = CastDevice.getFromBundle(routeInfo.extras)
                if (device != null) {
                    devices.add(device)
                    isCastRoutes.add(routeInfo)
                }
            }

//            if (mCastSession != null) {
//                if (mCastSession!!.isConnected) {
//                    Log.i(TAG, "bindSettingPan1: ${mCastSession!!.castDevice!!.friendlyName}")
//                    Log.i(TAG, "bindSettingPan: ${mCastContext!!.sessionManager.currentSession!!.isConnected}")
//                    AalertDialogDisconnectCast(cast!!, mCastSession!!)
//                } else {
//                    remoteMediaClient = mCastSession!!.remoteMediaClient
//
//                    val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
//
//                    movieMetadata.putString(MediaMetadata.KEY_TITLE, mEpisode.title)
////                  movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mEpisode.)
//                    movieMetadata.addImage(WebImage(Uri.parse(mEpisode.thumbnail_url)))
////                  movieMetadata.addImage(WebImage(Uri.parse(mEpisode.getImage(1))))
//
//                    mSelectedMedia = MediaInfo.Builder(mEpisode.media)
//                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
//                        .setContentType("videos/mp4")
//                        .setMetadata(movieMetadata)
//                        .setStreamDuration((customPlayerView!!.player.duration * 1000).toLong())
//                        .build()
//
////                    remoteMediaClient!!.load(MediaLoadRequestData.Builder().setMediaInfo(mSelectedMedia).build())
//
//                    loadRemoteMedia(0, true)
//                    AlertDialogCast(cast!!, isCastRoutes)
//                }
//
//            } else {
//                mCastContext = CastContext.getSharedInstance(context)
//                CastButtonFactory.setUpMediaRouteButton(context!!, mediaRouteButton!!)
//                mCastContext!!.addCastStateListener(this)
//                mCastContext!!.addAppVisibilityListener(this)
//                mCastSession = mCastContext!!.sessionManager.currentCastSession
//                mCastContext!!.sessionManager.addSessionManagerListener(this, CastSession::class.java)
//                AlertDialogCast(cast!!, isCastRoutes)
//            }
        }

        binding!!.tvNextEpisode!!.setOnClickListener {
            sec3Timer(customPlayerView!!)
            val next = customPlayerView!!.player.playlistIndex + 1
            if (next != customPlayerView!!.player.playlist.size) {
                positionEpisode = next
                customPlayerView!!.player.playlistItem(next)
            } else {
//                Toast.makeText(this, "Last Video", Toast.LENGTH_LONG).show()
            }
        }

//        binding!!.ivFastForward15!!.setOnClickListener {
//            sec3Timer(customPlayerView!!)
//            val position = customPlayerView!!.player.position + 30
//
//            if (position >= customPlayerView!!.player.duration) {
//                val next = customPlayerView!!.player.playlistIndex + 1
//                if (next != customPlayerView!!.player.playlist.size) {
//                    customPlayerView!!.player.playlistItem(next)
//                }
//            } else if (position >= 0) {
//                customPlayerView!!.player.seek(position)
//                customPlayerView!!.handleTimeUpdate(
//                    position,
//                    customPlayerView!!.player.duration,
//                    customPlayerView!!.contentProgressPercentage
//                )
//            }
//        }

//        binding!!.ivFastBackward15!!.setOnClickListener {
//            sec3Timer(customPlayerView!!)
//            val position = customPlayerView!!.player.position - 30
//            if (position > 31) {
//                customPlayerView!!.player.seek(position)
//                customPlayerView!!.handleTimeUpdate(
//                    position,
//                    customPlayerView!!.player.duration,
//                    customPlayerView!!.contentProgressPercentage
//                )
//            } else {
//                customPlayerView!!.player.seek(0.1)
//                customPlayerView!!.handleTimeUpdate(
//                    0.1,
//                    customPlayerView.player.duration,
//                    customPlayerView.contentProgressPercentage
//                )
//            }
//        }


        //SeekBar
        customPlayerView!!.contentProgressPercentage.observe(this) { progress ->
            binding!!.seekbar.progress = progress
        }

        customPlayerView!!.isSeekbarVisible.observe(this) { isVisible ->
            binding!!.seekbar.visibility =
                if (isVisible) ConstraintLayout.VISIBLE else ConstraintLayout.GONE
            binding!!.tvTime.visibility =
                if (isVisible) ConstraintLayout.VISIBLE else ConstraintLayout.GONE
        }
        binding!!.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    customPlayerView!!.seek(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        customPlayerView!!.isPlayToggleVisible.observe(this) { isVisible ->
            binding!!.playPauseToggle.visibility =
                if (isVisible) ConstraintLayout.VISIBLE else ConstraintLayout.GONE
        }
        customPlayerView!!.isPlayIcon.observe(this) { isPlay ->
            binding!!.playPauseToggle.setImageDrawable(
                if (isPlay) AppCompatResources
                    .getDrawable(this, R.drawable.ic_jw_play) else AppCompatResources
                    .getDrawable(this, R.drawable.ic_jw_pause)
            )
        }
        binding!!.playPauseToggle.setOnClickListener { v: View? ->
            sec3Timer(customPlayerView!!)
            customPlayerView!!.togglePlay()
        }

//        customPlayerView.isFullscreen.observe(this) { isFullscreen ->
//            fullscreenToggle!!.setImageDrawable(
//                if (isFullscreen) AppCompatResources.getDrawable(
//                    context,
//                    R.drawable.ic_jw_exit_fullscreen
//                )
//                else AppCompatResources.getDrawable(context, R.drawable.ic_jw_enter_fullscreen)
//            )
//        }
//        fullscreenToggle!!.setOnClickListener(OnClickListener { v: View? -> customPlayerView.toggleFullscreen() })
    }

    var countDown: CountDownTimer? = null
    var VisibilityTime = 2000
    private fun sec3Timer(customPlayerView: CustomPlayerViewModel) {
        visibilityComponents(ConstraintLayout.VISIBLE)
        countDown?.cancel()
        countDown = object : CountDownTimer(VisibilityTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "Timer " + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                visibilityComponents(ConstraintLayout.GONE)
                customPlayerView.disableTouch.value = false
            }
        }.start()
    }

    private fun visibilityComponents(isVisible: Int) {
        binding!!.tvVideoSetting.visibility = isVisible
        binding!!.tvEpisode.visibility = isVisible
        binding!!.tvSubtitle.visibility = isVisible
        binding!!.tvTitle.visibility = isVisible
        binding!!.seekbar.visibility = isVisible
        binding!!.tvTime.visibility = isVisible
        binding!!.playPauseToggle.visibility = isVisible
//        binding!!.fullscreenToggle.visibility = isVisible
        binding!!.ivZoomInOut.visibility = isVisible
        binding!!.ivChromeCast.visibility = isVisible
        binding!!.tvNextEpisode.visibility = isVisible
        binding!!.tvFastBackward15.visibility = isVisible
        binding!!.tvFastForward15.visibility = isVisible
    }

    override fun onFullscreen(fullscreenEvent: FullscreenEvent) {
        val actionBar = supportActionBar
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.expanded_controller, menu)
        CastButtonFactory.setUpMediaRouteButton(
            applicationContext,
            menu,
            R.id.media_route_menu_item
        )
//        mAscpectMenuItem = menu.findItem(R.id.aspect_ratio_menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setupActionBar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
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
//                        loadRemoteMedia(mSeekbar.getProgress(), true)
//                        return
//                    } else {
//                        mPlaybackState = PlaybackState.IDLE
//                        updatePlaybackLocation(PlaybackLocation.REMOTE)
//                    }
                }
//                updatePlayButton(mPlaybackState)
                invalidateOptionsMenu()
            }

            private fun onApplicationDisconnected() {
//                updatePlaybackLocation(PlaybackLocation.LOCAL)
//                mPlaybackState = PlaybackState.IDLE
//                mLocation =
//                    PlaybackLocation.LOCAL
//                updatePlayButton(mPlaybackState)
                invalidateOptionsMenu()
            }
        }
    }
}