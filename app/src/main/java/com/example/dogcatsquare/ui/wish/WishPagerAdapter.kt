package com.example.dogcatsquare.ui.wish

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WishPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2 // 🔹 Tab 개수 (장소, 산책로)

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WishPlaceFragment()  // 🔹 장소 Fragment
            else -> WishWalkFragment()   // 🔹 산책로 Fragment
        }
    }
}
