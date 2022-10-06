package com.google.sample.cast.refplayer.bugs

import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.*
import com.google.android.gms.cast.MediaStatus.REPEAT_MODE_REPEAT_OFF
import com.google.android.gms.cast.framework.*
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.images.WebImage
import com.google.gson.Gson
import com.google.sample.cast.refplayer.*
import com.google.sample.cast.refplayer.R
import com.google.sample.cast.refplayer.adapter.CastSelectAdapter
import com.google.sample.cast.refplayer.adapter.PlayListSeasonAdapter
import com.google.sample.cast.refplayer.adapter.SelectAdapter
import com.google.sample.cast.refplayer.databinding.ActivityMainBugsBinding
import com.google.sample.cast.refplayer.databinding.DialogCastBinding
import com.google.sample.cast.refplayer.databinding.DialogPlayrateSubtitleBinding
import com.google.sample.cast.refplayer.databinding.FragmentPlayListSeasonBinding
import com.google.sample.cast.refplayer.model.SelectItem
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.configuration.UiConfig
import com.jwplayer.pub.api.events.EventType
import com.jwplayer.pub.api.events.FullscreenEvent
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents
import com.jwplayer.pub.api.license.LicenseUtil
import com.jwplayer.pub.api.media.adaptive.QualityLevel
import com.jwplayer.pub.api.media.audio.AudioTrack
import com.jwplayer.pub.api.media.captions.Caption
import com.jwplayer.pub.api.media.captions.CaptionType
import com.jwplayer.pub.api.media.playlists.PlaylistItem
import com.jwplayer.pub.ui.viewmodels.CastingMenuViewModel
import org.json.JSONObject

