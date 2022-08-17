package com.example.jwplayer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
import com.example.jwplayer.R
import com.jwplayer.pub.ui.viewmodels.NextUpViewModel
import com.jwplayer.pub.ui.viewmodels.OverlayViewModel
import com.jwplayer.pub.ui.viewmodels.SettingsMenuViewModel
import com.jwplayer.pub.ui.viewmodels.PlaybackRatesMenuViewModel
import com.jwplayer.pub.ui.viewmodels.QualityLevelMenuViewModel
import com.jwplayer.pub.ui.viewmodels.AudiotracksMenuViewModel
import com.jwplayer.pub.ui.viewmodels.CaptionsMenuViewModel
import com.jwplayer.pub.ui.viewmodels.CastingMenuViewModel
import com.jwplayer.pub.ui.viewmodels.ControlbarViewModel
import com.jwplayer.pub.ui.viewmodels.ControlsContainerViewModel
import com.jwplayer.pub.ui.viewmodels.ChaptersViewModel
import com.jwplayer.pub.ui.viewmodels.PlaylistViewModel


import androidx.lifecycle.LifecycleOwner
import com.squareup.picasso.Picasso

class MyNextUpView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    ConstraintLayout(
        context!!, attrs, defStyleAttr, defStyleRes
    ) {
    private var title: TextView? = null
    private var countdown: TextView? = null
    private var poster: ShapeableImageView? = null
    private var close: View? = null

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : this(
        context,
        attrs,
        defStyleAttr,
        0
    ) {
    }

    private fun initView(context: Context?) {
        inflate(context, R.layout.view_my_next_up, this)
        title = findViewById(R.id.next_title)
        countdown = findViewById(R.id.next_count)
        poster = findViewById(R.id.next_poster)
        close = findViewById(R.id.next_close)
    }

    @SuppressLint("SetTextI18n")
    fun bind(nextUpViewModel: NextUpViewModel, lifecycleOwner: LifecycleOwner?) {
        nextUpViewModel.thumbnailUrl.observe(lifecycleOwner!!) { url: String? ->
            Picasso.get().load(url).into(poster)
        }
        nextUpViewModel.title.observe(lifecycleOwner) { string: String? -> title!!.text = string }
        nextUpViewModel.isUiLayerVisible.observe(lifecycleOwner) { visible: Boolean ->
            visibility = if (visible) VISIBLE else GONE
        }
        nextUpViewModel.nextUpTimeRemaining.observe(lifecycleOwner) { remaining: Int ->
            countdown!!.text = "Next in: $remaining"
        }
        close!!.setOnClickListener { v: View? -> nextUpViewModel.closeNextUpView() }
        setOnClickListener { v: View? -> nextUpViewModel.playNextPlaylistItem() }
    }

    init {
        initView(context)
    }
}