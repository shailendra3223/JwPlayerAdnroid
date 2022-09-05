package com.google.sample.cast.refplayer.cast

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import com.google.android.gms.cast.framework.media.uicontroller.UIMediaController

class CustomUIMediaController(activity: Activity) : UIMediaController(activity) {

    private val TAG = CustomUIMediaController::class.java.simpleName


    override fun onForwardClicked(view: View, l: Long) {
        super.onForwardClicked(view!!, l)
    }


    override fun onRewindClicked(view: View, l: Long) {
        super.onRewindClicked(view!!, l)
    }

    override fun onSeekBarProgressChanged(seekBar: SeekBar, progress: Int, fromuserb: Boolean) {
        super.onSeekBarProgressChanged(seekBar!!, progress, fromuserb)
    }

    override fun onSeekBarStartTrackingTouch(seekBar: SeekBar) {
        super.onSeekBarStartTrackingTouch(seekBar!!)
    }

    override fun onSeekBarStopTrackingTouch(seekBar: SeekBar) {
        super.onSeekBarStopTrackingTouch(seekBar!!)
    }

    override fun onPlayPauseToggleClicked(imageView: ImageView) {
        super.onPlayPauseToggleClicked(imageView!!)
    }

    override fun onClosedCaptionClicked(view: View) {
        super.onClosedCaptionClicked(view!!)
    }

    override fun onMuteToggleClicked(imageView: ImageView) {
        super.onMuteToggleClicked(imageView!!)
    }
}
