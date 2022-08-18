package com.example.jwplayer


import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.configuration.UiConfig
import com.jwplayer.pub.ui.viewmodels.CastingMenuViewModel
import com.jwplayer.pub.ui.viewmodels.PlaylistViewModel
import com.jwplayer.pub.ui.viewmodels.SettingsMenuViewModel


class CustomPlayerView(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) :
    ConstraintLayout(
        context!!, attrs, defStyleAttr, defStyleRes
    ) {
    private var mVideoSetting: TextView? = null
    private var mEpisodes: TextView? = null
    private var mSubtitle: TextView? = null
    private var mNextEpisode: TextView? = null
    private var mTitle: TextView? = null
    private var contentSeekBar: SeekBar? = null
    private var playToggle: ImageView? = null
    private var fullscreenToggle: ImageView? = null
    private var ZoomInOut: ImageView? = null
    private var ivChromeCast: ImageView? = null
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
        mSubtitle = findViewById(R.id.tv_subtitle)
        mNextEpisode = findViewById(R.id.tv_next_episode)
        contentSeekBar = findViewById(R.id.seekbar)
        playToggle = findViewById(R.id.play_pause_toggle)
        fullscreenToggle = findViewById(R.id.iv_exit_fullscreen)
        mTitle = findViewById(R.id.tv_title)
        ZoomInOut = findViewById(R.id.iv_zoom_in_out)
        ivChromeCast = findViewById(R.id.iv_chrome_cast)
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
        }

        customPlayerView.isVisibility.observe(lifecycleOwner) {
            Log.d("VIETAGL", "Firs${it}")
            if (it) {
                visibilityComponents(VISIBLE)
                sec3Timer(customPlayerView)
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
//            settingMenuViewModel.setSelectedSubmenu(
//                uiGroup
//            )
        }

        ZoomInOut!!.setOnClickListener { v: View? ->


            val enlarge = if (customPlayerView.player.config.mStretching == PlayerConfig.STRETCHING_FILL) {
                PlayerConfig.STRETCHING_UNIFORM
            } else {
                PlayerConfig.STRETCHING_FILL
            }
            customPlayerView.player.setup(PlayerConfig.Builder()
                .playlist(playerConfig.playlist)
                .uiConfig(playerConfig.mUiConfig)
                .stretching(enlarge)
                .autostart(true)
                .build())
//            customPlayerView.player.setP
//            customPlayerView.player.duration
        }

        mEpisodes!!.setOnClickListener { v: View? ->
            Log.i("TAG", "mEpisodes: ")
            customPlayerView.disableTouch = true
            customPlayerView.isVisibility.value = false
            visibilityComponents(GONE)
            Log.i("TAG", "mEpisodes: ${customPlayerView.disableTouch}")

            playlistViewModel.open()
        }
//        setOnClickListener { v: View? -> settingMenuViewModel.setSelectedSubmenu() }
        ivChromeCast!!.setOnClickListener { v: View? ->
            Log.i("TAG", "bindSettingPan: ")
            customPlayerView.isVisibility.value = false
            customPlayerView.disableTouch = true
            Log.i("TAG", "bindSettingPan: ${customPlayerView.disableTouch}")

//            customPlayerView.onChromeCast().beginCasting()

        }

        mNextEpisode!!.setOnClickListener { v: View? ->
//            Log.i("TAG", "bindSettingPan: ")
//            customPlayerView.disableTouch = true
//            Log.i("TAG", "bindSettingPan: ${customPlayerView.disableTouch}")
//            playlistViewModel.open()
            customPlayerView.player.next()
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
        playToggle!!.setOnClickListener(OnClickListener { v: View? -> customPlayerView.togglePlay() })

        customPlayerView.isFullscreen.observe(lifecycleOwner) { isFullscreen ->
            fullscreenToggle!!.setImageDrawable(
                if (isFullscreen) AppCompatResources.getDrawable(
                    context,
                    R.drawable.ic_jw_exit_fullscreen
                )
                else AppCompatResources.getDrawable(context, R.drawable.ic_jw_enter_fullscreen)
            )
        }
//        fullscreenToggle!!.setOnClickListener(OnClickListener { v: View? -> customPlayerView.toggleFullscreen() })
    }

    var countDown: CountDownTimer? = null
    private fun sec3Timer(customPlayerView: CustomPlayerViewModel) {
        countDown?.cancel()
        countDown = object : CountDownTimer(2000, 1000) {
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
        mSubtitle!!.visibility = isVisible
        mTitle!!.visibility = isVisible
        contentSeekBar!!.visibility = isVisible
        playToggle!!.visibility = isVisible
        fullscreenToggle!!.visibility = isVisible
        ZoomInOut!!.visibility = isVisible
        ivChromeCast!!.visibility = isVisible
        mNextEpisode!!.visibility = isVisible
    }

    private fun AlertDialogCast(isVisible: Int) {
//        val builder = AlertDialog.Builder(context,R.style.CustomAlertDialog).create()
//        val view = context.layoutInflater.inflate(R.layout.customView_layout,null)
//        val  button = view.findViewById<Button>(R.id.dialogDismiss_button)
//        builder.setView(view)
//        button.setOnClickListener {
//            builder.dismiss()
//        }
//        builder.setCanceledOnTouchOutside(false)
//        builder.show()
    }

    private fun alertDialog(context:Context, title:String, msg:String,positiveMethod: () -> Unit= {}){
        val dialogBuilder = MaterialAlertDialogBuilder(context)

        // set message of alert dialog
        dialogBuilder.setMessage(msg)

            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Done") { dialog, id ->
                dialog.cancel()
                positiveMethod()
            }
            // negative button text and action
            .setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle(title)
        // show alert dialog
        alert.show()
    }


    init {
        initView(context)
    }
}