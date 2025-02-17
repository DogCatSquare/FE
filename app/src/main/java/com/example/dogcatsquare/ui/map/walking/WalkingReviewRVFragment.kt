package com.example.dogcatsquare.ui.map.walking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.map.MapReview
import com.example.dogcatsquare.databinding.FragmentMapwalkingReviewrvBinding

class WalkingReviewRVFragment : Fragment() {

    private var _binding: FragmentMapwalkingReviewrvBinding? = null
    private val binding get() = _binding!!

    private val reviewDatas = ArrayList<MapReview>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapwalkingReviewrvBinding.inflate(inflater, container, false)

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupRecyclerView()

        return binding.root
    }

    private fun setupRecyclerView() {
        // 기존 데이터를 모두 지우고 새 데이터를 추가
        reviewDatas.clear()

        // 리뷰 데이터 추가
        reviewDatas.apply {
            add(MapReview(
                id = 1,
                content = "강아지 종합접종이랑 중성화했어요 의사선생님이 친절하시고 꼼꼼히 봐주셔서 좋아요.",
                breed = "포메라니안",
                nickname = "닉네임",
                userImageUrl = null,
                createdAt = "2024-01-04T00:00:00",
                userId = 1,
                placeReviewImageUrl = listOf(),
                placeId = 1
            ))
            add(MapReview(
                id = 2,
                content = "두 번째 리뷰",
                breed = "포메라니안",
                nickname = "닉네임",
                userImageUrl = null,
                createdAt = "2024-01-04T00:00:00",
                userId = 2,
                placeReviewImageUrl = listOf(),
                placeId = 1
            ))
            add(MapReview(
                id = 3,
                content = "세 번째 리뷰 내용",
                breed = "기니피그",
                nickname = "닉네임",
                userImageUrl = null,
                createdAt = "2024-01-03T00:00:00",
                userId = 3,
                placeReviewImageUrl = listOf(),
                placeId = 1
            ))
            add(MapReview(
                id = 4,
                content = "네 번째 리뷰 내용...",
                breed = "치와와",
                nickname = "닉네임",
                userImageUrl = null,
                createdAt = "2024-01-02T00:00:00",
                userId = 4,
                placeReviewImageUrl = listOf(),
                placeId = 1
            ))
        }

        // RecyclerView 어댑터 연결
        val walkingReviewRVAdapter = WalkingReviewRVAdapter(reviewDatas)
        binding.reviewRV.apply {
            adapter = walkingReviewRVAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
