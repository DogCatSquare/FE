package com.example.dogcatsquare.ui.map.walking

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
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.databinding.FragmentMapwalkingReviewallBinding
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkingListFragment : Fragment() {

    private var _binding: FragmentMapwalkingReviewallBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewAdapter: WalkRVAdapter
    private val walkReviewViewModel: WalkReviewViewModel by viewModels()
    private var walkId: Int? = null  // null 가능성 고려
    private var googlePlaceId: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            googlePlaceId = it.getString("googlePlaceId", "") ?: ""
            latitude = it.getDouble("latitude", 0.0)
            longitude = it.getDouble("longitude", 0.0)
        }
    }

    private fun setupRecyclerView() {
        reviewAdapter = WalkRVAdapter (
            onItemClick = { walkId ->
                val fragment = WalkingStartViewFragment.newInstance(walkId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            walkList = arrayListOf() // 초기 데이터로 빈 리스트 전달
        )
        binding.reviewRv.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchData()
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private fun fetchData() {
        // 전달받은 walkId를 사용하여 API 호출
        // WalkingMapFragment에서 넘겨줄 때 "walkId"라는 키를 사용했는지 확인 필요
        val name = arguments?.getString("walkName", "") ?: ""
        if (name != "") {
            loadPlaceDetails(name)
        } else {
            Log.e("ReviewAllFragment", "유효하지 않은 walkId")
        }
    }

    private fun loadPlaceDetails(name: String) {
        Log.d("WalkingMapFragment", "🚀 loadPlaceDetails 시작 - googlePlaceId: $googlePlaceId")

        lifecycleScope.launch {
            try {
                val walkResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.walkApiService.searchWalks(
                        title = name
                    )
                }

                // walkResponse 자체는 Boolean이 아니므로 데이터 존재 여부를 체크해야 합니다.
                if (walkResponse.walks.isNotEmpty()) {
                    binding.apply {
                        Log.d("ReviewAllFragment", "데이터 업데이트: ${walkResponse.walks.size}개")
                        // WalkSearchResponse 내부의 walks 리스트를 어댑터에 전달합니다.
                        reviewAdapter.updateData(ArrayList(walkResponse.walks))
                    }
                } else {
                    Log.d("ReviewAllFragment", "검색 결과가 없습니다.")
                    Toast.makeText(requireContext(), "추천 코스가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("WalkingMapFragment", "💥 예외 발생 (Exception): ${e.message}")
                handleError(e)
            }
        }
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
        Log.e("WalkingMapFragment", "Error: ", e)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapwalkingReviewallBinding.inflate(inflater, container, false)

        // 툴바 설정
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.walkingReviewToolbar)
            supportActionBar?.title = "이웃들의 추천코스"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            binding.walkingReviewToolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
