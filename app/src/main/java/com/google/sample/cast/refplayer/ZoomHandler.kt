package com.google.sample.cast.refplayer

import android.util.Log
import android.view.ViewGroup
import com.jwplayer.pub.api.fullscreen.FullscreenHandler
import com.jwplayer.pub.view.JWPlayerView
import java.lang.reflect.Constructor

open class ZoomHandler(var mPlayerView: JWPlayerView) : FullscreenHandler {
    var mDefaultParams: ViewGroup.LayoutParams = mPlayerView.layoutParams
    var mFullscreenParams: ViewGroup.LayoutParams? = null

    override fun onFullscreenRequested() {

        Log.i("TAG", "gIOKLL1 onFullscreenRequested:")
        doFullscreen(true)
    }

    override fun onFullscreenExitRequested() {

        Log.i("TAG", "gIOKLL1 onFullscreenExitRequested:")
        doFullscreen(false)
    }

    private var widthDp = 0.0f
    private var heightDp = 0.0f
    override fun onAllowRotationChanged(allowRotation: Boolean) {}
    override fun updateLayoutParams(layoutParams: ViewGroup.LayoutParams) {}
    override fun setUseFullscreenLayoutFlags(flags: Boolean) {}
    private fun doFullscreen(fullscreen: Boolean) {

        widthDp = mPlayerView.resources.displayMetrics.run { widthPixels / density }
        heightDp = mPlayerView.resources.displayMetrics.run { heightPixels / density }



        Log.i("TAG", "gIOKLL11 doFullscreen: ${widthDp.toString()} ${heightDp.toString()}")

        if (fullscreen) {
            mFullscreenParams = fullscreenLayoutParams(mDefaultParams)
            mPlayerView.layoutParams = mFullscreenParams
        } else {
            mPlayerView.layoutParams = mDefaultParams
        }
        mPlayerView.requestLayout()
        mPlayerView.postInvalidate()

        Log.i("TAG", "gIOKLL1 doFullscreen: ${mFullscreenParams.toString()} ${mDefaultParams.toString()}")
    }

    /**
     * Creates a clone of srcParams with the width and height set to MATCH_PARENT.
     *
     * @param srcParams
     * @return LayoutParams in fullscreen.
     */
    private fun fullscreenLayoutParams(srcParams: ViewGroup.LayoutParams): ViewGroup.LayoutParams? {
        var params: ViewGroup.LayoutParams? = null
        params = try {
            val ctor: Constructor<out ViewGroup.LayoutParams> =
                srcParams.javaClass.getConstructor(ViewGroup.LayoutParams::class.java)
            ctor.newInstance(srcParams)
        } catch (e: Exception) {
            ViewGroup.LayoutParams(srcParams)
        }
//        if (widthDp == 0.0f || heightDp == 0.0f) {
//            params!!.height = ViewGroup.LayoutParams.MATCH_PARENT
//            params.width = ViewGroup.LayoutParams.MATCH_PARENT
//        } else {
//            params!!.height = heightDp.toInt()
//            params.width = widthDp.toInt()
//        }
        params!!.height = ViewGroup.LayoutParams.MATCH_PARENT
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        Log.i("TAG", "gIOKLL1 fullscreenLayoutParams: ${params!!.height} ${ViewGroup.LayoutParams.MATCH_PARENT}")
        return params
    }

}