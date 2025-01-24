package com.example.dogcatsquare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.databinding.FragmentHomeAdBinding

class HomeAdFragment(val imgRes : Int) : Fragment() {
    lateinit var binding: FragmentHomeAdBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeAdBinding.inflate(inflater, container, false)
        binding.homeAdIv.setImageResource(imgRes)
        return binding.root
    }
}