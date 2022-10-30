package com.google.sample.cast.refplayer.listener

import android.view.View
import com.google.sample.cast.refplayer.CustomPlayerViewModel

interface DoubleTapCallback {
    fun onDoubleClick(v: View?, customPlayerView: CustomPlayerViewModel)
}