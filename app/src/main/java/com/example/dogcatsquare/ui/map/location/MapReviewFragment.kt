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
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        reviewDatas.clear()

        reviewDatas.apply {
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "포메라니안", "강아지 종합접종이랑 중성화했어요 의사선생님이 친절하시고 꼼꼼히 봐주셔서 좋아요. 다음에 건강검진도 이곳에... 더보기", "2024.01.04", R.drawable.ic_place_img_default))
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "포메라니안", "두 번째 리뷰 내용...", "2024.01.04", R.drawable.ic_place_img_default))
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "골든리트리버", "세 번째 리뷰 내용...", "2024.01.03", R.drawable.ic_place_img_default))
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "시바견", "네 번째 리뷰 내용...", "2024.01.02", R.drawable.ic_place_img_default))
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
                .replace(R.id.main_frm, mapAddReviewFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}