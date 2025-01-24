package com.example.dogcatsquare.ui.community

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CommunityPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // 각 탭에 사용할 프래그먼트 리스트
    private val fragments: List<Fragment> = listOf(
        CommunityHomeFragment(), // 홈
        CommunityLocalFragment(), // 동네 이야기
        CommunityTipsFragment()  // 꿀팁
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
