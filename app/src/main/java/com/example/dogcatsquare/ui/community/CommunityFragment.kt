package com.example.dogcatsquare.ui.community

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentCommunityBinding
import com.example.dogcatsquare.ui.community.BoardSettingsActivity
import com.google.android.material.tabs.TabLayoutMediator

class CommunityFragment : Fragment(R.layout.fragment_community) {

    private lateinit var binding: FragmentCommunityBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewPager2 어댑터 설정
        val pagerAdapter = CommunityPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "홈"
                1 -> "동네 이야기"
                2 -> "꿀팁"
                else -> "탭 $position"
            }
        }.attach()

        // TabLayout의 텍스트 색상 및 선택된 색상 설정
        binding.tabLayout.setTabTextColors(Color.parseColor("#000000"), Color.parseColor("#FFB200"))
        binding.tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFB200"))

        // 톱니바퀴 클릭 이벤트 설정
        binding.ivSettings.setOnClickListener {
            val intent = Intent(requireContext(), BoardSettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
