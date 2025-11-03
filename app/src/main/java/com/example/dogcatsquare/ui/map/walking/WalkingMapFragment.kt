package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.data.model.map.PlaceDetailRequest
import com.example.dogcatsquare.databinding.FragmentMapwalkingBinding
// Naver Map import 제거
// import com.naver.maps.geometry.LatLng
// import com.naver.maps.map.CameraUpdate
// import com.naver.maps.map.MapFragment
// import com.naver.maps.map.NaverMap
// import com.naver.maps.map.OnMapReadyCallback
// import com.naver.maps.map.overlay.Marker
// import com.naver.maps.map.overlay.OverlayImage

// Google Map import 추가
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
// ---
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkingMapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapwalkingBinding? = null
    private val binding get() = _binding!!

    private lateinit var walkRVAdapter: WalkRVAdapter
    private var placeId: Int = -1
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    // [수정됨] NaverMap -> GoogleMap
    private var googleMap: GoogleMap? = null
    // [수정됨] Naver Marker -> Google Marker
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [수정됨] setupNaverMap -> setupGoogleMap
        setupGoogleMap()
        setupRecyclerView()
        setupButtons()

        if (placeId != -1) {
            loadPlaceDetails(placeId)
        }
    }

    // [수정됨] Naver Map 설정 -> Google Map 설정
    private fun setupGoogleMap() {
        // XML에 <fragment>로 정의된 SupportMapFragment를 찾습니다.
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView3) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun setupRecyclerView() {
        walkRVAdapter = WalkRVAdapter { walkId ->
            val fragment = WalkingStartViewFragment.newInstance(walkId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }
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

                            // RecyclerView에 데이터 설정
                            walkRVAdapter.updateData(walkResponse.walks)

                            isWished = placeDetail.wished
                            wishButton.setImageResource(
                                if (isWished) R.drawable.ic_wish_check
                                else R.drawable.ic_wish
                            )
                        }

                        // [수정됨] naverMap.isInitialized -> googleMap != null
                        if (googleMap != null) {
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

    // [수정됨] onMapReady(NaverMap) -> onMapReady(GoogleMap)
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            uiSettings.apply {
                // NaverMap과 동일하게 모든 UI 설정 및 제스처 비활성화
                isZoomControlsEnabled = false
                isScrollGesturesEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isZoomGesturesEnabled = false
                isMapToolbarEnabled = false // Google Map의 경우 툴바도 비활성화
            }
        }
        updateMapLocation(latitude, longitude)
    }

    // [수정됨] Naver Map API -> Google Map API
    private fun updateMapLocation(lat: Double, lng: Double) {
        // Google Map의 LatLng 사용
        val location = LatLng(lat, lng)

        // Google Map 마커 제거 방식
        currentMarker?.remove()

        // Google Map 마커 추가 방식
        val icon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker)
        currentMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .icon(icon)
        )

        // Google Map 카메라 이동 방식
        googleMap?.moveCamera(CameraUpdateFactory.newLatLng(location))
    }

    // [추가됨] MapFragment.kt와 동일한 마커 아이콘 변환 헬퍼 함수
    private fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
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
        googleMap = null // [추가됨] 맵 리소스 해제
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