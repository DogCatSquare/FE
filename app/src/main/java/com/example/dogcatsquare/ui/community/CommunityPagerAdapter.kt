package com.example.dogcatsquare.ui.community

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CommunityPagerAdapter(fragment: Fragment, private var fragments: List<Fragment>, private var tabTitles: List<String>) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    fun updateFragments(newFragments: List<Fragment>, newTabTitles: List<String>) {
        fragments = newFragments
        tabTitles = newTabTitles
        notifyDataSetChanged() // 🔹 UI 업데이트
    }

    fun getTitle(position: Int): String = tabTitles[position]
}
