package com.example.dogcatsquare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentHomeWeatherBinding

class HomeWeatherFragment : Fragment() {
    lateinit var binding: FragmentHomeWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeWeatherBinding.inflate(inflater, container, false)

        binding.homeBellIv.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, AlarmFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        setupWeatherViewPager()

        return binding.root
    }

    // 설정한 디데이에 따라 뷰페이지 바뀌어야 함
    private fun setupWeatherViewPager() {

    }

    // 날씨 api 연결

}