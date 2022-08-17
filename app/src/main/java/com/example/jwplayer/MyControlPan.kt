package com.example.jwplayer

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.jwplayer.pub.api.JWPlayer
import androidx.lifecycle.LifecycleOwner
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.ui.viewmodels.PlaylistViewModel
import com.jwplayer.pub.ui.viewmodels.SettingsMenuViewModel

class MyControlPan(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    ConstraintLayout(
        context!!, attrs, defStyleAttr, defStyleRes
    ) {
    private var controlView: MyControlPanView? = null

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

    fun bindSettingPan(player: JWPlayer, uiGroup: UiGroup, lifecycleOwner: LifecycleOwner) {
        // Bind Views
        val nextUpVM = player.getViewModelForUiGroup(UiGroup.SETTINGS_MENU) as SettingsMenuViewModel
        val playlistViewModel = player.getViewModelForUiGroup(UiGroup.PLAYLIST) as PlaylistViewModel
        controlView!!.bindSettingPan(nextUpVM, playlistViewModel, uiGroup, lifecycleOwner)
    }

    init {
        initView(context)
    }
}