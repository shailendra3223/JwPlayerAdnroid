package com.example.jwplayer.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.jwplayer.Episode
import com.example.jwplayer.R
import com.example.jwplayer.Season
import com.example.jwplayer.adapter.PlayListSeasonAdapter
import com.example.jwplayer.databinding.FragmentPlayListSeasonBinding
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