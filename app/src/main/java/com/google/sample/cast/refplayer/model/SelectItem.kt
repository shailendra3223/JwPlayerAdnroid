package com.google.sample.cast.refplayer.model

import com.google.gson.annotations.SerializedName
import com.jwplayer.pub.api.media.adaptive.QualityLevel
import com.jwplayer.pub.api.media.audio.AudioTrack
import com.jwplayer.pub.api.media.captions.Caption

class SelectItem (var valuePlayRate: Double?,
                  var valueQuality: QualityLevel?,
                  var valueAudio: AudioTrack?,
                  var valueSubtitle: Caption?,
                  var check: Boolean?)