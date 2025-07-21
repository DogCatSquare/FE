package com.example.dogcatsquare.ui.map.location

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.data.model.map.MapReview
import com.example.dogcatsquare.databinding.FragmentMapReviewBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapReviewFragment : Fragment() {
    private var _binding: FragmentMapReviewBinding? = null
    private val binding get() = _binding!!

    private var placeId: Int = -1
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private val reviewDatas = ArrayList<MapReview>()
    private lateinit var reviewAdapter: MapReviewRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            placeId = it.getInt(ARG_PLACE_ID, -1)
        }
    }

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
        loadReviews()
    }

    private fun setupRecyclerView() {
        lifecycleScope.launch {
            val nickname = fetchUserNickname()

            reviewAdapter = MapReviewRVAdapter(
                reviewList = reviewDatas,
                currentUserNickname = nickname,
                onReviewDeleted = {
                    // 리뷰가 삭제되면 현재 페이지부터 다시 로드
                    currentPage = 0
                    loadReviews()

                    // MapDetailFragment도 새로고침
                    requireActivity().supportFragmentManager.fragments
                        .filterIsInstance<MapDetailFragment>()
                        .firstOrNull()?.refreshPlaceDetails()
                }
            )

            binding.reviewRV.apply {
                adapter = reviewAdapter
                layoutManager = LinearLayoutManager(context)

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                        if (!isLoading && !isLastPage) {
                            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                            ) {
                                loadMoreReviews()
                            }
                        }
                    }
                })
            }
        }
    }

    private fun loadReviews() {
        if (placeId == -1) {
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.getReviews(
                        token = "Bearer $token",
                        placeId = placeId,
                        page = currentPage
                    )
                }

                if (response.isSuccess) {
                    response.result?.let { pageResponse ->
                        if (currentPage == 0) {
                            reviewDatas.clear()
                        }

                        pageResponse.content?.let { reviews ->
                            reviewDatas.addAll(reviews)
                            reviewAdapter.notifyDataSetChanged()
                        }

                        isLastPage = pageResponse.last
                        if (!isLastPage) {
                            currentPage++
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "리뷰를 불러오는데 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadMoreReviews() {
        if (!isLoading && !isLastPage) {
            loadReviews()
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.fragments
                .filterIsInstance<MapDetailFragment>()
                .firstOrNull()?.let { detailFragment ->
                    requireActivity().supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_left,
                            R.anim.slide_out_right,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                        )
                        .show(detailFragment)
                        .commit()
                }
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupAddReviewButton() {
        binding.addReview.setOnClickListener {
            val mapAddReviewFragment = MapAddReviewFragment.newInstance(placeId)
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .hide(this)
                .add(R.id.main_frm, mapAddReviewFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private suspend fun fetchUserNickname(): String {
        try {
            val token = getToken() ?: run {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return ""
            }

            val response = withContext(Dispatchers.IO) {
                RetrofitClient.userApiService.getUser("Bearer $token").execute()
            }

            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null && userResponse.isSuccess) {
                    return userResponse.result?.nickname ?: ""
                }
            }
        } catch (e: Exception) {
            Log.e("MapReviewFragment", "사용자 정보 가져오기 중 예외 발생", e)
        }
        return ""
    }

    private fun handleError(e: Exception) {
        val errorMessage = when (e) {
            is retrofit2.HttpException -> {
                when (e.code()) {
                    401 -> "로그인이 필요합니다."
                    403 -> "권한이 없습니다."
                    404 -> "데이터를 찾을 수 없습니다."
                    else -> "서버 오류가 발생했습니다. (${e.code()})"
                }
            }
            is java.io.IOException -> "네트워크 연결을 확인해주세요."
            else -> "알 수 없는 오류가 발생했습니다: ${e.message}"
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().supportFragmentManager.fragments
            .filterIsInstance<MapDetailFragment>()
            .firstOrNull()?.let { detailFragment ->
                requireActivity().supportFragmentManager.beginTransaction()
                    .show(detailFragment)
                    .commit()
            }
        _binding = null
    }

    companion object {
        private const val ARG_PLACE_ID = "placeId"

        fun newInstance(placeId: Int) = MapReviewFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PLACE_ID, placeId)
            }
        }
    }
}