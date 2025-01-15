package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentCommunityHomeBinding

class CommunityHomeFragment : Fragment(R.layout.fragment_community_home) {

    private lateinit var binding: FragmentCommunityHomeBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var tipsAdapter: TipsAdapter
    private lateinit var localPostAdapter: LocalPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 더미 데이터 생성
        val popularPosts = listOf(
            Post("닉네임", "포메라니안", "제목을 입력해주세요", "내용을 입력해주세요 내용을 입력해주세요...", "1시간 전", null, 6, 1),
            Post("닉네임", "포메라니안", "제목을 입력해주세요", "내용을 입력해주세요 내용을 입력해주세요...", "1시간 전", null, 6, 1)
        )

        val tips = listOf(
            Tip("강아지 산책할 때 주의할 점", "내용을 입력해주세요 내용을 입력해주세요..."),
            Tip("강아지 간식 추천", "내용을 입력해주세요 내용을 입력해주세요...")
        )

        val localPosts = listOf(
            Post("닉네임1", "성북구 하월곡동", "우리 동네 맛집 소개", "내용을 입력해주세요...", "2024.01.04", null, 6, 1),
            Post("닉네임2", "강남구 삼성동", "새로운 소식 공유", "새로운 소식을 공유합니다...", "2024.01.05", null, 3, 2)
        )

        // RecyclerView 설정
        postAdapter = PostAdapter(popularPosts)
        tipsAdapter = TipsAdapter(tips)
        localPostAdapter = LocalPostAdapter(localPosts)

        binding.rvPopularPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        binding.rvTips.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = tipsAdapter
        }

        binding.rvLocalPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = localPostAdapter
        }
    }
}
