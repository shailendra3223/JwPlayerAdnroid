package com.example.jwplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.mediarouter.media.MediaRouter
import androidx.recyclerview.widget.RecyclerView
import com.example.jwplayer.CustomPlayerViewModel
import com.example.jwplayer.R
import com.example.jwplayer.databinding.LayoutCastBinding
import com.jwplayer.pub.api.UiGroup
import com.jwplayer.pub.ui.viewmodels.CastingMenuViewModel
import kotlin.collections.List

class CastSelectAdapter (
    private val context: Context,
    private val dataList: List<MediaRouter.RouteInfo>,
    private val customPlayerView: CustomPlayerViewModel,
    private val selectListener: CastDevicesInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class SelectedViewHolder(var binding: LayoutCastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: MediaRouter.RouteInfo, position: Int) {
            binding.tvItemName.text = data.name!!.toString()
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(context)
        val binding: LayoutCastBinding =
            DataBindingUtil.inflate(inflater, R.layout.layout_cast, viewGroup, false)

        return SelectedViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        if (viewHolder is SelectedViewHolder) {
            val data: MediaRouter.RouteInfo = dataList[position]
            viewHolder.onBind(data, position)

            viewHolder.binding.tvConnect.setOnClickListener {
                viewHolder.binding.tvConnect.tag = data
                val cast = customPlayerView.player.getViewModelForUiGroup(UiGroup.CASTING_MENU) as CastingMenuViewModel
                selectListener.onConnect(data, cast, position)
            }

        }
    }

    interface CastDevicesInterface {
        fun onConnect(data: MediaRouter.RouteInfo, cast: CastingMenuViewModel, position : Int)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataList.size
}