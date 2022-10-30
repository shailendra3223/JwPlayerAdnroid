package com.google.sample.cast.refplayer.listener

import android.content.Context
import android.view.View
import com.google.sample.cast.refplayer.CustomPlayerViewModel

class DoubleTapListener(context: Context, customPlayerView: CustomPlayerViewModel) : View.OnClickListener {
    private var isRunning = false
    private val resetInTime = 500
    private var counter = 0
    private val listener: DoubleTapCallback = context as DoubleTapCallback
    var mCustomPlayerView: CustomPlayerViewModel = customPlayerView

    override fun onClick(v: View) {
        if (isRunning) {
            if (counter == 1) //<-- makes sure that the callback is triggered on double click
                listener.onDoubleClick(v, mCustomPlayerView)
        }
        counter++
        if (!isRunning) {
            isRunning = true
            Thread {
                try {
                    Thread.sleep(resetInTime.toLong())
                    isRunning = false
                    counter = 0
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

}