class MainBugsActivity : AppCompatActivity(), VideoPlayerEvents.OnFullscreenListener,
    SelectAdapter.SelectItemInterface, CastStateListener {

    private lateinit var binding: ActivityMainBugsBinding

    private var mPlayer: JWPlayer? = null

    private val GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD = "com.google.market"
    private val GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW = "com.android.vending"
    private val TAG = "MainActivity::Class"

    private var mSessionManagerListener: SessionManagerListener<CastSession>? = null
    private var mCastContext: CastContext? = null
    private var mCastSession: CastSession? = null

    //    var mMediaInfo: MediaInfo? = null
    var remoteMediaClient: RemoteMediaClient? = null
    var EXTRA_DATA: String? = "EXTRA_DATA"
    var CUSTOME_DATA: String? = "CUSTOME_DATA"

    private var listChromcastEpisode = ArrayList<MediaQueueItem>()

    private val mRemoteMediaClientCallback: RemoteMediaClient.Callback =
        MyRemoteMediaClientCallback()

    var seasonHashMap: HashMap<Int,Int> = HashMap()

//    var nQualityLevel: ArrayList<QualityLevel> = ArrayList()
    val nSubtitle = ArrayList<Caption>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_bugs)

        setupActionBar()
        setupCastListener()

        WebView.setWebContentsDebuggingEnabled(true)
        // TODO: Add your license key
        LicenseUtil().setLicenseKey(this, "OwyAxwyK8E//wkcI5SlH3MIBOqrcNP8P3YI3SKFF5zMdhshU")


        mCastContext = CastContext.getSharedInstance(this)
        mCastSession = mCastContext!!.sessionManager.currentCastSession
        mCastContext!!.addCastStateListener(this)

        binding.mPlayerView.getPlayerAsync(this, this) { jwPlayer: JWPlayer? ->
            mPlayer = jwPlayer
            mPlayer!!.setFullscreenHandler(ZoomHandler(binding.mPlayerView))
            setupPlayer1()
        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    override fun onResume() {
        Log.d(TAG, "onResume() was called")
        mCastContext!!.sessionManager.addSessionManagerListener(
            mSessionManagerListener!!, CastSession::class.java
        )

        super.onResume()
    }


    var positionSeason = 0
    var positionEpisode = 0
    private fun setupPlayer1() {
        // Handle hiding/showing of ActionBar
        mPlayer!!.addListener(EventType.FULLSCREEN, this)

        // Keep the screen on during playback
        KeepScreenOnHandler(mPlayer!!, window)

        val data = Gson().fromJson(AppConstant.data, ModelClass::class.java) as ModelClass
        val playlist: MutableList<PlaylistItem> = ArrayList()
        var listQueue = ArrayList<Episode>()
        val captionList: MutableList<Caption> = ArrayList()


        for ((i, season) in data[0].seasons.withIndex()) {
            seasonHashMap[i] = season.episodes.size
            for (episode in data[0].seasons[i].episodes) {
                Log.i("TAGvv", "setupPlayer1: ${episode.media}")

                val caption1 = Caption.Builder()
                    .file("")
                    .kind(CaptionType.CAPTIONS)
                    .label("off")
                    .isDefault(true)
                    .build()
                captionList.add(caption1)


                val caption2 = Caption.Builder()
                    .file("file:///android_asset/press-play-captions.vtt")
                    .kind(CaptionType.CAPTIONS)
                    .label("eng")
                    .isDefault(true)
                    .build()
                captionList.add(caption2)


                val pi = PlaylistItem.Builder()
                    .description(episode.description)
                    .file(episode.media)
                    .image(episode.original_thumbnail_file)
                    .tracks(captionList)
                    .title(episode.title)
                    .build()

                episode.captionList = captionList
//            episode.qualityLevelList = pi.
                listQueue.add(episode)
                playlist.add(pi)
            }
            Log.i("TAGvv", "SeasonPos: ${i}")
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
                    .hide(UiGroup.NEXT_UP)
                    .show(UiGroup.PLAYLIST)
                    .build()
            )
            .build()

        mPlayer!!.setup(playerConfig)

        mPlayer!!.setFullscreen(true, true)


        queuePlay(listQueue)
        bindSettingPan(CustomPlayerViewModel(mPlayer!!), playerConfig!!, data)

        /* val mEpisode = data[0].seasons[positionSeason].episodes[positionEpisode]

         val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
         movieMetadata.putString(MediaMetadata.KEY_TITLE, mEpisode.title)
 //      movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mEpisode.)
         movieMetadata.addImage(WebImage(Uri.parse(mEpisode.thumbnail_url)))
 //      movieMetadata.addImage(WebImage(Uri.parse(mEpisode.getImage(1))))

         Log.i(TAG, "loadVideos: ${mPlayer!!.duration} - ${mEpisode.media} - ${mEpisode.title}")
         mMediaInfo = MediaInfo.Builder(mEpisode.media)
             .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
             .setContentType("videos/mp4")
             .setMetadata(movieMetadata)
             .setStreamDuration((mPlayer!!.duration * 1000).toLong())
             .build()*/

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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.expanded_controller, menu)
        val itemCast = CastButtonFactory.setUpMediaRouteButton(
            applicationContext, menu,
            R.id.media_route_menu_item
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            itemCast.actionView.defaultFocusHighlightEnabled = false
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.aspect_ratio_menu -> {
                sec3Timer()
                val isFull = mPlayer!!.fullscreen
                if (isFull) {
                    item.setIcon(R.drawable.ic_aspect_ratio)
                } else {
                    item.setIcon(R.drawable.ic_full_screen)
                }

//                controls!!.onZoomUpdatePan(isFull)
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
//                if (null != mMediaInfo) {

//                    if (mPlaybackState == PlaybackState.PLAYING) {
//                        mVideoView.pause()
                loadRemoteMedia(positionEpisode, true) //mSeekbar.getProgress()
//                        return
//                    } else {
//                        mPlaybackState = PlaybackState.IDLE
//                        updatePlaybackLocation(REMOTE)
//                    }
//                }
//                updatePlayButton(mPlaybackState)
                invalidateOptionsMenu()

            }

            private fun onApplicationDisconnected() {
//                updatePlaybackLocation(LOCAL)
//                mPlaybackState = PlaybackState.IDLE
//                mLocation = LOCAL
//                updatePlayButton(mPlaybackState)
                if (mPlayer != null) {
                    mPlayer!!.play()
                }
//                remoteMediaClient!!.unregisterCallback(this)
                invalidateOptionsMenu()
            }
        }
    }

    private fun loadRemoteMedia(position: Int, autoPlay: Boolean) {
        Log.i("TAGlk", "Playing GOLD...")
        if (mCastSession == null) {
            return
        }
        remoteMediaClient = mCastSession!!.remoteMediaClient ?: return

        val customeData = JSONObject()
        try {
            customeData.put(CUSTOME_DATA, listChromcastEpisode)
        } catch (e: Exception) {
            Log.i(TAG, "queuePlay: exception $e")

        }

        remoteMediaClient!!.queueLoad(
            listChromcastEpisode.toTypedArray(),
            position,
            REPEAT_MODE_REPEAT_OFF,
            customeData
        )

        Log.i(
            TAG,
            "listChromcastEpisode: ${positionEpisode} ${listChromcastEpisode[positionEpisode].itemId}"
        )


        remoteMediaClient!!.registerCallback(object : RemoteMediaClient.Callback() {
            override fun onStatusUpdated() {

//                val intent = Intent(this@MainBugsActivity, ExpandedControlsActivity::class.java)
//                startActivity(intent)

                val mMediaStatus = remoteMediaClient!!.mediaStatus
//                Log.w(TAG, "onStatusUpdated: remoteMediaClient $remoteMediaClient")
                if (mMediaStatus != null && mMediaStatus.queueItems != null) {

//                    if (queueItemPlayedPosition < mMediaStatus.currentItemId) {
//                        Log.w(TAG, "onStatusUpdated: Delete video $queueItemPlayedPosition")
//                        updateCastList(false)
//                        queueItemPlayedPosition++
//                    }

                    Log.e(
                        TAG,
                        "onStatusUpdated getCurrentItemId " + (remoteMediaClient!!.mediaStatus?.currentItemId) + " ***  getQueueItemCount *** " + mMediaStatus!!.queueItemCount
                    )
                }

//                remoteMediaClient!!.unregisterCallback(this)
            }
        })
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

    override fun onDestroy() {
        super.onDestroy()
//        remoteMediaClient!!.unregisterCallback(this)
    }

    @JvmName("getRemoteMediaClient1")
    fun getRemoteMediaClient(): RemoteMediaClient? {
        val castSession = CastContext.getSharedInstance(this).sessionManager
            .currentCastSession
        return if (castSession != null && castSession.isConnected) {
            castSession.remoteMediaClient
        } else null
    }

    fun bindSettingPan(
        customPlayerView: CustomPlayerViewModel,
        playerConfig: PlayerConfig,
        data: ModelClass
    ) {

//        val myActivity: MainActivity = lifecycleOwner as MainActivity
        /*  nUiMediaController = CustomUIMediaController(lifecycleOwner as Activity)

          nUiMediaController!!.bindViewToUIController(binding.containerAll, nUiController!!)
          nUiMediaController!!.bindImageViewToPlayPauseToggle(binding.playPauseToggle,
              resources.getDrawable(R.drawable.ic_jw_play),
              resources.getDrawable(R.drawable.ic_jw_pause),
              resources.getDrawable(R.drawable.ic_jw_pause),
              binding.containerAll,
              false
          )*/
//        nUiMediaController!!.bindTextViewToMetadataOfCurrentItem(mTitle!!, "com.google.android.gms.cast.metadata.TITLE")
//        nUiMediaController!!.bindTextViewToSmartSubtitle(mSubtitleAudio!!)
//        nUiMediaController!!.bindViewToLaunchExpandedController(binding.containerAll)


        val cast = mPlayer!!.getViewModelForUiGroup(UiGroup.CASTING_MENU) as CastingMenuViewModel

        if (mCastSession != null && mCastSession!!.isConnected) {
            mPlayer!!.pause()
//            val intent = Intent(this, ExpandedControlsActivity::class.java)
//            startActivity(intent)
        }

        val mEpisode = data[0].seasons[positionSeason].episodes[positionEpisode]
//        val jsonObj: JSONObject = VideoProvider().parseUrl(mEpisode.media)
//        val jsonObj = VideoProvider.buildMedia(mEpisode.media) as JSONObject


        customPlayerView.isFirstFrame.observe(this) { mJWPlayer: JWPlayer ->
            binding.tvTitle.text = mJWPlayer.playlistItem.mTitle
            binding.toolbar.title = mJWPlayer.playlistItem.mTitle
        }

        customPlayerView.printTime.observe(this) {
            binding.tvTime.text = it
        }

        customPlayerView.isVisibility.observe(this) {
            sec3Timer()
        }

        binding.tvVideoSetting!!.setOnClickListener { v: View? ->
            sec3Timer()
//            mPlayer!!.pause()
            visibilityComponents(ConstraintLayout.GONE)
            AlertDialogPlayer(1001)
        }

        binding.tvSubtitle.setOnClickListener { v: View? ->
            sec3Timer()
//            mPlayer!!.pause()
            visibilityComponents(ConstraintLayout.GONE)
            AlertDialogPlayer(1003)
        }

        binding.containerAll.setOnClickListener {
            sec3Timer()
        }

        binding.tvEpisode.setOnClickListener {
            sec3Timer()
            visibilityComponents(ConstraintLayout.GONE)
            AlertDialogPlayList(customPlayerView, data[0].seasons, playerConfig)
        }

        binding.tvNextEpisode.setOnClickListener {
            sec3Timer()

            val next = mPlayer!!.playlistIndex + 1

            if (next != mPlayer!!.playlist.size) {
                positionEpisode = next
                if (mCastContext!!.castState == CastState.CONNECTED) {
                    remoteMediaClient = getRemoteMediaClient()
                    Log.i(TAG, "bindSettingPan: ${listChromcastEpisode[positionEpisode].itemId} ${remoteMediaClient} ${positionEpisode}")
//                    remoteMediaClient!!.queueJumpToItem(listChromcastEpisode[positionEpisode].itemId, JSONObject())
                    loadRemoteMedia(positionEpisode, true)
                }
                mPlayer!!.playlistItem(next)

            } else {
                Toast.makeText(this, "Last Video", Toast.LENGTH_LONG).show()
            }

        }

//        ivFastForward15!!.setOnClickListener {
//            sec3Timer()
//            val position = mPlayer!!.position + 30
//
//            if (position >= mPlayer!!.duration) {
//                val next = mPlayer!!.playlistIndex + 1
//                if (next != mPlayer!!.playlist.size) {
//                    mPlayer!!.playlistItem(next)
//                }
//            } else if (position >= 0) {
//                mPlayer!!.seek(position)
//                customPlayerView.handleTimeUpdate(
//                    position,
//                    mPlayer!!.duration,
//                    customPlayerView.contentProgressPercentage
//                )
//            }
//        }

//        ivFastBackward15!!.setOnClickListener {
//            sec3Timer()
//            val position = mPlayer!!.position - 30
//            if (position > 31) {
//                mPlayer!!.seek(position)
//                customPlayerView.handleTimeUpdate(
//                    position,
//                    mPlayer!!.duration,
//                    customPlayerView.contentProgressPercentage
//                )
//            } else {
//                mPlayer!!.seek(0.1)
//                customPlayerView.handleTimeUpdate(
//                    0.1,
//                    mPlayer!!.duration,
//                    customPlayerView.contentProgressPercentage
//                )
//            }
//        }


        //SeekBar
        customPlayerView.contentProgressPercentage.observe(this) { progress ->
            binding.seekbar.progress = progress
        }

        customPlayerView.isSeekbarVisible.observe(this) { isVisible ->
//            contentSeekBar!!.visibility = if (isVisible) VISIBLE else GONE
//            tvTime!!.visibility = if (isVisible) VISIBLE else GONE

            if (isVisible) {
                visibilityComponents(ConstraintLayout.VISIBLE)
            } else {
                visibilityComponents(ConstraintLayout.GONE)
            }
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    customPlayerView.seek(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        customPlayerView.isPlayToggleVisible.observe(this) { isVisible ->
            binding.playPauseToggle.visibility =
                if (isVisible) ConstraintLayout.VISIBLE else ConstraintLayout.GONE
        }
        customPlayerView.isPlayIcon.observe(this) { isPlay ->
            binding.playPauseToggle.setImageDrawable(
                if (isPlay) AppCompatResources
                    .getDrawable(this, R.drawable.ic_jw_play) else AppCompatResources
                    .getDrawable(this, R.drawable.ic_jw_pause)
            )
        }
        binding.playPauseToggle.setOnClickListener { v: View? ->
            sec3Timer()

            if (mCastContext!!.castState == CastState.CONNECTED) {
                Log.w(TAG, "Connected to a cast device")
//                remoteMediaClient!!.togglePlayback()
//                val intent = Intent(this, ExpandedControlsActivity::class.java)
//                startActivity(intent)
            } else {
                customPlayerView.togglePlay()
            }
        }
    }

    var countDown: CountDownTimer? = null
    var VisibilityTime = 2000
    private fun sec3Timer() {
        visibilityComponents(ConstraintLayout.VISIBLE)
        countDown?.cancel()
        countDown = object : CountDownTimer(VisibilityTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "Timer " + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                visibilityComponents(ConstraintLayout.GONE)
            }
        }.start()
    }

    private fun visibilityComponents(isVisible: Int) {
        binding.tvVideoSetting.visibility = isVisible
        binding.tvEpisode.visibility = isVisible
        binding.tvSubtitle.visibility = isVisible
        binding.tvTitle.visibility = ConstraintLayout.GONE
        binding.seekbar.visibility = isVisible
        binding.tvTime.visibility = isVisible
        binding.playPauseToggle.visibility = isVisible
        binding.tvNextEpisode.visibility = isVisible
        binding.ivChromeCast.visibility = View.GONE
        binding.toolbar.visibility = isVisible
//        ivFastForward15!!.visibility = isVisible
//        ivFastBackward15!!.visibility = isVisible
    }

    private fun AlertDialogPlayer(FLAG: Int) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val binding: DialogPlayrateSubtitleBinding =
            DataBindingUtil.inflate(
                dialog.layoutInflater,
                R.layout.dialog_playrate_subtitle,
                null,
                false
            )
        if (FLAG == 1001) {
            binding.adapterPlayRate = setPlayRateData()

            val nQualityLevel = ArrayList(mPlayer!!.qualityLevels)
            binding.adapterQuality = setQualityLevel(nQualityLevel)

            Log.i(TAG, "AlertDialogPlayer: ")
        } else if (FLAG == 1003) {
            val subtitleList = ArrayList(mPlayer!!.captionsList)
            binding.adapterSubtitle = setSubtitle(subtitleList)
            binding.rvSubtitle.visibility = ConstraintLayout.VISIBLE
            binding.rvQualityLevel.visibility = ConstraintLayout.GONE
            binding.tvQualityLevel.visibility = ConstraintLayout.GONE
            binding.rvPlayRate.visibility = ConstraintLayout.GONE
            binding.tvPlayRate.text = "Subtitle"
//            val audioList = ArrayList(mPlayer!!.audioTracks)
//            binding.adapterQuality = setAudio(audioList)
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvDone.setOnClickListener {
            if (FLAG == 1001) {
                if (mValuePlayRate != 0.0) {
                    mPlayer!!.playbackRate = mValuePlayRate
                }
                if (mValueQuality != null) {
                    mPlayer!!.currentQuality = mValueQuality!!.trackIndex
                }
            } else if (FLAG == 1003) {
//                if (mValueAudio != null) {
//                    mPlayer!!.currentAudioTrack = mValueAudio.
//                }
                if (mValueSubtitle != null) {
                    Log.i(
                        TAG,
                        "AlertDialogPlayer: ${mValueSubtitle!!.label} : ${mPositionSubTitle}"
                    )
//                    mPlayer!!.setCurrentCaptions(mValueSubtitle!!.label)


                    if (mPositionSubTitle == 0) {
                        mPlayer!!.currentCaptions = mPositionSubTitle
                    } else {
                        mPlayer!!.currentCaptions = mPositionSubTitleSeleted
                    }
                    mPlayer!!.play()


                }
            }
//            mPlayer!!.play()
            dialog.dismiss()
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun setPlayRateData(): SelectAdapter {
        val list = ArrayList<SelectItem>()
        val playbackRates = doubleArrayOf(0.25, 0.5, 1.0, 1.25, 1.5, 2.0)
        for (item in playbackRates) {
            val isSelect = mValuePlayRate == item
            list.add(SelectItem(item, null, null, null, isSelect))
        }
        return SelectAdapter(this, list, 1001, this)
    }

    private fun setQualityLevel(qualityLevel: ArrayList<QualityLevel>): SelectAdapter {
        val list = ArrayList<SelectItem>()
        var isFHD = true
        var isHD = true
        var isSD = true
        for (item in qualityLevel) {

            val isSelect = (mValueQuality != null && mValueQuality!!.trackIndex == item.trackIndex)

            if (item.label.toString().contains("Auto")) {
                if (mValueQuality == null) {
                    list.add(SelectItem(0.0, item, null, null, true))
                } else {
                    list.add(SelectItem(0.0, item, null, null, isSelect))
                }
            } else if (item.label.toString().contains("1080p") && isFHD) {
                isFHD = false
                list.add(SelectItem(0.0, item, null, null, isSelect))
            } else if (item.label.toString().contains("720p") && isHD) {
                isHD = false
                list.add(SelectItem(0.0, item, null, null, isSelect))
            } else if (item.label.toString().contains("360p") && isSD) {
                isSD = false
                list.add(SelectItem(0.0, item, null, null, isSelect))
            }

        }
        return SelectAdapter(this, list, 1002, this)
    }

    private fun setAudio(audioTrack: ArrayList<AudioTrack>): SelectAdapter {
        val list = ArrayList<SelectItem>()
        for (item in audioTrack) {
            list.add(SelectItem(0.0, null, item, null, false))
        }
        return SelectAdapter(this, list, 1003, this)
    }


    private fun setSubtitle(caption: ArrayList<Caption>): SelectAdapter {
        val list = ArrayList<SelectItem>()
        for ((count, item) in caption.withIndex()) {
            val isSelect = mPositionSubTitle == 0
            var trackPos = 0L
            if (item.label.equals("Off")) {
                list.add(SelectItem(0.0, null, null, item, isSelect))
            } else if (item.label.equals("eng")) {
                mPositionSubTitleSeleted = count
                list.add(SelectItem(0.0, null, null, item, !isSelect))
            }

            if (mCastContext!!.castState == CastState.CONNECTED) {
                var trackPos = 0L
                if (mPositionSubTitle != 0) trackPos = 1

                remoteMediaClient!!.setActiveMediaTracks(longArrayOf(trackPos))
                    .setResultCallback(ResultCallback { mediaChannelResult: RemoteMediaClient.MediaChannelResult ->
                        if (!mediaChannelResult.status.isSuccess) {
                            Log.e(
                                TAG,
                                "Failed with status code:" + mediaChannelResult.status.statusCode
                            )
                        }
                    })
            }
        }
        return SelectAdapter(this, list, 1004, this)
    }

    var mValuePlayRate: Double = 1.0
    var mValueQuality: QualityLevel? = null
    var mValueAudio: AudioTrack? = null
    var mValueSubtitle: Caption? = null
    var mPositionSubTitle: Int = 1
    var mPositionSubTitleSeleted: Int = 0 // get the english sub caption pos

    override fun onSelected(data: SelectItem, position: Int, FLAG: Int) {
        if (FLAG == 1001) {
            mValuePlayRate = data.valuePlayRate!!
        } else if (FLAG == 1002) {
            mValueQuality = data.valueQuality!!
        } else if (FLAG == 1003) {
            mValueAudio = data.valueAudio!!
        } else if (FLAG == 1004) {
            mValueSubtitle = data.valueSubtitle!!
            mPositionSubTitle = position
        }
    }

    private fun AlertDialogPlayList(
        customPlayerView: CustomPlayerViewModel,
        season: List<Season>,
        playerConfig: PlayerConfig
    ) {
        val dialog = Dialog(
            this,
            android.R.style.Theme_Black_NoTitleBar_Fullscreen
        ) // where "this" is the context

        val binding: FragmentPlayListSeasonBinding =
            DataBindingUtil.inflate(
                dialog.layoutInflater,
                R.layout.fragment_play_list_season,
                null,
                false
            )

        val listSeason = ArrayList<String>()
        for (i in season.indices) {
            listSeason.add("Season ${i + 1}")
        }

        val adapterSpinner = ArrayAdapter(this, R.layout.layout_textview, listSeason)
        binding.spSeason.adapter = adapterSpinner

        binding.spSeason.setSelection(positionSeason)
        binding.spSeason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val adapter =
                    PlayListSeasonAdapter(this@MainBugsActivity, season[position].episodes,
                        object : PlayListSeasonAdapter.CastDevicesInterface {
                            override fun onVideoClick(data: Episode, posVideo: Int) {

                                positionSeason = position

                                var allEpisode = 0
                                for (map in seasonHashMap.entries) {
                                   if (map.key  != position) allEpisode += map.value
                                   else break
                                }


/*
                                val playlist: MutableList<PlaylistItem> = ArrayList()
                                for (episode in season[position].episodes) {
//                                    val caption = Caption.Builder()
//                                        .file("file:///android_asset/press-play-captions.vtt")
//                                        .kind(CaptionType.CAPTIONS)
//                                        .label("eng")
//                                        .isDefault(true)
//                                        .build()
//                                    val captionList: MutableList<Caption> = ArrayList()
//                                    captionList.add(caption)

                                    val pi = PlaylistItem.Builder()
                                        .description(episode.description)
                                        .file(episode.media)
                                        .image(episode.original_thumbnail_file)
                                        .tracks(episode.captionList)
                                        .title(episode.title)
                                        .build()
                                    Log.i(TAG, "onVideoClickk: ${episode.description}")
                                    Log.i(TAG, "onVideoClickk: ${episode.media}")
                                    playlist.add(pi)
                                }
*/

//                                mPlayer!!.setup(
//                                    PlayerConfig.Builder()
//                                        .playlist(playlist)
//                                        .playlistIndex(posVideo)
//                                        .uiConfig(playerConfig.mUiConfig)
//                                        .autostart(true)
//                                        .build())

                                if (mCastContext!!.castState == CastState.CONNECTED) {
                                    remoteMediaClient = getRemoteMediaClient()
                                    Log.i(TAG, "bindSettingPan: ${listChromcastEpisode[positionEpisode].itemId} ${remoteMediaClient} ${positionEpisode}")

                                    loadRemoteMedia(positionEpisode, true)
                                }

                                positionEpisode = allEpisode + posVideo
                                Log.i(TAG, "onVideoClick: ${positionEpisode}")
                                mPlayer!!.playlistItem(positionEpisode)

                                dialog.dismiss()
                            }
                        })

                binding.adapterPlayList = adapter
                positionSeason = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        dialog.window?.setBackgroundDrawableResource(R.color.transparent75)

        binding.ivClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(binding.root)
        dialog.show()
    }

    override fun onCastStateChanged(p0: Int) {
//        TODO("Not yet implemented")
    }


    private fun queuePlay(data: ArrayList<Episode>) {

        var queueList: ArrayList<MediaQueueItem> = ArrayList()
        for (item in data) {

            var mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)

            mediaMetadata.putString(MediaMetadata.KEY_TITLE, item.title)
            mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, item.description)
            mediaMetadata.addImage(WebImage(Uri.parse(item.thumbnail_url)))

            val extraData = JSONObject()

            try {
                extraData.put(EXTRA_DATA, item)

            } catch (e: Exception) {
                Log.i(TAG, "queuePlay: exception $e")
            }

            var tracks = ArrayList<MediaTrack>()
            if (item.captionList != null && item.captionList.isNotEmpty()) {

                for ((i, caption) in item.captionList.withIndex()) {

                    val name = if (caption.label.equals("off", ignoreCase = true)) "Off"
                    else "English"

                    tracks.add(
                        MediaTrack.Builder(i.toLong(), MediaTrack.TYPE_TEXT)
                            .setName(name)
                            .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                            .setContentId(caption.file)
                            .setLanguage(caption.label).build()
                    )
                }
            }


            val mMediaInfo = MediaInfo.Builder(item.media)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(mediaMetadata)
                .setCustomData(extraData)
                .setMediaTracks(tracks)
                .setStreamDuration(4 * 60 * 1000) // Custome Time
                .build()

            queueList.add(MediaQueueItem.Builder(mMediaInfo).build())
        }

        listChromcastEpisode.addAll(queueList)

    }

    private class MyRemoteMediaClientCallback : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            updateMediaQueue()
        }

        override fun onQueueStatusUpdated() {
            updateMediaQueue()
        }

        private fun updateMediaQueue() {
//            val mediaStatus: MediaStatus = mRemoteMediaClient.getMediaStatus()
//            val queueItems = if (mediaStatus == null) null else mediaStatus.queueItems
        }
    }


    private fun AlertDialogCast(
        cast: CastingMenuViewModel,
        listRoutes: List<MediaRouter.RouteInfo>
    ) {
        val dialog = Dialog(this) // where "this" is the context

        val binding: DialogCastBinding =
            DataBindingUtil.inflate(
                dialog.layoutInflater,
                R.layout.dialog_cast,
                null,
                false
            )

        val adapter =
            CastSelectAdapter(this, listRoutes, object : CastSelectAdapter.CastDevicesInterface {
                override fun onConnect(data: MediaRouter.RouteInfo, position: Int) {
                    dialog.dismiss()
                    cast.beginCasting(data)
                }
            })
        binding.adapterCast = adapter

        if (listRoutes.isEmpty()) {
            binding.tvMessage.visibility = View.VISIBLE
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }

/*   private fun AalertDialogDisconnectCast(
        cast: CastingMenuViewModel,
        castSession: CastSession
    ) {
        val dialog = Dialog(this) // where "this" is the context

        val binding: DialogCastBinding =
            DataBindingUtil.inflate(
                dialog.layoutInflater,
                R.layout.dialog_cast,
                null,
                false
            )

        binding.tvCasting.text = "Disconnect the Devices"
        binding.tvDisconnectDevicesName.text = castSession.castDevice!!.friendlyName

        binding.rvCast.visibility = View.GONE
        binding.tvDisconnect.visibility = View.VISIBLE
        binding.tvDisconnectDevicesName.visibility = View.VISIBLE

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.tvDisconnect.setOnClickListener {
            dialog.dismiss()
            mCastContext!!.sessionManager.endCurrentSession(true)
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }


    binding.ivChromeCast.setOnClickListener {
        //CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
        sec3Timer()

        val mRoutes: List<MediaRouter.RouteInfo> = MediaRouter.getInstance(this).routes
        val isCastRoutes = ArrayList<MediaRouter.RouteInfo>()
        val devices = ArrayList<CastDevice>()
//            Log.i(TAG, "bindSettingPan1: ${cast.availableDevices.}")
        for (routeInfo in mRoutes) {
            val device = CastDevice.getFromBundle(routeInfo.extras)
            if (device != null) {
                devices.add(device)
                isCastRoutes.add(routeInfo)
            }
        }

        if (mCastSession != null) {
            if (mCastSession!!.isConnected) {
                Log.i(TAG, "bindSettingPan1: ${mCastSession!!.castDevice!!.friendlyName}")
                Log.i(TAG, "bindSettingPan: ${mCastContext!!.sessionManager.currentSession!!.isConnected}"
                )
                AalertDialogDisconnectCast(cast!!, mCastSession!!)
            } else {
//                    remoteMediaClient = mCastSession!!.remoteMediaClient

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
//                        .setStreamDuration((customPlayerView.player.duration * 1000).toLong())
//                        .build()

//                    remoteMediaClient!!.load(MediaLoadRequestData.Builder().setMediaInfo(mSelectedMedia).build())

//                    loadRemoteMedia(0, true)
                AlertDialogCast(cast, isCastRoutes!!)
            }

        } else {
            mCastContext = CastContext.getSharedInstance(this)
//                CastButtonFactory.setUpMediaRouteButton(this, mediaRouteButton!!)
            mCastSession = mCastContext!!.sessionManager.currentCastSession
//                mCastContext!!.sessionManager.addSessionManagerListener(this@MainBugsActivity, CastSession::class.java)

            Log.i(TAG, "bindSettingPan1i: ${isCastRoutes!!.size}")
            Log.i(TAG, "bindSettingPan1i: ${cast.castingState}")
            AlertDialogCast(cast, isCastRoutes!!)
        }
    }*/

}