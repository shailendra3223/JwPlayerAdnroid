package com.google.sample.cast.refplayer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.PlayerState
import com.jwplayer.pub.api.events.*
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.*

/**
 * This is only an example of how you could handle player events to drive UI state and behavior
 */
class CustomPlayerViewModel(val player: JWPlayer) : OnFirstFrameListener, OnPlayListener,
    OnPauseListener, OnCompleteListener, OnTimeListener, OnFullscreenListener,
    OnReadyListener, OnDisplayClickListener, OnErrorListener , OnSetupErrorListener {

    // ADS
    var isAdPlaying = false
    private val adProgressPercentage = MutableLiveData(NO_VALUE_POSITION)
    private var currentSkipOffset = NO_VALUE_POSITION
    private val skipOffsetCountdown = MutableLiveData(NO_VALUE_STRING)
    private val isSkipButtonVisible = MutableLiveData(false)
    private val isLearnMoreVisible = MutableLiveData(false)
    private var clickthroughURL = NO_VALUE_STRING

    // CONTENT
    val isPlayToggleVisible = MutableLiveData(false)
    val isPlayIcon = MutableLiveData(false)
    val isSeekbarVisible = MutableLiveData(false)
    val isFullscreen = MutableLiveData(false)
    val contentProgressPercentage = MutableLiveData(NO_VALUE_POSITION_PLAY)

    val isFirstFrame = MutableLiveData<JWPlayer>()
    val isError = MutableLiveData<ErrorEvent>()
    val isSetupError = MutableLiveData<SetupErrorEvent>()
    val isVisibility = MutableLiveData(true)
    val disableTouch = MutableLiveData(false)
    val printTime = MutableLiveData("")

    override fun onReady(readyEvent: ReadyEvent) {
        Log.d("TAG", "onReady")
//        updateContentUi()
    }

    override fun onFirstFrame(firstFrameEvent: FirstFrameEvent) {
        Log.d("TAG", "onFirstFrame")
        isFirstFrame.value = firstFrameEvent.player
    }

    override fun onPlay(playEvent: PlayEvent) {
        Log.d("TAG", "onPlay")
        updateContentUi()
    }

    override fun onPause(pauseEvent: PauseEvent) {
        Log.d("TAG", "onPause")
        updateContentUi()
    }

    override fun onTime(timeEvent: TimeEvent) {
        Log.d("TAG", "onTime ${timeEvent.duration} ${timeEvent.position}")
//        printTime.value = readTime(timeEvent.position)
        printTime.value = readRemainingTime(timeEvent.position, timeEvent.duration)
        handleTimeUpdate(
            timeEvent.position,
            timeEvent.duration,
            contentProgressPercentage
        )
    }

    private fun readTime(position: Double) : String {
        var min = position.toInt() / 60
        val sec = position.toInt() % 60
        var hr = 0
        if (min >= 60) {
            hr = min/60
            min %= 60
        }

        val hours = if(hr.toString().length == 1) "0${hr}" else hr
        val minute = if(min.toString().length == 1) "0${min}" else min
        val second = if(sec.toString().length == 1) "0${sec}" else sec
        return if (hr > 0) {
            ("${hours}:${minute}:${second}")
        } else {
            ("${minute}:${second}")
        }
    }

    private fun readRemainingTime(position: Double, duration: Double) : String {

        val remainPos = duration-position
        var min = remainPos.toInt() / 60
        val sec = remainPos.toInt() % 60
        var hr = 0
        if (min >= 60) {
            hr = min/60
            min %= 60
        }

        val hours = if(hr.toString().length == 1) "0${hr}" else hr
        val minute = if(min.toString().length == 1) "0${min}" else min
        val second = if(sec.toString().length == 1) "0${sec}" else sec
        return if (hr > 0) {
            ("- ${hours}:${minute}:${second}")
        } else {
            ("- ${minute}:${second}")
        }
    }
    /**
     * This assumes VOD content only. Does not account for Live and DVR scenarios
     */
    fun handleTimeUpdate(
        position: Double,
        duration: Double,
        percentageLD: MutableLiveData<Float>
    ) {
        Log.i("TAG", "handleTimeUpdate:onTime ${calculateProgressPercentage(position, duration)}")
        val currentPercentage = calculateProgressPercentage(position, duration)
        val lastPercentage = percentageLD.value!!
        Log.i("TAG", "handleTimeUpdate:onTime ${lastPercentage} ${percentageLD.value}")
        if (currentPercentage != lastPercentage) {
            percentageLD.value = currentPercentage
        }
    }

    private fun calculateProgressPercentage(position: Double, duration: Double): Float {
        return (position * 100 / duration).toFloat()
    }

    override fun onFullscreen(fullscreenEvent: FullscreenEvent) {
        Log.d("TAG", "onFullscreen")
        isFullscreen.value = fullscreenEvent.fullscreen
    }

    private fun resetAdState() {
        isAdPlaying = false
        clickthroughURL = NO_VALUE_STRING
        currentSkipOffset = NO_VALUE_POSITION
        adProgressPercentage.value = NO_VALUE_POSITION
        skipOffsetCountdown.value = NO_VALUE_STRING
        isSkipButtonVisible.value = false
        isLearnMoreVisible.value = false
    }

    private fun updateContentUi() {
        isPlayToggleVisible.value = shouldPlayToggleBeVisible()
        isPlayIcon.value = shouldPlayIconBeVisible()
        isSeekbarVisible.value = shouldSeekbarBeVisible()
        isVisibility.value = shouldPlayIconBeVisible()
    }

    private fun updateAdsUi() {
//        isAdProgressVisible.setValue(shouldAdProgressBeVisible());
        isSkipButtonVisible.value = shouldSkipButtonBeVisible()
        isLearnMoreVisible.value = shouldLearnMoreBeVisible()
    }

    private fun shouldPlayToggleBeVisible(): Boolean {
        return player.state != PlayerState.ERROR &&
                player.state != PlayerState.BUFFERING
    }

    private fun shouldPlayIconBeVisible(): Boolean {
        return player.state != PlayerState.PLAYING && player.state != PlayerState.ERROR && player.state != PlayerState.BUFFERING
    }

    private fun shouldSeekbarBeVisible(): Boolean {
        return player.state == PlayerState.PLAYING || player
            .state == PlayerState.PAUSED
    }

    private fun shouldAdProgressBeVisible(): Boolean {
        return isAdPlaying
    }

    private fun shouldSkipButtonBeVisible(): Boolean {
        return isAdPlaying && currentSkipOffset != NO_VALUE_POSITION
    }

    private fun shouldLearnMoreBeVisible(): Boolean {
        return isAdPlaying && clickthroughURL == NO_VALUE_STRING
    }

    fun togglePlay() {
        if (player.state != PlayerState.PLAYING) {
            player.play()
        } else {
            player.pause()
        }
    }

    fun seek(percentage: Int) {
        val position = (percentage * player.duration / 100).toInt()
        player.seek(position.toDouble())
    }

    fun skipAd() {
        player.skipAd()
    }

    fun toggleFullscreen() {
        player.setFullscreen(!player.fullscreen, true)
    }

    fun getAdProgressPercentage(): LiveData<Int> {
        return adProgressPercentage
    }


    fun getIsPlayToggleVisible(): LiveData<Boolean> {
        return isPlayToggleVisible
    }

    fun getIsPlayIcon(): LiveData<Boolean> {
        return isPlayIcon
    }

    fun getIsSeekbarVisible(): LiveData<Boolean> {
        return isSeekbarVisible
    }

    fun getIsFullscreen(): LiveData<Boolean> {
        return isFullscreen
    }

    fun getContentProgressPercentage(): LiveData<Float> {
        return contentProgressPercentage
    }

    override fun onComplete(completeEvent: CompleteEvent) {
        Log.d("TAGSS", "onComplete")
        updateContentUi()
    }

    companion object {
        private const val NO_VALUE_POSITION = -1
        private const val NO_VALUE_POSITION_PLAY = 0.0f
        private const val NO_VALUE_STRING = ""
    }

    init {
        player.addListener(EventType.FIRST_FRAME, this)
        player.addListener(EventType.PLAY, this)
        player.addListener(EventType.PAUSE, this)
        player.addListener(EventType.TIME, this)
        player.addListener(EventType.COMPLETE, this)
        player.addListener(EventType.FULLSCREEN, this)
        player.addListener(EventType.DISPLAY_CLICK, this)
        player.addListener(EventType.READY, this)
        player.addListener(EventType.SETUP_ERROR, this)
        player.addListener(EventType.ERROR, this)
    }

    override fun onDisplayClick(click: DisplayClickEvent) {
//        if (!disableTouch.value!!) {
            isVisibility.value = !isVisibility.value!!
//        }
    }

    override fun onError(errorEvent: ErrorEvent?) {
        isError.value = errorEvent
    }

    override fun onSetupError(setupErrorEvent: SetupErrorEvent?) {
        isSetupError.value = setupErrorEvent
    }
}