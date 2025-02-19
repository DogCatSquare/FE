package com.example.dogcatsquare.ui.map.walking

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel

class WalkingReviewAllFragment : Fragment() {

    private lateinit var reviewAdapter: ReviewAdapter
    private val walkReviewViewModel: WalkReviewViewModel by viewModels()
    val placeId = arguments?.getInt("placeId") ?: 4

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mapwalking_reviewall, container, false)

        (activity as? AppCompatActivity)?.apply {
            val toolbar: Toolbar = view.findViewById(R.id.walking_review_toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.title = "이웃들의 후기"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.review_rv)
        recyclerView.layoutManager = LinearLayoutManager(context)

        if (placeId != -1) {
            walkReviewViewModel.getWalkReviews(placeId)
        }

        reviewAdapter = ReviewAdapter(emptyList())
        recyclerView.adapter = reviewAdapter

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

                reviewAdapter = ReviewAdapter(reviews)
                recyclerView.adapter = reviewAdapter
            } else {
                Log.e("WalkingReviewAll", "Failed to load reviews.")
            }
        }

        return view
    }
}
