package com.example.dogcatsquare.ui.wish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.dogcatsquare.databinding.FragmentWishBinding
import com.google.android.material.tabs.TabLayoutMediator

class WishFragment : Fragment() {
    private var _binding: FragmentWishBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishBinding.inflate(inflater, container, false)

        // 🔹 ViewPager2 어댑터 설정
        val adapter = WishPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // 🔹 TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "장소" else "산책로"
        }.attach()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}