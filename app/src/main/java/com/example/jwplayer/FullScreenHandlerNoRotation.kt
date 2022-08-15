package com.example.jwplayer

import android.view.ViewGroup
import com.jwplayer.pub.api.fullscreen.FullscreenHandler
import com.jwplayer.pub.view.JWPlayerView
import java.lang.reflect.Constructor

class FullScreenHandlerNoRotation(view : JWPlayerView) : FullscreenHandler {
    var mPlayerView: JWPlayerView? = view
    private var mDefaultParams: ViewGroup.LayoutParams? = mPlayerView!!.layoutParams
    private var mFullscreenParams: ViewGroup.LayoutParams? = null

    override fun onFullscreenRequested() {
        doFullscreen(true)
    }

    override fun onFullscreenExitRequested() {
        doFullscreen(false)
    }

    override fun onAllowRotationChanged(allowRotation: Boolean) {}

    override fun updateLayoutParams(layoutParams: ViewGroup.LayoutParams?) {}

    override fun setUseFullscreenLayoutFlags(flags: Boolean) {}

    private fun doFullscreen(fullscreen: Boolean) {
        if (fullscreen) {
            mFullscreenParams = fullscreenLayoutParams(mDefaultParams!!)
            mPlayerView!!.layoutParams = mFullscreenParams
        } else {
            mPlayerView!!.layoutParams = mDefaultParams
        }
        mPlayerView!!.requestLayout()
        mPlayerView!!.postInvalidate()
    }


    private fun fullscreenLayoutParams(srcParams: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        val params: ViewGroup.LayoutParams? = try {
            val ctor: Constructor<out ViewGroup.LayoutParams> = srcParams.javaClass.getConstructor(
                ViewGroup.LayoutParams::class.java
            )
            ctor.newInstance(srcParams)
        } catch (e: Exception) {
            ViewGroup.LayoutParams(srcParams)
        }
        params!!.height = ViewGroup.LayoutParams.MATCH_PARENT
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        return params
    }
}
