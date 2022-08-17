package com.example.jwplayer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import com.example.jwplayer.R
import com.jwplayer.pub.ui.viewmodels.SettingsMenuViewModel


import androidx.lifecycle.LifecycleOwner
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.media.adaptive.QualityLevel
import com.jwplayer.pub.ui.viewmodels.PlaylistViewModel

class MyControlPanView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    ConstraintLayout(
        context!!, attrs, defStyleAttr, defStyleRes
    ) {
    private var mVideoSetting: TextView? = null
    private var mEpisodes: TextView? = null
    private var mSubtitle: TextView? = null


    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : this(
        context,
        attrs,
        defStyleAttr,
        0
    ) {
    }

    private fun initView(context: Context?) {
        inflate(context, R.layout.view_all_submenu, this)
        mVideoSetting = findViewById(R.id.tv_video_setting)
        mEpisodes = findViewById(R.id.tv_episode)
        mSubtitle = findViewById(R.id.tv_subtitle)
    }

    @SuppressLint("SetTextI18n")
    fun bindSettingPan(settingMenuViewModel: SettingsMenuViewModel, playlistViewModel: PlaylistViewModel, uiGroup: UiGroup, lifecycleOwner: LifecycleOwner) {
//        settingMenuViewModel.currentPlaybackRate.observe(lifecycleOwner!!) { url: String? ->
//            Picasso.get().load(url).into(poster)
//        }
//        settingMenuViewModel.currentQualityLevel.observe(lifecycleOwner) { string: QualityLevel? ->  }
        settingMenuViewModel.isUiLayerVisible.observe(lifecycleOwner) { visible: Boolean ->
            visibility = if (visible) VISIBLE else GONE
            if (visible) {
                mVideoSetting!!.visibility = VISIBLE
                mEpisodes!!.visibility = VISIBLE
                mSubtitle!!.visibility = VISIBLE
            } else {
                mVideoSetting!!.visibility = GONE
                mEpisodes!!.visibility = GONE
                mSubtitle!!.visibility = GONE
            }
        }
//        settingMenuViewModel.nextUpTimeRemaining.observe(lifecycleOwner) { remaining: Int ->
//            countdown!!.text = "Next in: $remaining"
//        }
        mVideoSetting!!.setOnClickListener { v: View? -> settingMenuViewModel.setSelectedSubmenu(uiGroup) }
        mEpisodes!!.setOnClickListener { v: View? -> Log.i("TAG", "bindSettingPan: ")
            playlistViewModel.open()}
//        setOnClickListener { v: View? -> settingMenuViewModel.setSelectedSubmenu() }
    }

    init {
        initView(context)
    }
}