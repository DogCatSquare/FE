package com.example.dogcatsquare.ui.map.walking

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.FragmentMapwalkingReviewallBinding
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel

class WalkingMapReviewAllFragment : Fragment() {

    private var _binding: FragmentMapwalkingReviewallBinding? = null
    private val binding get() = _binding!!

    private val walkReviewViewModel: WalkReviewViewModel by viewModels()

    private var placeId: Int = -1
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    private val reviewDatas = ArrayList<WalkReview>()
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            placeId = it.getInt("placeId", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapwalkingReviewallBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()

        if (placeId != -1) {
            loadReviews()
        }

        walkReviewViewModel.reviewResponse.observe(viewLifecycleOwner) { response ->
            response?.result?.walkReviews?.let { newReviews ->
                if (newReviews.isEmpty()) {
                    isLastPage = true
                } else {
                    reviewDatas.addAll(newReviews)
                    reviewAdapter.notifyDataSetChanged()
                    currentPage++
                }
                isLoading = false
            } ?: Log.e("WalkingReviewAll", "Failed to load reviews.")
        }
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.walkingReviewToolbar)
            supportActionBar?.title = "이웃들의 후기"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            binding.walkingReviewToolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter(reviewDatas)
        binding.reviewRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (!isLoading && !isLastPage && lastVisibleItemPosition == totalItemCount - 1) {
                        loadReviews()
                    }
                }
            })
        }
    }

    private fun loadReviews() {
        isLoading = true
        walkReviewViewModel.getWalkReviews(placeId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
