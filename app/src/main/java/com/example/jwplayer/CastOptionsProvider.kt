package com.example.jwplayer

import android.content.Context
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.media.NotificationOptions
import com.google.android.gms.cast.framework.media.MediaIntentReceiver
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.example.jwplayer.CastOptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import java.util.*

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        val notificationOptions = NotificationOptions.Builder()
            .setActions(
                listOf(
                    MediaIntentReceiver.ACTION_SKIP_NEXT,
                    MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                    MediaIntentReceiver.ACTION_STOP_CASTING
                ), intArrayOf(1, 2)
            )
            .setTargetActivityClassName(MainActivity::class.java.name)
            .build()
        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .build()
        return CastOptions.Builder()
            .setReceiverApplicationId(DEFAULT_APPLICATION_ID)
            .setCastMediaOptions(mediaOptions)
            .build()
    }

    override fun getAdditionalSessionProviders(appContext: Context): List<SessionProvider>? {
        return null
    }

    companion object {
        /**
         * The Application Id to use, currently the Default Media Receiver.
         */
        private const val DEFAULT_APPLICATION_ID =
            CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
    }
}