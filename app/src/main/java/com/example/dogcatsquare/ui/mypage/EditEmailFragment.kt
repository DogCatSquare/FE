package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.databinding.FragmentEditEmailBinding

class EditEmailFragment : Fragment() {
    lateinit var binding: FragmentEditEmailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditEmailBinding.inflate(inflater, container, false)

        return binding.root
    }
}