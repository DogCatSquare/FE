package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.walk.ReportRequest
import com.example.dogcatsquare.data.model.walk.Walk
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.databinding.FragmentMapwalkingReviewallBinding
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkingListFragment : Fragment() {

    private var _binding: FragmentMapwalkingReviewallBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewAdapter: WalkRVAdapter
    private val walkReviewViewModel: WalkReviewViewModel by viewModels()
    private var walkId: Int? = null
    private var googlePlaceId: String = ""

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    data class ErrorResponse(
        val isSuccess: Boolean? = null,
        val code: String? = null,
        val message: String? = null,
        val result: Any? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            googlePlaceId = it.getString("googlePlaceId", "") ?: ""
            latitude = it.getDouble("latitude", 0.0)
            longitude = it.getDouble("longitude", 0.0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapwalkingReviewallBinding.inflate(inflater, container, false)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchData()
    }

    private fun setupRecyclerView() {
        reviewAdapter = WalkRVAdapter(
            onItemClick = { walkId ->
                val fragment = WalkingStartViewFragment.newInstance(walkId, googlePlaceId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onMenuClick = { walk, anchorView ->
                showWalkMenu(walk, anchorView)
            },
            walkList = arrayListOf()
        )

        binding.reviewRv.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showWalkMenu(walk: Walk, anchorView: View) {
        val popupMenu = PopupMenu(requireContext(), anchorView)
        popupMenu.menuInflater.inflate(R.menu.walk_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    AlertDialog.Builder(requireContext())
                        .setMessage("이 산책로를 삭제할까요?")
                        .setPositiveButton("삭제") { _, _ ->
                            deleteWalk(walk.walkId)
                        }
                        .setNegativeButton("취소", null)
                        .show()
                    true
                }

                R.id.action_report -> {
                    showReportTypeDialog(walk.walkId)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showReportTypeDialog(walkId: Int) {
        val reportLabels = arrayOf(
            "홍보성",
            "욕설 비방 혐오",
            "음란 선정성",
            "도배",
            "개인정보 노출",
            "기타"
        )

        val reportTypes = arrayOf(
            "ADVERTISEMENT",
            "ABUSE_HATE_SPEECH",
            "ADULT_CONTENT",
            "SPAM",
            "PERSONAL_INFO",
            "OTHER"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("신고 사유를 선택하세요")
            .setItems(reportLabels) { _, which ->
                val selectedType = reportTypes[which]

                if (selectedType == "OTHER") {
                    reportWalk(walkId, selectedType, "기타")
                } else {
                    reportWalk(walkId, selectedType, null)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private fun fetchData() {
        val name = arguments?.getString("walkName", "") ?: ""
        if (name.isNotEmpty()) {
            loadPlaceDetails(name)
        } else {
            Log.e("WalkingListFragment", "유효하지 않은 walkName")
        }
    }

    private fun loadPlaceDetails(name: String) {
        lifecycleScope.launch {
            try {
                val walkResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.walkApiService.searchWalks(title = name)
                }

                if (walkResponse.walks.isNotEmpty()) {
                    Log.d("WalkingListFragment", "데이터 업데이트: ${walkResponse.walks.size}개")
                    reviewAdapter.updateData(ArrayList(walkResponse.walks))
                } else {
                    Log.d("WalkingListFragment", "검색 결과가 없습니다.")
                    reviewAdapter.updateData(arrayListOf())
                    Toast.makeText(requireContext(), "추천 코스가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("WalkingListFragment", "💥 예외 발생: ${e.message}", e)
                handleError(e)
            }
        }
    }

    private fun deleteWalk(walkId: Int) {
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.walkApiService.deleteWalk(
                        token = "Bearer $token",
                        walkId = walkId
                    )
                }

                if (response.isSuccess) {
                    Toast.makeText(requireContext(), "산책로가 삭제되었습니다.", Toast.LENGTH_SHORT).show()

                    val name = arguments?.getString("walkName", "") ?: ""
                    if (name.isNotEmpty()) {
                        loadPlaceDetails(name)
                    } else {
                        reviewAdapter.updateData(arrayListOf())
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "산책로 삭제에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("WalkingListFragment", "삭제 중 예외 발생: ${e.message}", e)
                handleError(e)
            }
        }
    }

    private fun reportWalk(walkId: Int, reportType: String, otherReason: String?) {
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.walkApiService.reportWalk(
                        token = "Bearer $token",
                        walkId = walkId,
                        body = ReportRequest(
                            reportType = reportType,
                            otherReason = otherReason
                        )
                    )
                }

                Toast.makeText(
                    requireContext(),
                    response.message ?: "산책로가 신고되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Log.e("WalkingListFragment", "신고 중 예외 발생: ${e.message}", e)
                handleError(e)
            }
        }
    }

    private fun parseErrorMessage(e: retrofit2.HttpException): String? {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody.isNullOrBlank()) return null

            Gson().fromJson(errorBody, ErrorResponse::class.java)?.message
        } catch (ex: Exception) {
            null
        }
    }

    private fun handleError(e: Exception) {
        val errorMessage = when (e) {
            is retrofit2.HttpException -> {
                parseErrorMessage(e) ?: when (e.code()) {
                    401 -> "잘못된 토큰입니다."
                    403 -> "권한이 없습니다."
                    404 -> "산책로를 찾을 수 없습니다."
                    else -> "서버 오류가 발생했습니다. (${e.code()})"
                }
            }
            is java.io.IOException -> "네트워크 연결을 확인해주세요."
            else -> "알 수 없는 오류가 발생했습니다: ${e.message}"
        }

        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        Log.e("WalkingListFragment", "Error: ", e)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}