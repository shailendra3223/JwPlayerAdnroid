package com.google.sample.cast.refplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.cast.*
import com.google.android.gms.cast.framework.*
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import com.google.sample.cast.refplayer.adapter.PlayListSeasonAdapter
import com.google.sample.cast.refplayer.adapter.SelectAdapter
import com.google.sample.cast.refplayer.cast.CustomUIController
import com.google.sample.cast.refplayer.cast.CustomUIMediaController
import com.google.sample.cast.refplayer.cast.ExpandedControlsActivity
import com.google.sample.cast.refplayer.databinding.DialogPlayrateSubtitleBinding
import com.google.sample.cast.refplayer.databinding.FragmentPlayListSeasonBinding
import com.google.sample.cast.refplayer.model.SelectItem
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.media.adaptive.QualityLevel
import com.jwplayer.pub.api.media.audio.AudioTrack
import com.jwplayer.pub.api.media.captions.Caption
import com.jwplayer.pub.api.media.captions.CaptionType
import com.jwplayer.pub.api.media.playlists.PlaylistItem
import com.jwplayer.pub.ui.viewmodels.CastingMenuViewModel
import com.jwplayer.pub.ui.viewmodels.PlaylistViewModel
import com.jwplayer.pub.view.JWPlayerView
import kotlin.math.roundToInt


