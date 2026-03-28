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
import com.example.dogcatsquare.LoadingDialog
// ---
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkingMapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapwalkingBinding? = null
    private val binding get() = _binding!!

    private lateinit var walkRVAdapter: WalkRVAdapter
    private var googlePlaceId: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    // [수정됨] NaverMap -> GoogleMap
    private var googleMap: GoogleMap? = null
    // [수정됨] Naver Marker -> Google Marker
    private var currentMarker: Marker? = null
    private var isWished = false
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
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
        _binding = FragmentMapwalkingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [수정됨] setupNaverMap -> setupGoogleMap
        setupGoogleMap()
        setupRecyclerView()
        setupButtons()

        if (googlePlaceId != "") {
            loadPlaceDetails(googlePlaceId)
        }
    }

    // [수정됨] Naver Map 설정 -> Google Map 설정
    private fun setupGoogleMap() {
        // XML에 <fragment>로 정의된 SupportMapFragment를 찾습니다.
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView3) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun setupRecyclerView() {
        walkRVAdapter = WalkRVAdapter (
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
            adapter = walkRVAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupButtons() {
        binding.apply {
            backButton.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            // 산책로
            reviewAllBt.setOnClickListener {
                val fragment = WalkingListFragment().apply {
                    arguments = Bundle().apply {
                        // 현재 상세 페이지의 walkId 또는 placeId를 넘겨줘야 함
                        putString("walkName", binding.placeName.text.toString())
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            addButton.setOnClickListener {
                // 1. 현재 화면(binding)에 세팅된 장소 이름 가져오기
                val currentPlaceName = binding.placeName.text.toString()

                // 2. 다음 화면으로 넘어갈 때 Bundle에 장소 이름 담아주기
                val fragment = WalkingMapViewFragment().apply {
                    arguments = Bundle().apply {
                        putString("placeName", currentPlaceName) // "placeName" 이라는 이름으로 저장
                    }
                }

                // 3. 화면 전환
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            wishButton.setOnClickListener {
                toggleWish(googlePlaceId)
            }
        }
    }

    private fun loadPlaceDetails(googlePlaceId: String) {
        Log.d("WalkingMapFragment", "🚀 loadPlaceDetails 시작 - googlePlaceId: $googlePlaceId")
        if (!loadingDialog.isDialogShowing) loadingDialog.show()

        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Log.e("WalkingMapFragment", "❌ 토큰 없음: 로그인이 필요함")
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val request = PlaceDetailRequest(
                    latitude = latitude,
                    longitude = longitude
                )

                Log.d("WalkingMapFragment", "📡 장소 상세정보 요청 중... 위도: $latitude, 경도: $longitude")
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.getPlaceById(
                        token = "Bearer $token",
                        googlePlaceId = googlePlaceId,
                        request = request
                    )
                }

                if (response.isSuccess) {
                    response.result?.let { placeDetail ->
                        // 1. 상단 장소 정보 즉시 업데이트
                        binding.apply {
                            placeName.text = placeDetail.name
                            placeLocation.text = placeDetail.address.split(" ").getOrNull(2) ?: ""
                            placeDistance.text = "${String.format("%.2f", placeDetail.distance)}km"
                            addressTv.text = placeDetail.address
                            
                            isWished = placeDetail.wished
                            wishButton.setImageResource(
                                if (isWished) R.drawable.ic_wish_check
                                else R.drawable.ic_wish
                            )
                        }

                        if (googleMap != null) {
                            updateMapLocation(placeDetail.latitude, placeDetail.longitude)
                        }

                        // 2. 산책로 목록 검색 (별도 에러 처리로 장소 정보 표시를 방해하지 않음)
                        try {
                            Log.d("WalkingMapFragment", "📡 산책로 목록 검색 요청 중 (키워드: ${placeDetail.name})")
                            val walkResponse = withContext(Dispatchers.IO) {
                                RetrofitClient.walkApiService.searchWalks(
                                    title = placeDetail.name
                                )
                            }
                            Log.d("WalkingMapFragment", "✅ 산책로 목록 수신 성공: ${walkResponse.walks.size}개 발견")

                            binding.apply {
                                placeType.text = "리뷰(${walkResponse.walks.size})"
                                rightText.text = walkResponse.walks.size.toString()

                                if (walkResponse.walks.isEmpty()) {
                                    reviewRv.visibility = View.GONE
                                    defaultWalkText.visibility = View.VISIBLE
                                    rightText.visibility = View.GONE
                                    reviewAllBt.visibility = View.GONE
                                } else {
                                    reviewRv.visibility = View.VISIBLE
                                    defaultWalkText.visibility = View.GONE
                                    rightText.visibility = View.VISIBLE
                                    reviewAllBt.visibility = View.VISIBLE
                                    walkRVAdapter.updateData(ArrayList(walkResponse.walks))
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("WalkingMapFragment", "⚠️ 산책로 검색 실패: ${e.message}")
                            binding.apply {
                                placeType.text = "리뷰(0)"
                                reviewRv.visibility = View.GONE
                                defaultWalkText.visibility = View.VISIBLE
                            }
                        }
                    }
                } else {
                    Log.e("WalkingMapFragment", "❌ 서버 에러 응답: ${response.message}")
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "상세 정보를 불러오는데 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("WalkingMapFragment", "💥 예외 발생 (Exception): ${e.message}")
                handleError(e)
            } finally {
                if (loadingDialog.isDialogShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun toggleWish(googlePlaceId: String) {
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
                        googlePlaceId = googlePlaceId
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
        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                15.0f
            )
        )
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

    override fun onStop() {
        super.onStop()
        if (::loadingDialog.isInitialized && loadingDialog.isDialogShowing) {
            loadingDialog.dismiss()
        }
    }

    companion object {
        fun newInstance(googlePlaceId: String, latitude: Double, longitude: Double): WalkingMapFragment {
            return WalkingMapFragment().apply {
                arguments = Bundle().apply {
                    putString("googlePlaceId", googlePlaceId)
                    putDouble("latitude", latitude)
                    putDouble("longitude", longitude)
                }
            }
        }
    }
}