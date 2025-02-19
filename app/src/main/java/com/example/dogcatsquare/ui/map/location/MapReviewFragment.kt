package com.example.dogcatsquare.ui.map.location

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.map.MapReview
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapReviewBinding

class MapReviewFragment : Fragment() {
    private var _binding: FragmentMapReviewBinding? = null
    private val binding get() = _binding!!

    private val reviewDatas by lazy { ArrayList<MapReview>() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupRecyclerView()
        setupAddReviewButton()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            // 뒤로가기 시 부모 프래그먼트 표시
            requireActivity().supportFragmentManager.fragments
                .filterIsInstance<MapDetailFragment>()
                .firstOrNull()?.let { detailFragment ->
                    requireActivity().supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_left,   // 이전 프래그먼트가 왼쪽에서 들어옴
                            R.anim.slide_out_right, // 현재 프래그먼트가 오른쪽으로 나감
                            R.anim.slide_in_right,  // 새 프래그먼트가 오른쪽에서 들어옴
                            R.anim.slide_out_left   // 현재 프래그먼트가 왼쪽으로 나감
                        )
                        .show(detailFragment)
                        .commit()
                }
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        reviewDatas.clear()

        reviewDatas.apply {
            add(MapReview(
                id = 1,
                content = "강아지 종합접종이랑 중성화했어요 의사선생님이 친절하시고 꼼꼼히 봐주셔서 좋아요. 다음에 건강검진도 이곳에... 더보기",
                breed = "포메라니안",
                nickname = "닉네임",
                userImageUrl = null,  // null이면 기본 이미지가 표시됨
                createdAt = "2024-01-04T00:00:00",
                userId = 1,
                placeReviewImageUrl = listOf(),  // 빈 리스트이면 기본 이미지가 표시됨
                placeId = 1
            ))
            add(MapReview(
                id = 2,
                content = "두 번째 리뷰 내용...",
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
                content = "세 번째 리뷰 내용...",
                breed = "골든리트리버",
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
                breed = "시바견",
                nickname = "닉네임",
                userImageUrl = null,
                createdAt = "2024-01-02T00:00:00",
                userId = 4,
                placeReviewImageUrl = listOf(),
                placeId = 1
            ))
        }

        val mapReviewRVAdapter = MapReviewRVAdapter(reviewDatas)
        binding.reviewRV.apply {
            adapter = mapReviewRVAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupAddReviewButton() {
        binding.addReview.setOnClickListener {
            val mapAddReviewFragment = MapAddReviewFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,  // 새 프래그먼트가 오른쪽에서 들어옴
                    R.anim.slide_out_left,  // 현재 프래그먼트가 왼쪽으로 나감
                    R.anim.slide_in_left,   // 뒤로가기 시 현재 프래그먼트가 왼쪽에서 들어옴
                    R.anim.slide_out_right  // 새 프래그먼트가 오른쪽으로 나감
                )
                .hide(this)  // replace 대신 hide 사용
                .add(R.id.main_frm, mapAddReviewFragment)  // add로 새 프래그먼트 추가
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 프래그먼트가 제거될 때 부모 프래그먼트 표시
        requireActivity().supportFragmentManager.fragments
            .filterIsInstance<MapDetailFragment>()
            .firstOrNull()?.let { detailFragment ->
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_left,   // 이전 프래그먼트가 왼쪽에서 들어옴
                        R.anim.slide_out_right, // 현재 프래그먼트가 오른쪽으로 나감
                        R.anim.slide_in_right,  // 새 프래그먼트가 오른쪽에서 들어옴
                        R.anim.slide_out_left   // 현재 프래그먼트가 왼쪽으로 나감
                    )
                    .show(detailFragment)
                    .commit()
            }
        _binding = null
    }
}