class CustomPlayerView(
    mContext: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ConstraintLayout(
    mContext!!, attrs, defStyleAttr, defStyleRes
), SelectAdapter.SelectItemInterface, SessionManagerListener<CastSession>, CastStateListener {
    private var mVideoSetting: TextView? = null
    private var mEpisodes: TextView? = null
    private var mSubtitleAudio: TextView? = null
    private var mNextEpisode: TextView? = null
    private var mTitle: TextView? = null
    private var tvTime: TextView? = null
    private var contentSeekBar: SeekBar? = null
    private var playToggle: ImageView? = null
    private val nContext = mContext

    private var TAG = "CustomPlayerView:Class"

    private var ivFastForward15: ImageView? = null
    private var ivFastBackward15: ImageView? = null
    private var mListener: OnMainScreenVisibilityListener? = null

    private lateinit var nPlayer: JWPlayer
    private lateinit var containerAll: View
//    private var containerAll: View? = null
    var nUiMediaController: CustomUIMediaController? = null
    var nUiController: CustomUIController? = null

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : this(
        context,
        attrs,
        defStyleAttr,
        0
    ) {
    }

    fun initListener(nListener: OnMainScreenVisibilityListener?) {
        mListener = nListener
    }

    private fun initView(context: Context?) {
        containerAll = inflate(context, R.layout.view_custom_player_ui, this)
//        val binding: ViewCustomPlayerUiBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_custom_player_ui, null, true)
//        val content = findViewById<ViewGroup>(android.R.id.content)
//        binding = MainActivity.inflate(layoutInflater, content)
//        val containerAll = findViewById<View>(R.id.cl_parent_view)
//        val mergeBinding = ViewCustomPlayerUiBinding.bind(binding.root)
        mVideoSetting = findViewById(R.id.tv_video_setting)
        mEpisodes = findViewById(R.id.tv_episode)
        mSubtitleAudio = findViewById(R.id.tv_subtitle)
        mNextEpisode = findViewById(R.id.tv_next_episode)
        contentSeekBar = findViewById(R.id.seekbar)
        playToggle = findViewById(R.id.play_pause_toggle)
        mTitle = findViewById(R.id.tv_title)
        ivFastForward15 = findViewById(R.id.tv_fast_forward_15)
        ivFastBackward15 = findViewById(R.id.tv_fast_backward_15)
        tvTime = findViewById(R.id.tv_time)

        nUiController = CustomUIController(containerAll)

    }

    var cast: CastingMenuViewModel? = null
    var mCastContext: CastContext? = null
    var mCastSession: CastSession? = null
    var mSelectedMedia: MediaInfo? = null
    var remoteMediaClient: RemoteMediaClient? = null


    init {
        initView(mContext)
    }

    @SuppressLint("SetTextI18n")
    fun bindSettingPan(
        customPlayerView: CustomPlayerViewModel,
        playlistViewModel: PlaylistViewModel,
        playerConfig: PlayerConfig,
        lifecycleOwner: LifecycleOwner,
        data: ModelClass,
        jwplayerView: JWPlayerView,
        castContext: CastContext
    ) {

//        val myActivity: MainActivity = lifecycleOwner as MainActivity
        nUiMediaController = CustomUIMediaController(lifecycleOwner as Activity)

        nUiMediaController!!.bindViewToUIController(containerAll, nUiController!!)
        nUiMediaController!!.bindImageViewToPlayPauseToggle(playToggle!!,
            resources.getDrawable(R.drawable.ic_jw_play),
            resources.getDrawable(R.drawable.ic_jw_pause),
            resources.getDrawable(R.drawable.ic_jw_pause),
            containerAll,
            false
        )
//        nUiMediaController!!.bindTextViewToMetadataOfCurrentItem(mTitle!!, "com.google.android.gms.cast.metadata.TITLE")
//        nUiMediaController!!.bindTextViewToSmartSubtitle(mSubtitleAudio!!)
//        nUiMediaController!!.bindViewToLaunchExpandedController(containerAll)

        nPlayer = customPlayerView.player
        val widthDp = resources.displayMetrics.run { widthPixels / density }
        val heightDp = resources.displayMetrics.run { heightPixels / density }

        var halfHeightDp = (heightDp / 2).toInt() - 30
        var marginTop = 0

        if (halfHeightDp < 140) {
            halfHeightDp -= 15
            marginTop = (halfHeightDp/2) - 15
        } else {
            marginTop = (halfHeightDp/2)
        }

        if (marginTop <= 0) {
            marginTop = (halfHeightDp / 2)
        }

        Log.i(TAG, "onZoomUpdate5x: ${widthDp}  ${heightDp} ${halfHeightDp}  ${marginTop}")

        mCastContext = castContext
        mCastContext!!.addCastStateListener(this)
        mCastSession = mCastContext!!.sessionManager.currentCastSession
        mCastContext!!.sessionManager.addSessionManagerListener(this, CastSession::class.java)
        cast = customPlayerView.player.getViewModelForUiGroup(UiGroup.CASTING_MENU) as CastingMenuViewModel

        playToggle!!.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(dpFormat(10), dpFormat(halfHeightDp), dpFormat(2), dpFormat(4))
        }

        contentSeekBar!!.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(dpFormat(10), dpFormat(marginTop), dpFormat(2), dpFormat(4))
        }

        if (mCastSession!=null && mCastSession!!.isConnected) {
            customPlayerView.player.pause()
//            val intent = Intent(context, ExpandedControlsActivity::class.java)
//            context.startActivity(intent)
        }

        val mEpisode = data[0].seasons[positionSeason].episodes[positionEpisode]
//        val jsonObj: JSONObject = VideoProvider().parseUrl(mEpisode.media)
//        val jsonObj = VideoProvider.buildMedia(mEpisode.media) as JSONObject


        customPlayerView.isFirstFrame.observe(lifecycleOwner) { mJWPlayer: JWPlayer ->
            mTitle!!.text = mJWPlayer.playlistItem.mTitle
            mListener!!.onToolInfo(mJWPlayer.playlistItem.mTitle)

        }

        customPlayerView.printTime.observe(lifecycleOwner) {
            tvTime!!.text = it
        }

        customPlayerView.isVisibility.observe(lifecycleOwner) {
            sec3Timer(customPlayerView)
        }

        mVideoSetting!!.setOnClickListener { v: View? ->
            sec3Timer(customPlayerView)
//            customPlayerView.player.pause()
            visibilityComponents(GONE)
            AlertDialogPlayer(customPlayerView, 1001)
        }

        mSubtitleAudio!!.setOnClickListener { v: View? ->
            sec3Timer(customPlayerView)
//            customPlayerView.player.pause()
            visibilityComponents(GONE)
            AlertDialogPlayer(customPlayerView, 1003)
        }

        containerAll.setOnClickListener {
            sec3Timer(customPlayerView)
        }

        mEpisodes!!.setOnClickListener {
            sec3Timer(customPlayerView)
            visibilityComponents(GONE)
            AlertDialogPlayList(customPlayerView, data[positionSeason].seasons, playerConfig)
        }

        mNextEpisode!!.setOnClickListener {
            sec3Timer(customPlayerView)
            val next = customPlayerView.player.playlistIndex + 1
            if (next != customPlayerView.player.playlist.size) {
                positionEpisode = next
                customPlayerView.player.playlistItem(next)
            } else {
//                Toast.makeText(this, "Last Video", Toast.LENGTH_LONG).show()
            }
        }

//        ivFastForward15!!.setOnClickListener {
//            sec3Timer(customPlayerView)
//            val position = customPlayerView.player.position + 30
//
//            if (position >= customPlayerView.player.duration) {
//                val next = customPlayerView.player.playlistIndex + 1
//                if (next != customPlayerView.player.playlist.size) {
//                    customPlayerView.player.playlistItem(next)
//                }
//            } else if (position >= 0) {
//                customPlayerView.player.seek(position)
//                customPlayerView.handleTimeUpdate(
//                    position,
//                    customPlayerView.player.duration,
//                    customPlayerView.contentProgressPercentage
//                )
//            }
//        }

//        ivFastBackward15!!.setOnClickListener {
//            sec3Timer(customPlayerView)
//            val position = customPlayerView.player.position - 30
//            if (position > 31) {
//                customPlayerView.player.seek(position)
//                customPlayerView.handleTimeUpdate(
//                    position,
//                    customPlayerView.player.duration,
//                    customPlayerView.contentProgressPercentage
//                )
//            } else {
//                customPlayerView.player.seek(0.1)
//                customPlayerView.handleTimeUpdate(
//                    0.1,
//                    customPlayerView.player.duration,
//                    customPlayerView.contentProgressPercentage
//                )
//            }
//        }


        //SeekBar
        customPlayerView.contentProgressPercentage.observe(lifecycleOwner) { progress ->
            contentSeekBar!!.progress = progress.toInt()
        }

        customPlayerView.isSeekbarVisible.observe(lifecycleOwner) { isVisible ->
//            contentSeekBar!!.visibility = if (isVisible) VISIBLE else GONE
//            tvTime!!.visibility = if (isVisible) VISIBLE else GONE

            if (isVisible){
                visibilityComponents(VISIBLE)
            } else {
                visibilityComponents(GONE)
            }
        }

        contentSeekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    customPlayerView.seek(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        customPlayerView.isPlayToggleVisible.observe(lifecycleOwner) { isVisible ->
            playToggle!!.visibility = if (isVisible) VISIBLE else GONE
        }
        customPlayerView.isPlayIcon.observe(lifecycleOwner) { isPlay ->
            playToggle!!.setImageDrawable(
                if (isPlay) AppCompatResources
                    .getDrawable(context, R.drawable.ic_jw_play) else AppCompatResources
                    .getDrawable(context, R.drawable.ic_jw_pause)
            )
        }
        playToggle!!.setOnClickListener { v: View? ->
            sec3Timer(customPlayerView)

            if (mCastSession!=null && mCastSession!!.isConnected) {
                Log.w(TAG, "Connected to a cast device")
//                val intent = Intent(context, ExpandedControlsActivity::class.java)
//                context.startActivity(intent)
            } else {
                customPlayerView.togglePlay()
            }
        }
    }

    fun onZoomUpdate(isFull: Boolean) {
        sec3Timer(null)

//        val widthDp = resources.displayMetrics.run { widthPixels / density }
//        val heightDp = resources.displayMetrics.run { heightPixels / density }
//
//        val halfHeightDp = (heightDp / 2).toInt() - 60
//
//        var marginTop = (halfHeightDp / 2) - 20
//
//
//        if (marginTop <= 0) {
//            marginTop = (halfHeightDp / 2).toInt()
//        }

//        Log.i(TAG, "onZoomUpdate5: ${widthDp}  ${heightDp} ${halfHeightDp}  ${marginTop} ")
//        contentSeekBar!!.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//            setMargins(dpFormat(10), dpFormat(marginTop), dpFormat(2), dpFormat(4))
//        }
    }

    var countDown: CountDownTimer? = null
    var VisibilityTime = 2000
    private fun sec3Timer(customPlayerView: CustomPlayerViewModel?) {
        visibilityComponents(VISIBLE)
        countDown?.cancel()
        countDown = object : CountDownTimer(VisibilityTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "Timer " + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                visibilityComponents(GONE)
            }
        }.start()
    }

    private fun visibilityComponents(isVisible: Int) {
        mListener!!.onVisible(isVisible == VISIBLE)
        mVideoSetting!!.visibility = isVisible
        mEpisodes!!.visibility = isVisible
        mSubtitleAudio!!.visibility = isVisible
        mTitle!!.visibility = GONE
        contentSeekBar!!.visibility = isVisible
        tvTime!!.visibility = isVisible
        playToggle!!.visibility = isVisible
        mNextEpisode!!.visibility = isVisible
//        ivFastForward15!!.visibility = isVisible
//        ivFastBackward15!!.visibility = isVisible
    }

    private fun AlertDialogPlayer(customPlayerView: CustomPlayerViewModel, FLAG: Int) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val binding: DialogPlayrateSubtitleBinding =
            DataBindingUtil.inflate(
                dialog.layoutInflater,
                R.layout.dialog_playrate_subtitle,
                null,
                false
            )
        if (FLAG == 1001) {
            binding.adapterPlayRate = setPlayRateData()

            val qualityLevel = ArrayList(customPlayerView.player.qualityLevels)
            binding.adapterQuality = setQualityLevel(qualityLevel)
        } else if (FLAG == 1003) {
            val subtitleList = ArrayList(customPlayerView.player.captionsList)
            binding.adapterSubtitle = setSubtitle(subtitleList)
            binding.rvSubtitle.visibility = VISIBLE
            binding.rvQualityLevel.visibility = GONE
            binding.tvQualityLevel.visibility = GONE
            binding.rvPlayRate.visibility = GONE
            binding.tvPlayRate.text = "Subtitle"
//            val audioList = ArrayList(customPlayerView.player.audioTracks)
//            binding.adapterQuality = setAudio(audioList)
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvDone.setOnClickListener {
            if (FLAG == 1001) {
                if (mValuePlayRate != 0.0) {
                    customPlayerView.player.playbackRate = mValuePlayRate
                }
                if (mValueQuality != null) {
                    customPlayerView.player.currentQuality = mValueQuality!!.trackIndex
                }
            } else if (FLAG == 1003) else {
//                if (mValueAudio != null) {
//                    customPlayerView.player.currentAudioTrack = mValueAudio.
//                }
                if (mValueSubtitle != null) {
//                    customPlayerView.player.setCurrentCaptions(mValueSubtitle!!.label)
                    customPlayerView.player.setCurrentCaptions(mPositionSubTitle)
                }
            }
//            customPlayerView.player.play()
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
        return SelectAdapter(context, list, 1001, this)
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
                } else{
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
        return SelectAdapter(context, list, 1002, this)
    }

    private fun setAudio(audioTrack: ArrayList<AudioTrack>): SelectAdapter {
        val list = ArrayList<SelectItem>()
        for (item in audioTrack) {
            list.add(SelectItem(0.0, null, item, null, false))
        }
        return SelectAdapter(context, list, 1003, this)
    }

    private fun setSubtitle(caption: ArrayList<Caption>): SelectAdapter {
        val list = ArrayList<SelectItem>()
        for (item in caption) {

            val isSelect = mPositionSubTitle == 0

            if (item.label.equals("Off")) {
                list.add(SelectItem(0.0, null, null, item, isSelect))
            } else if (item.label.equals("en")) {
                list.add(SelectItem(0.0, null, null, item, !isSelect))
            }
        }
        return SelectAdapter(context, list, 1004, this)
    }

    var mValuePlayRate: Double = 1.0
    var mValueQuality: QualityLevel? = null
    var mValueAudio: AudioTrack? = null
    var mValueSubtitle: Caption? = null
    var mPositionSubTitle: Int = 0

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

    var positionSeason = 0
    var positionEpisode = 0
    private fun AlertDialogPlayList(
        customPlayerView: CustomPlayerViewModel,
        season: List<Season>,
        playerConfig: PlayerConfig
    ) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) // where "this" is the context

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

        val adapterSpinner =
            ArrayAdapter(context, R.layout.layout_textview, listSeason)
        binding.spSeason.adapter = adapterSpinner

        binding.spSeason.setSelection(positionSeason)
        binding.spSeason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val adapter = PlayListSeasonAdapter(
                    context,
                    season[position].episodes,
                    object : PlayListSeasonAdapter.CastDevicesInterface {
                        override fun onVideoClick(data: Episode, posVideo: Int) {
                            positionEpisode = posVideo
                            val playlist: MutableList<PlaylistItem> = ArrayList()
                            for (episode in season[position].episodes) {
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


                            if (mCastSession!=null && mCastSession!!.isConnected) {
                                customPlayerView.player.setup(
                                    PlayerConfig.Builder()
                                        .playlist(playlist)
                                        .playlistIndex(posVideo)
                                        .uiConfig(playerConfig.mUiConfig)
                                        .autostart(false)
                                        .build()
                                )
                                loadVideo(season[position].episodes[posVideo], customPlayerView.player)
//                                loadRemoteMedia(0, true, season[position].episodes[posVideo], customPlayerView.player)
                            } else {
                                customPlayerView.player.setup(
                                    PlayerConfig.Builder()
                                        .playlist(playlist)
                                        .playlistIndex(posVideo)
                                        .uiConfig(playerConfig.mUiConfig)
                                        .autostart(true)
                                        .build()
                                )
                            }

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

    fun dpFormat(dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    override fun onSessionEnded(session: CastSession, p1: Int) {
        Log.i(TAG, "onSessionEnded")
        if (nPlayer != null) {
            nPlayer.play()
        }
    }

    override fun onSessionEnding(session: CastSession) {
        Log.i(TAG, "onSessionEnding")
    }

    override fun onSessionResumeFailed(session: CastSession, p1: Int) {
        Log.i(TAG, "onSessionResumeFailed")
        if (nPlayer != null) {
            nPlayer.play()
        }
    }

    override fun onSessionResumed(session: CastSession, p1: Boolean) {
        Log.i(TAG, "onSessionResumed")
//        onApplicationConnected(session)
    }

    override fun onSessionResuming(session: CastSession, p1: String) {
        Log.i(TAG, "onSessionResuming")

    }

    override fun onSessionStartFailed(session: CastSession, p1: Int) {
        Log.i(TAG, "onSessionStartFailed")
    }

    override fun onSessionStarted(session: CastSession, p1: String) {
        mCastSession = session
        Log.i(TAG, "onSessionStarted")
//        onApplicationConnected(session)
    }

    override fun onSessionStarting(session: CastSession) {
        Log.i(TAG, "onSessionStarting")
    }

    override fun onSessionSuspended(session: CastSession, p1: Int) {
        Log.i(TAG, "onSessionSuspended")
    }

    private var mIntroductoryOverlay: IntroductoryOverlay? = null
    private fun showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay!!.remove()
            return
        }
//        Handler().post {
//            mIntroductoryOverlay = IntroductoryOverlay.Builder(this, mediaRouteButton)
//                .setTitleText("Casting..")
//                .setOverlayColor(R.color.skyblue)
//                .setSingleTime()
//                .setOnOverlayDismissedListener(
//                    OnOverlayDismissedListener { mIntroductoryOverlay = null })
//                .build()
//            mIntroductoryOverlay!!.show()
//        }
    }

//    public class MyMediaIntentReceiver : MediaIntentReceiver() {
//        override fun onReceiveOtherAction(action: String, intent: Intent) {
//
//        }
//    }


    private fun loadRemoteMedia(position: Int, autoPlay: Boolean, mEpisode: Episode, mPlayer : JWPlayer) {
        if (mCastSession == null) {
            Toast.makeText(context, "RETURN", Toast.LENGTH_LONG).show()
            return
        }
        remoteMediaClient = mCastSession!!.remoteMediaClient ?: return
        remoteMediaClient!!.registerCallback(object : RemoteMediaClient.Callback() {
            override fun onStatusUpdated() {
                Toast.makeText(context, "STARTA", Toast.LENGTH_LONG).show()
//                val intent = Intent(context, ExpandedControlsActivity::class.java)
//                context.startActivity(intent)
                loadVideo(mEpisode, mPlayer)
                remoteMediaClient!!.unregisterCallback(this)
            }
        })
//        remoteMediaClient!!.load(
//            MediaLoadRequestData.Builder()
//                .setMediaInfo(mSelectedMedia)
//                .setAutoplay(autoPlay)
//                .setCurrentTime(position.toLong()).build()
//        )


    }

    private fun onApplicationConnected(castSession: CastSession) {
        mCastSession = castSession
        if (null != mSelectedMedia) {
//            loadRemoteMedia(contentSeekBar!!.progress, true)
        }
    }

//    private fun onApplicationDisconnected() {
//        updatePlaybackLocation(PlaybackLocation.LOCAL)
//        mPlaybackState = PlaybackState.IDLE
//        mLocation = PlaybackLocation.LOCAL
//        updatePlayButton(mPlaybackState)
//        invalidateOptionsMenu()
//    }

    fun mediaTracker(episode: Episode) {
        val englishSubtitle = MediaTrack.Builder(1 /* ID */, MediaTrack.TYPE_TEXT)
            .setName("English Subtitle")
            .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
            .setContentId("https://some-url/caption_en.vtt")
            /* language is required for subtitle type but optional otherwise */
            .setLanguage("en-US")
            .build()

        val tracks: MutableList<MediaTrack> = ArrayList<MediaTrack>()
        tracks.add(englishSubtitle)
        val mediaInfo = MediaInfo.Builder(episode.url)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
//            .setContentType(getContentType())
//            .setMetadata(getMetadata())
            .setMediaTracks(tracks)
            .build()

    }

    private val PRELOAD_TIME_S = 20
    fun loadVideo(mEpisode: Episode, mPlayer : JWPlayer) {
        remoteMediaClient = mCastSession!!.remoteMediaClient
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mEpisode.title)
//      movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mEpisode.)
        movieMetadata.addImage(WebImage(Uri.parse(mEpisode.thumbnail_url)))
//      movieMetadata.addImage(WebImage(Uri.parse(mEpisode.getImage(1))))

        Log.i(TAG, "loadVideo: ${mPlayer.duration} - ${mEpisode.media} - ${mEpisode.title}")
        mSelectedMedia = MediaInfo.Builder(mEpisode.media)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("videos/mp4")
            .setMetadata(movieMetadata)
            .setStreamDuration((mPlayer.duration * 1000).toLong())
            .build()

        val queueItem: MediaQueueItem = MediaQueueItem.Builder(mSelectedMedia!!).setAutoplay(true)
//            .setPreloadTime(PRELOAD_TIME_S.toDouble())
            .build()
        val newItemArray = arrayOf(queueItem)

//        remoteMediaClient!!.queueLoad(
//            newItemArray, 0,
//            MediaStatus.REPEAT_MODE_REPEAT_OFF, JSONObject())

        remoteMediaClient!!.load(mSelectedMedia!!)
//        remoteMediaClient!!.queueInsertAndPlayItem(queueItem, 0, JSONObject())
    }

    override fun onCastStateChanged(point: Int) {
        //NO_DEVICES_AVAILABLE = 1; NOT_CONNECTED = 2; CONNECTING = 3; CONNECTED = 4;
        Log.i(TAG, "onCastStateChanged: $point")
    }

    public interface OnMainScreenVisibilityListener {
        fun onVisible(isVisible: Boolean)
        fun onZoom(isVisible: Boolean)
        fun onToolInfo(title: String)
    }


}










/* ivChromeCast!!.setOnClickListener {

            //CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
            sec3Timer(customPlayerView)

            val router: MediaRouter = MediaRouter.getInstance(context)
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

            if (mCastSession != null) {
                if (mCastSession!!.isConnected) {
                    Log.i(TAG, "bindSettingPan1: ${mCastSession!!.castDevice!!.friendlyName}")
                    Log.i(
                        TAG,
                        "bindSettingPan: ${mCastContext!!.sessionManager.currentSession!!.isConnected}"
                    )
                    AalertDialogDisconnectCast(cast!!, mCastSession!!)
                } else {
                    remoteMediaClient = mCastSession!!.remoteMediaClient

                    val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)

                    movieMetadata.putString(MediaMetadata.KEY_TITLE, mEpisode.title)
//                  movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mEpisode.)
                    movieMetadata.addImage(WebImage(Uri.parse(mEpisode.thumbnail_url)))
//                  movieMetadata.addImage(WebImage(Uri.parse(mEpisode.getImage(1))))

                    mSelectedMedia = MediaInfo.Builder(mEpisode.media)
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setContentType("videos/mp4")
                        .setMetadata(movieMetadata)
                        .setStreamDuration((customPlayerView.player.duration * 1000).toLong())
                        .build()

//                    remoteMediaClient!!.load(MediaLoadRequestData.Builder().setMediaInfo(mSelectedMedia).build())

                    loadRemoteMedia(0, true)
                    AlertDialogCast(cast!!, isCastRoutes)
                }

            } else {
                mCastContext = CastContext.getSharedInstance(context)
                CastButtonFactory.setUpMediaRouteButton(context!!, mediaRouteButton!!)
                mCastContext!!.addCastStateListener(this)
                mCastContext!!.addAppVisibilityListener(this)
                mCastSession = mCastContext!!.sessionManager.currentCastSession
                mCastContext!!.sessionManager.addSessionManagerListener(
                    this,
                    CastSession::class.java
                )
                AlertDialogCast(cast!!, isCastRoutes)
            }
        }

                ZoomInOut!!.setOnClickListener {
            sec3Timer(customPlayerView)
            val widthDp = context.resources.displayMetrics.run { widthPixels / density }
            val heightDp = context.resources.displayMetrics.run { heightPixels / density }

            if (customPlayerView.player.fullscreen) {
                ZoomInOut!!.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_aspect_ratio
                    )
                )
                mVideoSetting!!.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    setMargins(dpFormat(12), dpFormat(12), dpFormat(12), dpFormat(12))
                }
            } else {
                ZoomInOut!!.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_full_screen
                    )
                )
            }
            customPlayerView.toggleFullscreen()
        }

                mediaRouteButton!!.setOnClickListener {
            CastButtonFactory.setUpMediaRouteButton(context, mediaRouteButton!!)
        }



        private fun AlertDialogCast(
        cast: CastingMenuViewModel,
        listRoutes: List<MediaRouter.RouteInfo>
    ) {
        val dialog = Dialog(context) // where "this" is the context

        val binding: DialogCastBinding =
            DataBindingUtil.inflate(
                dialog.layoutInflater,
                R.layout.dialog_cast,
                null,
                false
            )

        val adapter =
            CastSelectAdapter(context, listRoutes, object : CastSelectAdapter.CastDevicesInterface {
                override fun onConnect(data: MediaRouter.RouteInfo, position: Int) {
                    dialog.dismiss()
                    cast.beginCasting(data)
                }
            })
        binding.adapterCast = adapter

        if (listRoutes.isEmpty()) {
            binding.tvMessage.visibility = VISIBLE
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun AalertDialogDisconnectCast(
        cast: CastingMenuViewModel,
        castSession: CastSession
    ) {
        val dialog = Dialog(context) // where "this" is the context

        val binding: DialogCastBinding =
            DataBindingUtil.inflate(
                dialog.layoutInflater,
                R.layout.dialog_cast,
                null,
                false
            )

        binding.tvCasting.text = "Disconnect the Devices"
        binding.tvDisconnectDevicesName.text = castSession.castDevice!!.friendlyName

        binding.rvCast.visibility = GONE
        binding.tvDisconnect.visibility = VISIBLE
        binding.tvDisconnectDevicesName.visibility = VISIBLE

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

        */