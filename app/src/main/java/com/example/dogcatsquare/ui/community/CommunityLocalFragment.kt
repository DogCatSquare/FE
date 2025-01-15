package com.example.dogcatsquare.ui.community

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentCommunityLocalBinding

class CommunityLocalFragment : Fragment(R.layout.fragment_community_local) {

    private lateinit var binding: FragmentCommunityLocalBinding
    private lateinit var localPostAdapter: LocalPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityLocalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 글쓰기 버튼 클릭 이벤트
        binding.btnAddPost.setOnClickListener {
            val intent = Intent(requireContext(), CommunityWriteActivity::class.java)
            startActivity(intent)
        }
        // 더미 데이터 생성
        val localPosts = listOf(
            Post(
                username = "닉네임1",
                location = "성북구 하월곡동",
                title = "우리 동네 맛집 소개",
                content = "내용을 입력해주세요 내용을 입력해주세요 내용을 입력해주세요...",
                date = "2024.01.04",
                thumbnail = null, // null일 경우 회색 배경 사용
                likeCount = 6,
                commentCount = 1
            ),
            Post(
                username = "닉네임2",
                location = "강남구 삼성동",
                title = "새로운 소식 공유",
                content = "새로운 소식을 공유합니다! 내용을 입력해주세요...",
                date = "2024.01.05",
                thumbnail = null, // null일 경우 회색 배경 사용
                likeCount = 3,
                commentCount = 2
            )
        )

        // 어댑터 설정
        localPostAdapter = LocalPostAdapter(localPosts)

        // RecyclerView 설정
        binding.rvLocalPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = localPostAdapter
        }
    }
}
