package com.example.jwplayer.model

import androidx.mediarouter.media.MediaRouter
import com.google.gson.annotations.SerializedName
import com.jwplayer.pub.api.media.adaptive.QualityLevel
import com.jwplayer.pub.api.media.audio.AudioTrack
import com.jwplayer.pub.api.media.captions.Caption

class CastSelectItem (var valueCast: MediaRouter.RouteInfo?,
                      var isCheck: Boolean?)