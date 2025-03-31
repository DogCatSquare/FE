package com.example.dogcatsquare.ui.map.walking

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapwalkingReviewallBinding
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel

class WalkingReviewAllFragment : Fragment() {

    private var _binding: FragmentMapwalkingReviewallBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewAdapter: ReviewAdapter
    private val walkReviewViewModel: WalkReviewViewModel by viewModels()
    private var walkId: Int? = null  // null 가능성 고려

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapwalkingReviewallBinding.inflate(inflater, container, false)
        val view = binding.root

        // 툴바 설정
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.walkingReviewToolbar)
            supportActionBar?.title = "이웃들의 후기"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            binding.walkingReviewToolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        // RecyclerView 설정
        reviewAdapter = ReviewAdapter(emptyList())
        binding.reviewRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewAdapter
        }

        // walkId 가져오기
        walkId = arguments?.getInt("walkId")

        // walkId 유효할 경우 API 호출
        walkId?.let {
            Log.d("WalkingReviewAll", "Fetching reviews for walkId: $it")
            walkReviewViewModel.getWalkReviews(it)
        } ?: Log.e("WalkingReviewAll", "walkId is null or invalid")


        walkReviewViewModel.reviewResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                val reviews = response.result?.walkReviews?.map { walkReview ->
                    WalkReview(
                        reviewId = walkReview.reviewId,
                        walkId = walkReview.walkId,
                        content = walkReview.content,
                        walkReviewImageUrl = walkReview.walkReviewImageUrl,
                        createdAt = walkReview.createdAt,
                        updatedAt = walkReview.updatedAt,
                        createdBy = walkReview.createdBy
                    )
                } ?: emptyList()

                reviewAdapter.updateData(reviews)
                reviewAdapter.notifyDataSetChanged()
            } else {
                Log.e("WalkingReviewAll", "Failed to load reviews.")
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
