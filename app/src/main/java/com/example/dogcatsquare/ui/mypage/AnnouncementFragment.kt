package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.databinding.FragmentAnnouncementBinding

class AnnouncementFragment : Fragment() {
    lateinit var binding: FragmentAnnouncementBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAnnouncementBinding.inflate(inflater, container, false)

        return binding.root
    }
}