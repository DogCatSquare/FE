package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.Tip
import com.example.dogcatsquare.databinding.FragmentCommunityTipsBinding

class CommunityTipsFragment : Fragment(R.layout.fragment_community_tips) {

    private lateinit var binding: FragmentCommunityTipsBinding
    private lateinit var tipsAdapter: TipsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityTipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 더미 데이터 생성
        val tips = listOf(
            Tip(
                title = "강아지 산책할 때 주의할 점",
                content = "내용을 입력해주세요 내용을 입력해주세요 내용을 입력해주세요...",
                thumbnailResId = R.drawable.ic_sample_image
            ),
            Tip(
                title = "강아지 간식 추천",
                content = "내용을 입력해주세요 내용을 입력해주세요 내용을 입력해주세요...",
                thumbnailResId = R.drawable.ic_sample_image
            )
        )

        // RecyclerView 설정
        tipsAdapter = TipsAdapter(tips, isCompactView = false)
        binding.rvTips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tipsAdapter
        }

    }
}
