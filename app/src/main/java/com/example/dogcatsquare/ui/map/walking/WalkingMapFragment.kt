package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.data.map.PlaceDetailRequest
import com.example.dogcatsquare.databinding.FragmentMapwalkingBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.constraintlayout.widget.ConstraintSet
import com.example.dogcatsquare.ui.map.SearchFragment

class WalkingMapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapwalkingBinding? = null
    private val binding get() = _binding!!

    private lateinit var walkRVAdapter: WalkRVAdapter
    private var placeId: Int = -1
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var naverMap: NaverMap
    private var currentMarker: Marker? = null
    private var isWished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            placeId = it.getInt("placeId", -1)
            latitude = it.getDouble("latitude", 0.0)
            longitude = it.getDouble("longitude", 0.0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapwalkingBinding.inflate(inflater, container, false)

        binding.searchBox.setOnClickListener {
            val searchFragment = SearchFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, searchFragment)
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNaverMap()
        setupRecyclerView()
        setupButtons()

        if (placeId != -1) {
            loadPlaceDetails(placeId)
        }
    }

    private fun setupNaverMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView3) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.mapView3, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    private fun setupRecyclerView() {
        walkRVAdapter = WalkRVAdapter()
        binding.reviewRv.apply {
            adapter = walkRVAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupButtons() {
        binding.apply {
            backButton.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            reviewAllBt.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, WalkingReviewAllFragment())
                    .addToBackStack(null)
                    .commit()
            }

            addButton.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, WalkingMapViewFragment())
                    .addToBackStack(null)
                    .commit()
            }

            wishButton.setOnClickListener {
                toggleWish(placeId)
            }
        }
    }

    private fun loadPlaceDetails(placeId: Int) {
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val request = PlaceDetailRequest(
                    latitude = latitude,
                    longitude = longitude
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.getPlaceById(
                        token = "Bearer $token",
                        placeId = placeId,
                        request = request
                    )
                }

                if (response.isSuccess) {
                    response.result?.let { placeDetail ->
                        // 산책로 검색 수행
                        val walkResponse = withContext(Dispatchers.IO) {
                            RetrofitClient.walkApiService.searchWalks(
                                title = placeDetail.name
                            )
                        }

                        binding.apply {
                            placeName.text = placeDetail.name
                            placeLocation.text = placeDetail.address.split(" ").getOrNull(2) ?: ""
                            placeType.text = "리뷰(${walkResponse.walks.size})"
                            placeDistance.text = "${String.format("%.2f", placeDetail.distance)}km"
                            addressTv.text = placeDetail.address
                            rightText.text = walkResponse.walks.size.toString()

                            // 산책로 데이터 유무에 따른 뷰 처리
                            if (walkResponse.walks.isEmpty()) {
                                // 산책로가 없는 경우
                                reviewRv.visibility = View.GONE
                                defaultWalkImage.visibility = View.VISIBLE
                                defaultWalkText.visibility = View.VISIBLE
                                reviewAllBt.visibility = View.GONE

                                // 제약 조건 변경
                                val constraintSet = ConstraintSet()
                                constraintSet.clone(scrollView3.getChildAt(0) as ConstraintLayout)
                                constraintSet.connect(
                                    R.id.addButton,
                                    ConstraintSet.TOP,
                                    R.id.defaultWalkImage,
                                    ConstraintSet.BOTTOM,
                                    (24 * resources.displayMetrics.density).toInt()
                                )
                                constraintSet.applyTo(scrollView3.getChildAt(0) as ConstraintLayout)
                            } else {
                                // 산책로가 있는 경우
                                reviewRv.visibility = View.VISIBLE
                                defaultWalkImage.visibility = View.GONE
                                defaultWalkText.visibility = View.GONE
                                reviewAllBt.visibility = View.VISIBLE
                                walkRVAdapter.updateData(walkResponse.walks)
                            }

                            isWished = placeDetail.wished
                            wishButton.setImageResource(
                                if (isWished) R.drawable.ic_wish_check
                                else R.drawable.ic_wish
                            )
                        }

                        if (::naverMap.isInitialized) {
                            updateMapLocation(placeDetail.latitude, placeDetail.longitude)
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "상세 정보를 불러오는데 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun toggleWish(placeId: Int) {
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.toggleWish(
                        token = "Bearer $token",
                        placeId = placeId
                    )
                }

                if (response.isSuccess) {
                    isWished = response.result ?: !isWished
                    binding.wishButton.setImageResource(
                        if (isWished) R.drawable.ic_wish_check
                        else R.drawable.ic_wish
                    )

                    Toast.makeText(
                        requireContext(),
                        if (isWished) "찜 목록에 추가되었습니다."
                        else "찜 목록에서 제거되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "요청 처리에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.apply {
            uiSettings.apply {
                isZoomControlEnabled = false
                isScrollGesturesEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isZoomGesturesEnabled = false
            }
        }
        updateMapLocation(latitude, longitude)
    }

    private fun updateMapLocation(lat: Double, lng: Double) {
        val location = LatLng(lat, lng)

        currentMarker?.map = null
        currentMarker = Marker().apply {
            position = location
            icon = OverlayImage.fromResource(R.drawable.ic_marker_park)
            map = naverMap
        }

        naverMap.moveCamera(CameraUpdate.scrollTo(location))
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
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

    private fun convertCategory(category: String): String {
        return when (category) {
            "HOSPITAL" -> "동물병원"
            "HOTEL" -> "호텔"
            "RESTAURANT" -> "식당"
            "CAFE" -> "카페"
            "ETC" -> "기타"
            else -> category
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(placeId: Int, latitude: Double, longitude: Double): WalkingMapFragment {
            return WalkingMapFragment().apply {
                arguments = Bundle().apply {
                    putInt("placeId", placeId)
                    putDouble("latitude", latitude)
                    putDouble("longitude", longitude)
                }
            }
        }
    }
}