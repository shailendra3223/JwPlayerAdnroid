package com.google.sample.cast.refplayer.cast

import android.view.View
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.cast.framework.media.uicontroller.UIController

class CustomUIController(var1: View)  : UIController() {
    private var view = var1
    private val remoteMediaClient: RemoteMediaClient? = null

    override fun getRemoteMediaClient(): RemoteMediaClient? {
        return remoteMediaClient
    }

    override fun onSessionConnected(castSession: CastSession) {
        super.onSessionConnected(castSession!!)
    }


    override fun onSessionEnded() {
        super.onSessionEnded()
    }

    override fun onSendingRemoteMediaRequest() {
        super.onSendingRemoteMediaRequest()
    }

    override fun onMediaStatusUpdated() {
        super.onMediaStatusUpdated()
    }
}