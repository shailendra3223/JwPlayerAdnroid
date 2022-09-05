package com.google.sample.cast.refplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.sample.cast.refplayer.Episode
import com.google.sample.cast.refplayer.R
import com.google.sample.cast.refplayer.Season
import com.google.sample.cast.refplayer.adapter.PlayListSeasonAdapter
import com.google.sample.cast.refplayer.databinding.FragmentPlayListSeasonBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class PlayListSeasonFragment(val season: List<Season>) : BottomSheetDialogFragment() {

    val binding: FragmentPlayListSeasonBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentPlayListSeasonBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_play_list_season, container, false)

        // Inflate the layout for this fragment

        val adapter =
            PlayListSeasonAdapter(requireActivity(), season[0].episodes, object : PlayListSeasonAdapter.CastDevicesInterface {
                override fun onVideoClick(data: Episode, position: Int) {

                }
            })

        binding.adapterPlayList = adapter
        return binding.root
    }

}