package com.google.sample.cast.refplayer

import android.content.Context
import com.google.sample.cast.refplayer.R
import com.google.sample.cast.refplayer.cast.ExpandedControlsActivity
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.LaunchOptions
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.MediaIntentReceiver
import com.google.android.gms.cast.framework.media.NotificationOptions
import java.util.*

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        val notificationOptions = NotificationOptions.Builder()
            .setActions(
                Arrays.asList(
                    MediaIntentReceiver.ACTION_SKIP_NEXT,
                    MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                    MediaIntentReceiver.ACTION_STOP_CASTING
                ), intArrayOf(1, 2)
            )
            .setTargetActivityClassName(MainActivity::class.java.name)
            .build()
        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(ExpandedControlsActivity::class.java.name)
            .build()

        /** Following lines enable Cast Connect  */
        val launchOptions = LaunchOptions.Builder()
            .setAndroidReceiverCompatible(true)
            .build()
        return CastOptions.Builder()
            .setLaunchOptions(launchOptions)
//            .setReceiverApplicationId(DEFAULT_APPLICATION_ID)
            .setReceiverApplicationId(context.getString(R.string.app_id))
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