package com.example.dogcatsquare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dogcatsquare.databinding.FragmentHomeBinding
import com.example.dogcatsquare.databinding.FragmentMapDetailBinding

class MapDetailFragment : Fragment() {
    lateinit var binding: FragmentMapDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapDetailBinding.inflate(inflater, container, false)

        return binding.root
    }
}