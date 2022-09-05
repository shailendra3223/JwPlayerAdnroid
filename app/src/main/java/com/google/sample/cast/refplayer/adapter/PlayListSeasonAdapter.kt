package com.google.sample.cast.refplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.sample.cast.refplayer.*
import com.google.sample.cast.refplayer.databinding.PlaylistItemBinding
import com.google.sample.cast.refplayer.Episode
import com.squareup.picasso.Picasso

class PlayListSeasonAdapter (
    private val context: Context,
    private val dataList: List<Episode>,
    private val selectListener: CastDevicesInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class SelectedViewHolder(var binding: PlaylistItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: Episode, position: Int) {
            binding.tvTitle.text = data.title
            binding.tvDescription.text = data.description
            Picasso.get().load(data.original_thumbnail_file).into(binding.ivThumbnail)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(context)
        val binding: PlaylistItemBinding =
            DataBindingUtil.inflate(inflater, R.layout.playlist_item, viewGroup, false)

        return SelectedViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        if (viewHolder is SelectedViewHolder) {
            val data: Episode = dataList[position]
            viewHolder.onBind(data, position)

            viewHolder.binding.parent.setOnClickListener {
                viewHolder.binding.parent.tag = data
                selectListener.onVideoClick(data, position)
            }

        }
    }

    interface CastDevicesInterface {
        fun onVideoClick(data: Episode, position : Int)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataList.size
}