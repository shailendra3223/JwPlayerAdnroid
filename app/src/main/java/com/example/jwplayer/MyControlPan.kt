package com.example.jwplayer

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.ui.viewmodels.CastingMenuViewModel
import com.jwplayer.pub.ui.viewmodels.PlaylistViewModel
import com.jwplayer.pub.ui.viewmodels.SettingsMenuViewModel

class MyControlPan(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    ConstraintLayout(
        context!!, attrs, defStyleAttr, defStyleRes
    ) {
    private var controlView: CustomPlayerView? = null

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : this(
        context,
        attrs,
        defStyleAttr,
        0
    ) {
    }

    private fun initView(context: Context?) {
        inflate(context, R.layout.view_setting_pan, this)
        controlView = findViewById(R.id.custome_setting_pan)
    }

    fun bindSettingPan(player: JWPlayer, playerConfig: PlayerConfig, lifecycleOwner: LifecycleOwner) {
        // Bind Views
        val settingVM = player.getViewModelForUiGroup(UiGroup.CASTING_MENU) as CastingMenuViewModel
        val playlistViewModel = player.getViewModelForUiGroup(UiGroup.PLAYLIST) as PlaylistViewModel
        val customPlayerView = CustomPlayerViewModel(player)

        controlView!!.bindSettingPan(customPlayerView, settingVM, playlistViewModel, playerConfig, lifecycleOwner)
    }

    init {
        initView(context)
    }
}