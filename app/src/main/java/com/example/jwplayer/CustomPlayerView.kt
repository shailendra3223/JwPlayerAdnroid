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
import com.example.jwplayer.databinding.DialogCastBinding
import com.example.jwplayer.databinding.DialogPlayrateSubtitleBinding
import com.example.jwplayer.model.SelectItem
import com.google.android.gms.cast.CastDevice
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.MediaIntentReceiver
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
//        fullscreenToggle = findViewById(R.id.iv_exit_fullscreen)
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
        lifecycleOwner: LifecycleOwner
    ) {
        val mCastContext = CastContext.getSharedInstance(context)
        val cast = customPlayerView.player.getViewModelForUiGroup(UiGroup.CASTING_MENU) as CastingMenuViewModel
        customPlayerView.isFirstFrame.observe(lifecycleOwner) { mJWPlayer: JWPlayer ->
            mTitle!!.text = mJWPlayer.playlistItem.mTitle
        }

        customPlayerView.printTime.observe(lifecycleOwner) {
            tvTime!!.text = it
        }

        customPlayerView.isVisibility.observe(lifecycleOwner) {
            if (it && customPlayerView.disableTouch.value != true) {
                sec3Timer(customPlayerView)
            } else {
                visibilityComponents(GONE)
                customPlayerView.disableTouch.value = false
            }
            Log.i("TAG", "GIOLL2: ${mEpisodes!!.visibility}")
        }

        mVideoSetting!!.setOnClickListener { v: View? ->
            customPlayerView.isVisibility.value = false
            customPlayerView.disableTouch.value = true
            customPlayerView.player.pause()
            AlertDialogPlayer(customPlayerView, 1001)
        }

        mSubtitleAudio!!.setOnClickListener { v: View? ->
            customPlayerView.isVisibility.value = false
            customPlayerView.disableTouch.value = true
            customPlayerView.player.pause()
            AlertDialogPlayer(customPlayerView, 1003)
        }

        ZoomInOut!!.setOnClickListener {
            //TODO Shailendre on this button click the video should zoom in n out
            customPlayerView.isVisibility.value = false
            customPlayerView.disableTouch.value = true

            val mPosition = customPlayerView.player.position
            Log.i("TAG", "IOKLL1: ${mPosition}")
            val enlarge =
                if (customPlayerView.player.config.mStretching == PlayerConfig.STRETCHING_FILL) {
                    PlayerConfig.STRETCHING_UNIFORM
                } else {
                    PlayerConfig.STRETCHING_EXACT_FIT
                }
            customPlayerView.player.setup(
                PlayerConfig.Builder()
                    .playlist(playerConfig.playlist)
                    .uiConfig(playerConfig.mUiConfig)
                    .stretching(enlarge)
                    .autostart(true)
                    .build()
            )
            customPlayerView.player.seek(mPosition)

            Log.i("TAG", "IOKLL2: ${customPlayerView.player.position}")
        }

        mEpisodes!!.setOnClickListener {
            customPlayerView.isVisibility.value = false
            customPlayerView.disableTouch.value = true
            Log.i("TAG", "GIOLL1: ${mEpisodes!!.visibility}")
            playlistViewModel.open()
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

//            CastStateListener mCastStateListener
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

        ivFastForward15!!.setOnClickListener {
            sec3Timer(customPlayerView)
            val position = customPlayerView.player.position + 15

            if (position >= customPlayerView.player.duration) {
                val next = customPlayerView.player.playlistIndex + 1
                if (next != customPlayerView.player.playlist.size) {
                    customPlayerView.player.playlistItem(next)
                }
            } else if (position >= 0) {
                customPlayerView.player.seek(position)
                customPlayerView.handleTimeUpdate(
                    position,
                    customPlayerView.player.duration,
                    customPlayerView.contentProgressPercentage
                )
            }
        }

        ivFastBackward15!!.setOnClickListener {
            sec3Timer(customPlayerView)
            val position = customPlayerView.player.position - 15
            if (position > 16) {
                customPlayerView.player.seek(position)
                customPlayerView.handleTimeUpdate(
                    position,
                    customPlayerView.player.duration,
                    customPlayerView.contentProgressPercentage
                )
            } else {
                customPlayerView.player.seek(0.1)
                customPlayerView.handleTimeUpdate(
                    0.1,
                    customPlayerView.player.duration,
                    customPlayerView.contentProgressPercentage
                )
            }
        }


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
        ivFastForward15!!.visibility = isVisible
        ivFastBackward15!!.visibility = isVisible
    }

    private fun AlertDialogPlayer(customPlayerView: CustomPlayerViewModel, FLAG: Int) {
        val dialog = Dialog(context)
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

        binding.tvDone.text = context.getString(R.string.cancel)
        val adapter =
            CastSelectAdapter(context, listRoutes, object : CastSelectAdapter.CastDevicesInterface {
                override fun onConnect(data: MediaRouter.RouteInfo, position: Int) {
                    dialog.dismiss()
                    cast.beginCasting(data)
                }
            })
        binding.adapterCast = adapter

        if(listRoutes.isEmpty()) {
            binding.tvMessage.visibility = VISIBLE
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvDone.setOnClickListener {
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