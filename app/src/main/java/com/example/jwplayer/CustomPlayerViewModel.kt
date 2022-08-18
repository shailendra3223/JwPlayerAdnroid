package com.example.jwplayer

import android.util.Log
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jwplayer.pub.api.JWPlayer
import com.jwplayer.pub.api.PlayerState
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.api.configuration.PlayerConfig
import com.jwplayer.pub.api.events.*
import com.jwplayer.pub.api.events.listeners.CastingEvents
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents.*
import com.jwplayer.pub.ui.viewmodels.CastingMenuViewModel
import com.jwplayer.pub.ui.viewmodels.SettingsMenuViewModel

/**
 * This is only an example of how you could handle player events to drive UI state and behavior
 */
class CustomPlayerViewModel(val player: JWPlayer) : OnFirstFrameListener, OnPlayListener,
    OnPauseListener, OnCompleteListener, OnTimeListener, OnFullscreenListener,
    OnReadyListener, OnDisplayClickListener,
    CastingEvents.OnCastListener  {
    // ADS
    var isAdPlaying = false
        private set
    private val adProgressPercentage = MutableLiveData(NO_VALUE_POSITION)

    //    private MutableLiveData<Boolean> isAdProgressVisible = new MutableLiveData<>(false);
    private var currentSkipOffset = NO_VALUE_POSITION
    private val skipOffsetCountdown = MutableLiveData(NO_VALUE_STRING)
    private val isSkipButtonVisible = MutableLiveData(false)
    private val isSkipButtonEnabled = MutableLiveData(false)
    private val isLearnMoreVisible = MutableLiveData(false)
    private var clickthroughURL = NO_VALUE_STRING

    // CONTENT
    val isPlayToggleVisible = MutableLiveData(false)
    val isPlayIcon = MutableLiveData(false)
    val isSeekbarVisible = MutableLiveData(false)
    val isFullscreen = MutableLiveData(false)
    val contentProgressPercentage = MutableLiveData(NO_VALUE_POSITION)


    val isFirstFrame = MutableLiveData<JWPlayer>()
    val isVisibility = MutableLiveData(true)
    var disableTouch = false
    val isControl = MutableLiveData<JWPlayer>()

    public fun onChromeCast() : CastingMenuViewModel {
        return player.getViewModelForUiGroup(UiGroup.CASTING_MENU) as CastingMenuViewModel
    }

    override fun onFirstFrame(firstFrameEvent: FirstFrameEvent) {
        Log.d("VIEWMODEL", "FirstFrame fired")
        isFirstFrame.value = firstFrameEvent.player
    }

    override fun onPlay(playEvent: PlayEvent) {
        resetAdState()
        //        updateAdsUi();
        updateContentUi()
    }

    override fun onPause(pauseEvent: PauseEvent) {
        updateContentUi()
    }

    override fun onTime(timeEvent: TimeEvent) {
        handleTimeUpdate(
            timeEvent.position,
            timeEvent.duration,
            contentProgressPercentage
        )
    }

    //    @Override
    //    public void onAdMeta(AdMetaEvent adMetaEvent) {
    //        currentSkipOffset = adMetaEvent.getSkipOffset();
    //        clickthroughURL = adMetaEvent.getClickThroughUrl();
    //    }
    //
    //    @Override
    //    public void onAdTime(AdTimeEvent adTimeEvent) {
    //        handleTimeUpdate(adTimeEvent.getPosition(),
    //                         adTimeEvent.getDuration(),
    //                         adProgressPercentage
    //        );
    //        handleSkipOffsetUpdates((int) adTimeEvent.getPosition());
    //    }
    private fun handleSkipOffsetUpdates(position: Int) {
        if (currentSkipOffset == NO_VALUE_POSITION) {
            isSkipButtonVisible.value = false
            isSkipButtonEnabled.value = false
            return
        }
        val skipCounter = currentSkipOffset - position
        if (skipCounter <= 0) {
            // you are allowed to skip
            isSkipButtonVisible.value = true
            isSkipButtonEnabled.value = true
            skipOffsetCountdown.setValue("Skip ad")
        } else {
            // skip is available, but still counting down
            isSkipButtonVisible.value = true
            isSkipButtonEnabled.value = false
            skipOffsetCountdown.setValue("Skip ad in $skipCounter")
        }
    }

    /**
     * This assumes VOD content only. Does not account for Live and DVR scenarios
     */
    private fun handleTimeUpdate(
        position: Double,
        duration: Double,
        percentageLD: MutableLiveData<Int>
    ) {
        val currentPercentage = calculateProgressPercentage(position, duration)
        val lastPercentage = percentageLD.value!!
        if (currentPercentage != lastPercentage) {
            percentageLD.value = currentPercentage
        }
    }

    private fun calculateProgressPercentage(position: Double, duration: Double): Int {
        return (position * 100 / duration).toInt()
    }

    //    @Override
    //    public void onAdBreakEnd(AdBreakEndEvent adBreakEndEvent) {
    //        resetAdState();
    //        updateAdsUi();
    //        updateContentUi();
    //    }
    //
    //    @Override
    //    public void onAdBreakStart(AdBreakStartEvent adBreakStartEvent) {
    //        isAdPlaying = true;
    //        updateContentUi();
    //        updateAdsUi();
    //    }
    override fun onReady(readyEvent: ReadyEvent) {
        resetAdState()
        //        updateAdsUi();
        updateContentUi()
    }

    override fun onFullscreen(fullscreenEvent: FullscreenEvent) {
        isFullscreen.value = fullscreenEvent.fullscreen
    }

    private fun resetAdState() {
        isAdPlaying = false
        clickthroughURL = NO_VALUE_STRING
        currentSkipOffset = NO_VALUE_POSITION
        adProgressPercentage.value = NO_VALUE_POSITION
        skipOffsetCountdown.value = NO_VALUE_STRING
        isSkipButtonVisible.value = false
        //        isAdProgressVisible.setValue(false);
        isLearnMoreVisible.value = false
    }

    private fun updateContentUi() {
        isVisibility.value = true
        isPlayToggleVisible.value = shouldPlayToggleBeVisible()
        isPlayIcon.value = shouldPlayIconBeVisible()
        isSeekbarVisible.value = shouldSeekbarBeVisible()
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

    fun getSkipOffsetCountdown(): LiveData<String> {
        return skipOffsetCountdown
    }

    fun getIsSkipButtonVisible(): LiveData<Boolean> {
        return isSkipButtonVisible
    }

    fun getIsSkipButtonEnabled(): LiveData<Boolean> {
        return isSkipButtonEnabled
    }

    fun openAdClickthrough() {
        player.openAdClickthrough()
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

    //    public LiveData<Boolean> getIsAdProgressVisible() {
    //        return isAdProgressVisible;
    //    }
    fun getIsLearnMoreVisible(): LiveData<Boolean> {
        return isLearnMoreVisible
    }

    fun getIsFullscreen(): LiveData<Boolean> {
        return isFullscreen
    }

    fun getContentProgressPercentage(): LiveData<Int> {
        return contentProgressPercentage
    }

    override fun onComplete(completeEvent: CompleteEvent) {
        updateContentUi()
    }

    companion object {
        private const val NO_VALUE_POSITION = -1
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
        //        player.addListener(EventType.AD_TIME, this);
//        player.addListener(EventType.AD_META, this);
//        player.addListener(EventType.AD_BREAK_START, this);
//        player.addListener(EventType.AD_BREAK_END, this);
        player.addListener(EventType.READY, this)
    }

    override fun onDisplayClick(click: DisplayClickEvent) {
        Log.i("TAGop", "onDisplayClick:mEpisodes ${isVisibility.value} $disableTouch")

        if (!disableTouch) {
            isVisibility.value = !isVisibility.value!!
        }
//        isVisibility.value = true

    }

    override fun onCast(p0: CastEvent?) {
        TODO("Not yet implemented")
    }
}