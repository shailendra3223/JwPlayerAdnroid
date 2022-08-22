package com.example.jwplayer.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.jwplayer.R
import com.example.jwplayer.databinding.LayoutRadioBinding
import com.example.jwplayer.model.SelectItem
import kotlin.collections.ArrayList

//FLAG PlayRate:1001, Quality:1002, Audio:1003, Subtitle:1004
class SelectAdapter(
    private val context: Context,
    private val dataList: ArrayList<SelectItem>,
    private val FLAG: Int,
    private val selectListener: SelectItemInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPosition = -1

    class SelectedViewHolder(var binding: LayoutRadioBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: SelectItem, position: Int, mFLAG: Int) {
            if (mFLAG == 1001) {
                binding.tvItemName.text = data.valuePlayRate.toString()
            } else if (mFLAG == 1002) {
                binding.tvItemName.text = data.valueQuality!!.label.toString()
            } else if (mFLAG == 1004) {
                Log.i("TAGlo", "onBind: ${data.valueSubtitle!!.label.toString()}")
                binding.tvItemName.text = data.valueSubtitle!!.label.toString()
                binding.view.visibility = GONE
            }
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(context)
        val binding: LayoutRadioBinding =
            DataBindingUtil.inflate(inflater, R.layout.layout_radio, viewGroup, false)

        return SelectedViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        if (viewHolder is SelectedViewHolder) {
            val data: SelectItem = dataList[position]
            viewHolder.onBind(data, position, FLAG)

            var pos = position
            if(selectedPosition < 0)
            {
                if (data.check == true) {
                    selectedPosition = pos
                }
            }


            viewHolder.binding.radio.isChecked = selectedPosition == position
            viewHolder.binding.radio.setOnClickListener {
                if (selectedPosition >= 0)
                    notifyItemChanged(selectedPosition)
                selectedPosition = viewHolder.absoluteAdapterPosition
                notifyItemChanged(selectedPosition)

                viewHolder.binding.radio.tag = data
                selectListener.onSelected(data, position, FLAG)

            }

        }
    }

    interface SelectItemInterface {
        fun onSelected(data: SelectItem, position : Int, FLAG: Int)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataList.size
}