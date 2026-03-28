package com.example.dogcatsquare.ui.map.walking

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.map.MapReview
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.databinding.FragmentMapReviewBinding
import com.example.dogcatsquare.databinding.FragmentMapwalkingReviewallBinding
import com.example.dogcatsquare.ui.map.location.MapAddReviewFragment
import com.example.dogcatsquare.ui.map.location.MapDetailFragment
import com.example.dogcatsquare.ui.map.location.MapReviewFragment
import com.example.dogcatsquare.ui.map.location.MapReviewRVAdapter
import com.example.dogcatsquare.LoadingDialog
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkDetailViewModel
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkingReviewListFragment : Fragment() {
    private var _binding: FragmentMapReviewBinding? = null
    private val binding get() = _binding!!

    private val walkReviewViewModel: WalkReviewViewModel by viewModels()
    private var walkId: Int = -1
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private val reviewDatas = ArrayList<WalkReview>()
    private lateinit var reviewAdapter: WalkingReviewListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            walkId = it.getInt(ARG_WALK_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapReviewBinding.inflate(inflater, container, false)

        walkId = arguments?.getInt("walkId", -1) ?: -1

        walkId?.let {
            walkReviewViewModel.getWalkReviews(it)
            Log.d("WalkingStartView", it.toString())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupRecyclerView()
        setupAddReviewButton()

        observeViewModel()

        loadReviews()
    }

    private fun setupRecyclerView() {
        reviewAdapter = WalkingReviewListAdapter(
            reviewList = reviewDatas,
//            currentUserNickname = "",
//            onReviewDeleted = {
//                // 리뷰가 삭제되면 현재 페이지부터 다시 로드
//                currentPage = 0
//                loadReviews()
//
//                // MapDetailFragment도 새로고침
//                requireActivity().supportFragmentManager.fragments
//                    .filterIsInstance<WalkingStartViewFragment>()
//                    .firstOrNull()?.refreshPlaceDetails()
//            }
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

        // 닉네임은 별도로 업데이트
//        lifecycleScope.launch {
//            val nickname = fetchUserNickname()
//            reviewAdapter.updateNickname(nickname) // 어댑터에 닉네임 업데이트 메서드 추가 권장
//        }
    }

    private fun observeViewModel() {
        walkReviewViewModel.reviewResponse.observe(viewLifecycleOwner) { response ->
            if (response?.isSuccess == true) {
                val reviews = response.result?.walkReviews?.map { walkReview ->
                    WalkReview(
                        reviewId = walkReview.reviewId,
                        walkId = walkId.toLong(),
                        content = walkReview.content,
                        walkReviewImageUrl = walkReview.walkReviewImageUrl,
                        createdAt = walkReview.createdAt,
                        updatedAt = walkReview.updatedAt,
                        createdBy = walkReview.createdBy
                    )
                } ?: emptyList()

                // 어댑터 내부에 데이터를 교체하는 함수를 만들어서 호출하세요.
                reviewAdapter.updateReviews(reviews)

                reviewAdapter = WalkingReviewListAdapter(ArrayList(reviews))
                binding.reviewRV.adapter = reviewAdapter
                reviewAdapter.notifyDataSetChanged()
            }
            isLoading = false
        }
    }

    private fun loadReviews() {
        if (walkId == -1) return
        isLoading = true
        walkReviewViewModel.getWalkReviews(walkId)
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
            val walkingReviewAddFragment = WalkingReviewAddFragment.newInstance(walkId)
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .hide(this)
                .add(R.id.main_frm, walkingReviewAddFragment)
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
        private const val ARG_WALK_ID = "walkId"

        fun newInstance(walkId: Int) = WalkingReviewListFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_WALK_ID, walkId)
            }
        }
    }
}
