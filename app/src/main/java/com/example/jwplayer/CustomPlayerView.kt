package com.example.jwplayer

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.mediarouter.media.MediaRouter
import com.example.jwplayer.adapter.CastSelectAdapter
import com.example.jwplayer.adapter.PlayListSeasonAdapter
import com.example.jwplayer.adapter.SelectAdapter
import com.example.jwplayer.databinding.DialogCastBinding
import com.example.jwplayer.databinding.DialogPlayrateSubtitleBinding
import com.example.jwplayer.databinding.FragmentPlayListSeasonBinding
import com.example.jwplayer.model.SelectItem
import com.google.android.gms.cast.CastDevice
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
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


class CustomPlayerView(
    mContext: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ConstraintLayout(
    mContext!!, attrs, defStyleAttr, defStyleRes
), SelectAdapter.SelectItemInterface, SessionManagerListener<CastSession> {
    private var mVideoSetting: TextView? = null
    private var mEpisodes: TextView? = null
    private var mSubtitleAudio: TextView? = null
    private var mNextEpisode: TextView? = null
    private var mTitle: TextView? = null
    private var tvTime: TextView? = null
    private var contentSeekBar: SeekBar? = null
    private var playToggle: ImageView? = null

    //    private var fullscreenToggle: ImageView? = null
    private var ZoomInOut: ImageView? = null
    private var ivChromeCast: ImageView? = null
    private var ivFastForward15: ImageView? = null
    private var ivFastBackward15: ImageView? = null

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : this(
        context,
        attrs,
        defStyleAttr,
        0
    ) {
    }

    private fun initView(context: Context?) {
        inflate(context, R.layout.view_custom_player_ui, this)
        mVideoSetting = findViewById(R.id.tv_video_setting)
        mEpisodes = findViewById(R.id.tv_episode)
        mSubtitleAudio = findViewById(R.id.tv_subtitle)
        mNextEpisode = findViewById(R.id.tv_next_episode)
        contentSeekBar = findViewById(R.id.seekbar)
        playToggle = findViewById(R.id.play_pause_toggle)
//      fullscreenToggle = findViewById(R.id.iv_exit_fullscreen)
        mTitle = findViewById(R.id.tv_title)
        ZoomInOut = findViewById(R.id.iv_zoom_in_out)
        ivChromeCast = findViewById(R.id.iv_chrome_cast)
        ivFastForward15 = findViewById(R.id.tv_fast_forward_15)
        ivFastBackward15 = findViewById(R.id.tv_fast_backward_15)
        tvTime = findViewById(R.id.tv_time)
    }

    @SuppressLint("SetTextI18n")
    fun bindSettingPan(
        customPlayerView: CustomPlayerViewModel,
        playlistViewModel: PlaylistViewModel,
        playerConfig: PlayerConfig,
        lifecycleOwner: LifecycleOwner,
        data: ModelClass
    ) {
        val mCastContext = CastContext.getSharedInstance(context)
        val cast =
            customPlayerView.player.getViewModelForUiGroup(UiGroup.CASTING_MENU) as CastingMenuViewModel
        customPlayerView.isFirstFrame.observe(lifecycleOwner) { mJWPlayer: JWPlayer ->
            mTitle!!.text = mJWPlayer.playlistItem.mTitle
        }

        customPlayerView.printTime.observe(lifecycleOwner) {
            tvTime!!.text = it
        }

        customPlayerView.isVisibility.observe(lifecycleOwner) {

            sec3Timer(customPlayerView)

//            if (it) {
//                sec3Timer(customPlayerView)
//            } else {
//                visibilityComponents(GONE)
////                customPlayerView.disableTouch.value = false
//            }
        }

        mVideoSetting!!.setOnClickListener { v: View? ->
//            customPlayerView.isVisibility.value = false
//            customPlayerView.disableTouch.value = true
//            customPlayerView.player.pause()
            visibilityComponents(GONE)
            AlertDialogPlayer(customPlayerView, 1001)
        }

        mSubtitleAudio!!.setOnClickListener { v: View? ->
//            customPlayerView.isVisibility.value = false
//            customPlayerView.disableTouch.value = true
//            customPlayerView.player.pause()
            visibilityComponents(GONE)
            AlertDialogPlayer(customPlayerView, 1003)
        }

        ZoomInOut!!.setOnClickListener {
            //TODO Shailendre on this button click the video should zoom in n out
//            customPlayerView.isVisibility.value = false
//            customPlayerView.disableTouch.value = true
//
//            val mPosition = customPlayerView.player.position
//            Log.i("TAG", "IOKLL1: ${mPosition}")
//            val enlarge =
//                if (customPlayerView.player.config.mStretching == PlayerConfig.STRETCHING_FILL) {
//                    PlayerConfig.STRETCHING_UNIFORM
//                } else {
//                    PlayerConfig.STRETCHING_EXACT_FIT
//                }
//            customPlayerView.player.setup(
//                PlayerConfig.Builder()
//                    .playlist(playerConfig.playlist)
//                    .uiConfig(playerConfig.mUiConfig)
//                    .stretching(enlarge)
//                    .autostart(true)
//                    .build()
//            )
            if (customPlayerView.player.fullscreen) {
                customPlayerView.player.setFullscreen(true, false)
            } else {
                customPlayerView.player.setFullscreen(false, false)
            }
//            customPlayerView.player.setFullscreen(true)

        }

        mEpisodes!!.setOnClickListener {
            visibilityComponents(GONE)
//            customPlayerView.isVisibility.value = false
//            customPlayerView.disableTouch.value = true
            AlertDialogPlayList(customPlayerView, data[0].seasons, playerConfig)
        }

        ivChromeCast!!.setOnClickListener {
            customPlayerView.isVisibility.value = false
            customPlayerView.disableTouch.value = true

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

            AlertDialogCast(cast, isCastRoutes)
            mCastContext.sessionManager.addSessionManagerListener(this, CastSession::class.java)
        }

        mNextEpisode!!.setOnClickListener {
            val next = customPlayerView.player.playlistIndex + 1
            if (next != customPlayerView.player.playlist.size) {
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
            contentSeekBar!!.progress = progress
        }

        customPlayerView.isSeekbarVisible.observe(lifecycleOwner) { isVisible ->
            contentSeekBar!!.visibility = if (isVisible) VISIBLE else GONE
            tvTime!!.visibility = if (isVisible) VISIBLE else GONE
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
        playToggle!!.setOnClickListener { v: View? -> customPlayerView.togglePlay() }

//        customPlayerView.isFullscreen.observe(lifecycleOwner) { isFullscreen ->
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
        visibilityComponents(VISIBLE)
        countDown?.cancel()
        countDown = object : CountDownTimer(VisibilityTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("TAG", "starting nuclear in " + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                visibilityComponents(GONE)
                customPlayerView.disableTouch.value = false
            }
        }.start()
    }

    private fun visibilityComponents(isVisible: Int) {
        mVideoSetting!!.visibility = isVisible
        mEpisodes!!.visibility = isVisible
        mSubtitleAudio!!.visibility = isVisible
        mTitle!!.visibility = isVisible
        contentSeekBar!!.visibility = isVisible
        tvTime!!.visibility = isVisible
        playToggle!!.visibility = isVisible
//        fullscreenToggle!!.visibility = isVisible
        ZoomInOut!!.visibility = isVisible
        ivChromeCast!!.visibility = isVisible
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
            binding.adapterPlayRate = setSubtitle(subtitleList)
            binding.rvQualityLevel.visibility = GONE
            binding.tvQualityLevel.visibility = GONE
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

            if(item.label.toString().contains("Auto")) {
                list.add(SelectItem(0.0, item, null, null, isSelect))
            } else if(item.label.toString().contains("1080p") && isFHD) {
                isFHD = false
                list.add(SelectItem(0.0, item, null, null, isSelect))
            } else if(item.label.toString().contains("720p") && isHD) {
                isHD = false
                list.add(SelectItem(0.0, item, null, null, isSelect))
            } else if(item.label.toString().contains("360p") && isSD) {
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

    init {
        initView(mContext)
    }

    var mValuePlayRate: Double = 0.0
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

    var positionSeason = 1
    private fun AlertDialogPlayList(
        customPlayerView: CustomPlayerViewModel,
        season: List<Season>,
        playerConfig: PlayerConfig
    ) {
        val dialog = Dialog(
            context,
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

                            customPlayerView.player.setup(
                                PlayerConfig.Builder()
                                    .playlist(playlist)
                                    .playlistIndex(posVideo)
                                    .uiConfig(playerConfig.mUiConfig)
                                    .autostart(true)
                                    .build()
                            )

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

    override fun onSessionEnded(p0: CastSession, p1: Int) {
        Log.i("TAG", "onSessionEnded")
    }

    override fun onSessionEnding(p0: CastSession) {
        Log.i("TAG", "onSessionEnding")
    }

    override fun onSessionResumeFailed(p0: CastSession, p1: Int) {
        Log.i("TAG", "onSessionResumeFailed")
    }

    override fun onSessionResumed(p0: CastSession, p1: Boolean) {
        Log.i("TAG", "onSessionResumed")
    }

    override fun onSessionResuming(p0: CastSession, p1: String) {
        Log.i("TAG", "onSessionResuming")
    }

    override fun onSessionStartFailed(p0: CastSession, p1: Int) {
        Log.i("TAG", "onSessionStartFailed")
    }

    override fun onSessionStarted(p0: CastSession, p1: String) {
        Log.i("TAG", "onSessionStarted")
    }

    override fun onSessionStarting(p0: CastSession) {
        Log.i("TAG", "onSessionStarting")
    }

    override fun onSessionSuspended(p0: CastSession, p1: Int) {
        Log.i("TAG", "onSessionSuspended")
    }

//    public class MyMediaIntentReceiver : MediaIntentReceiver() {
//        override fun onReceiveOtherAction(action: String, intent: Intent) {
//
//        }
//    }
}