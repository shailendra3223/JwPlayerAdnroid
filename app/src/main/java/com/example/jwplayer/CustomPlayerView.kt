package com.example.jwplayer

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.mediarouter.media.MediaRouter
import com.example.jwplayer.adapter.CastSelectAdapter
import com.example.jwplayer.adapter.SelectAdapter
import com.example.jwplayer.databinding.DialogPlayrateSubtitleBinding
import com.example.jwplayer.model.CastSelectItem
import com.example.jwplayer.model.SelectItem
import com.google.android.gms.cast.CastDevice
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.media.adaptive.QualityLevel
import com.jwplayer.pub.api.media.audio.AudioTrack
import com.jwplayer.pub.api.media.captions.Caption
import com.jwplayer.pub.ui.viewmodels.CastingMenuViewModel
import com.jwplayer.pub.ui.viewmodels.PlaylistViewModel


class CustomPlayerView(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) :
    ConstraintLayout(
        context!!, attrs, defStyleAttr, defStyleRes
    ), SelectAdapter.SelectItemInterface, CastSelectAdapter.CastDevicesInterface {
    private var mVideoSetting: TextView? = null
    private var mEpisodes: TextView? = null
    private var mSubtitleAudio: TextView? = null
    private var mNextEpisode: TextView? = null
    private var mTitle: TextView? = null
    private var contentSeekBar: SeekBar? = null
    private var playToggle: ImageView? = null
    //    private var fullscreenToggle: ImageView? = null
    private var ZoomInOut: ImageView? = null
    private var ivChromeCast: ImageView? = null
    private var ivFastForward15: ImageView? = null
    private var ivFastBackward15: ImageView? = null
//    private var clParentView: View? = null

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
//        fullscreenToggle = findViewById(R.id.iv_exit_fullscreen)
        mTitle = findViewById(R.id.tv_title)
        ZoomInOut = findViewById(R.id.iv_zoom_in_out)
        ZoomInOut!!.visibility = GONE
        ivChromeCast = findViewById(R.id.iv_chrome_cast)
        ivFastForward15 = findViewById(R.id.tv_fast_forward_15)
        ivFastBackward15 = findViewById(R.id.tv_fast_backward_15)
//        clParentView = findViewById(R.id.cl_parent_view)
    }

    @SuppressLint("SetTextI18n")
    fun bindSettingPan(
        customPlayerView: CustomPlayerViewModel,
        settingMenuViewModel: CastingMenuViewModel,
        playlistViewModel: PlaylistViewModel,
        playerConfig: PlayerConfig,
        lifecycleOwner: LifecycleOwner
    ) {

        customPlayerView.isFirstFrame.observe(lifecycleOwner) { mJWPlayer: JWPlayer ->
            mTitle!!.text = mJWPlayer.playlistItem.mTitle
            visibilityComponents(GONE)
        }

        customPlayerView.isVisibility.observe(lifecycleOwner) {
            if (it) {
                sec3Timer()
            } else {
                visibilityComponents(GONE)
            }
        }


//        settingMenuViewModel.currentPlaybackRate.observe(lifecycleOwner!!) { url: String? ->
//            Picasso.get().load(url).into(poster)
//        }
//        settingMenuViewModel.currentQualityLevel.observe(lifecycleOwner) { string: QualityLevel? ->  }

//        settingMenuViewModel.nextUpTimeRemaining.observe(lifecycleOwner) { remaining: Int ->
//            countdown!!.text = "Next in: $remaining"
//        }
        mVideoSetting!!.setOnClickListener { v: View? ->
            sec3Timer()
            customPlayerView.player.pause()
            AlertDialogPlayer(customPlayerView, 1001)
        }

        mSubtitleAudio!!.setOnClickListener { v: View? ->
            sec3Timer()
            customPlayerView.player.pause()
            AlertDialogPlayer(customPlayerView, 1003)
        }

        ZoomInOut!!.setOnClickListener {
            sec3Timer()
            visibilityComponents(GONE)

            val enlarge =
                if (customPlayerView.player.config.mStretching == PlayerConfig.STRETCHING_FILL) {
                    PlayerConfig.STRETCHING_UNIFORM
                } else {
                    PlayerConfig.STRETCHING_FILL
                }
            customPlayerView.player.setup(
                PlayerConfig.Builder()
                    .playlist(playerConfig.playlist)
                    .uiConfig(playerConfig.mUiConfig)
                    .stretching(enlarge)
                    .autostart(true)
                    .build()
            )
//            customPlayerView.player.setP
//            customPlayerView.player.duration
        }

        mEpisodes!!.setOnClickListener {
            Log.i("TAG", "mEpisodes: ")
            customPlayerView.disableTouch = true
            customPlayerView.isVisibility.value = false
            visibilityComponents(GONE)
            Log.i("TAG", "mEpisodes: ${customPlayerView.disableTouch}")

            playlistViewModel.open()
        }

        ivChromeCast!!.setOnClickListener {
            sec3Timer()
            customPlayerView.isVisibility.value = false
            customPlayerView.disableTouch = true

            val router : MediaRouter = MediaRouter.getInstance(context)
            val mRoutes : List<MediaRouter.RouteInfo> =  router.routes
            val isCastRoutes = ArrayList<MediaRouter.RouteInfo>()
            val devices = ArrayList<CastDevice>()

            for (routeInfo in mRoutes) {
                val device = CastDevice.getFromBundle(routeInfo.extras)
                if (device != null) {
                    devices.add(device)
                    isCastRoutes.add(routeInfo)
                }
            }

//            val cast = customPlayerView.player.getViewModelForUiGroup(UiGroup.CASTING_MENU) as CastingMenuViewModel
//            Log.i("TAG", "bindSettingPoan: ${devices[0]}")
//            Log.i("TAG", "bindSettingPoan: ${mRoutes[1]}")
//            cast.availableDevices?.value?.get(0)


            AlertDialogCast(customPlayerView, isCastRoutes)
//            Log.i("TAG", "bindSettingPoan: ${cast.beginCasting(
//                cast?.availableDevices?.value?.get(0))}")
//            if (customPlayerView.onChromeCast().availableDevices.value!!.size > 0) {
//
//            }

        }

        mNextEpisode!!.setOnClickListener {
            Log.i("TAG", "mNextEpisode: ${customPlayerView.disableTouch}")
            sec3Timer()
            val next = customPlayerView.player.playlistIndex + 1
            if (next != customPlayerView.player.playlist.size) {
                customPlayerView.player.playlistItem(next)
            } else {
//                Toast.makeText(this, "Last Video", Toast.LENGTH_LONG).show()
            }
        }

        ivFastForward15!!.setOnClickListener {
            sec3Timer()
            Log.i("TAG", "bindSettingPanff: ${customPlayerView.player.position}")
            val position = customPlayerView.player.position + 15

            if (position >= customPlayerView.player.duration) {
                val next = customPlayerView.player.playlistIndex + 1
                if (next != customPlayerView.player.playlist.size) {
                    customPlayerView.player.playlistItem(next)
                }
            } else if (position >= 0) {
                customPlayerView.player.seek(position)
                customPlayerView.handleTimeUpdate(position, customPlayerView.player.duration, customPlayerView.contentProgressPercentage)
            }
        }

        ivFastBackward15!!.setOnClickListener {
            sec3Timer()
            val position = customPlayerView.player.position - 15
            Log.i("TAG", "bindSettingPanff: $position")
            Log.i("TAG", "bindSettingPanff: ${customPlayerView.player.position}")
            if (position > 16) {
                customPlayerView.player.seek(position)
                customPlayerView.handleTimeUpdate(position, customPlayerView.player.duration, customPlayerView.contentProgressPercentage)
            } else {
                customPlayerView.player.seek(0.1)
                customPlayerView.handleTimeUpdate(0.1, customPlayerView.player.duration, customPlayerView.contentProgressPercentage)
            }
        }


        //SeekBar
        customPlayerView.contentProgressPercentage.observe(lifecycleOwner) { progress ->
            customPlayerView.disableTouch = false
            contentSeekBar!!.progress = progress
        }
        customPlayerView.isSeekbarVisible.observe(lifecycleOwner) { isVisible ->
            Log.i("TAG", "bindSettingPan isSeekbarVisible: $isVisible ")
            contentSeekBar!!.visibility = if (isVisible) VISIBLE else GONE
        }
        contentSeekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                Log.i("TAG", "bindSettingPan setOnSeekBarChangeListener: $fromUser $progress")
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
    private fun sec3Timer() {
        visibilityComponents(VISIBLE)
        countDown?.cancel()
        countDown = object : CountDownTimer(VisibilityTime.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //5 ,4 , 3, 2, 1
                Log.d("TAG", "starting nuclear in " + millisUntilFinished / 1000)
            }

            override fun onFinish() {
                visibilityComponents(GONE)
                //finish code
            }
        }.start()
    }

    private fun visibilityComponents(isVisible: Int) {
        mVideoSetting!!.visibility = isVisible
        mEpisodes!!.visibility = isVisible
        mSubtitleAudio!!.visibility = isVisible
        mTitle!!.visibility = isVisible
        contentSeekBar!!.visibility = isVisible
        playToggle!!.visibility = isVisible
//        fullscreenToggle!!.visibility = isVisible
//        ZoomInOut!!.visibility = isVisible
        ivChromeCast!!.visibility = isVisible
        mNextEpisode!!.visibility = isVisible
        ivFastForward15!!.visibility = isVisible
        ivFastBackward15!!.visibility = isVisible
    }

    private fun AlertDialogPlayer(customPlayerView: CustomPlayerViewModel, FLAG: Int) {
        val dialog = Dialog(context) // where "this" is the context

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
            Log.i("TAG", "AlertDialogPlayer: ${customPlayerView.player.captionsList.size}")
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
                    customPlayerView.player.currentCaptions = mPositionSubTitle
                }
            }
            customPlayerView.player.play()
            dialog.dismiss()
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun setPlayRateData(): SelectAdapter {
        val list = ArrayList<SelectItem>()
        val playbackRates = doubleArrayOf(0.5, 1.0, 1.5, 2.0)
        for (item in playbackRates) {
            list.add(SelectItem(item, null, null, null, false))
        }
        return SelectAdapter(context, list, 1001, this)
    }

    private fun setQualityLevel(qualityLevel: ArrayList<QualityLevel>): SelectAdapter {
        val list = ArrayList<SelectItem>()
        for (item in qualityLevel) {
            list.add(SelectItem(0.0, item, null, null, false))
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
            list.add(SelectItem(0.0, null, null, item, false))
        }
        return SelectAdapter(context, list, 1004, this)
    }

    init {
        initView(context)
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

    override fun onConnect(data: MediaRouter.RouteInfo, cast: CastingMenuViewModel,position: Int) {
        cast.beginCasting(data)
    }

    private fun AlertDialogCast(customPlayerView: CustomPlayerViewModel, listRoutes : List<MediaRouter.RouteInfo>) {
        val dialog = Dialog(context) // where "this" is the context

        val binding: DialogPlayrateSubtitleBinding =
            DataBindingUtil.inflate(
                dialog.layoutInflater,
                R.layout.dialog_playrate_subtitle,
                null,
                false
            )

        binding.rvPlayRate.visibility = GONE
        binding.rvQualityLevel.visibility = GONE
        binding.tvPlayRate.visibility = GONE
        binding.tvQualityLevel.visibility = GONE
        binding.rvCast.visibility = VISIBLE
        binding.tvCasting.visibility = VISIBLE

        binding.tvDone.text = context.getString(R.string.cancel)
        val adapter = CastSelectAdapter(context, listRoutes, customPlayerView, this)
        binding.adapterCast = adapter

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvDone.setOnClickListener {
            customPlayerView.player.play()
            dialog.dismiss()
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }
}