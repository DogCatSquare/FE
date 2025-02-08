package com.example.dogcatsquare.ui.map.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.map.MapButton
import com.example.dogcatsquare.data.map.MapPlace
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.databinding.FragmentMapBinding
import com.example.dogcatsquare.ui.map.SearchFragment
import com.example.dogcatsquare.ui.map.walking.WalkingStartViewFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.dogcatsquare.data.map.PlaceRequest
import com.example.dogcatsquare.data.map.RegionRequest
import com.example.dogcatsquare.data.map.SearchPlacesRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val buttonDatas by lazy { ArrayList<MapButton>() }
    private val originalPlaceDatas = ArrayList<MapPlace>()  // 원본 데이터 저장용
    private val placeDatas by lazy { ArrayList<MapPlace>() }

    // 위치 관련 변수
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private lateinit var sortTextView: TextView
    private var currentSortType = "주소기준"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sortTextView = binding.sortButton.findViewById(R.id.sortText)

        setupRecyclerView()
        setupBottomSheet()
        setupNaverMap()

        binding.filter.setOnClickListener {
            showSearchOptions()
        }

        binding.sortButton.setOnClickListener {
            val sortDialog = SortDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("currentSortType", currentSortType)
                }
            }
            sortDialog.show(childFragmentManager, "SortDialog")
        }

        binding.searchBox.setOnClickListener {
            val searchFragment = SearchFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .hide(this)  // 현재 Fragment 숨기기
                .add(R.id.main_frm, searchFragment)  // replace 대신 add 사용
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showSearchOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_search_option, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun setupNaverMap() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.mapView) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.mapView, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        // 위치 소스 지정
        naverMap.locationSource = locationSource

        // 현재 위치 버튼 활성화
        naverMap.uiSettings.isLocationButtonEnabled = true

        // 위치 추적 모드 설정
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 위치 권한 확인
        if (hasLocationPermission()) {
            enableCurrentLocation()
        } else {
            requestLocationPermission()
        }

//        loadPlaces()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun enableCurrentLocation() {
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults
            )) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            } else {
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupRecyclerView() {
        buttonDatas.clear()
        placeDatas.clear()
        originalPlaceDatas.clear()

        val dummyPlaces = listOf(
            MapPlace(
                placeName = "가나다 동물병원",
                placeType = "동물병원",
                placeDistance = "0.55km",
                placeLocation = "서울시 성북구 월곡동 77",
                placeCall = "02-1234-5678",
                char1Text = "중성화 수술",
                char2Text = "예방접종",
                char3Text = "24시",
                placeImg = null,
                placeImgUrl = null,
                placeReview = null,
                longitude = 127.0495556,
                latitude = 37.6074859,
                isOpen = "영업중"
            ),
            MapPlace(
                placeName = "서대문 안산자락길",
                placeType = "산책로",
                placeDistance = "0.55km",
                placeLocation = "서울시 서대문구 봉원사길 75-66",
                placeCall = "02-1234-5678",
                char1Text = "난이도 하",
                char2Text = "쓰레기통",
                char3Text = null,
                placeImg = null,
                placeImgUrl = null,
                placeReview = "리뷰(18)",
                longitude = 127.0495556,
                latitude = 37.6074859,
                isOpen = "영업중"
            ),
            MapPlace(
                placeName = "고양이호텔",
                placeType = "호텔",
                placeDistance = "0.55km",
                placeLocation = "서울시 성북구 월곡동 77",
                placeCall = "02-1234-5678",
                char1Text = "고양이탁묘",
                char2Text = "고양이 보호소",
                char3Text = null,
                placeImg = null,
                placeImgUrl = null,
                placeReview = "리뷰(18)",
                longitude = 127.0495556,
                latitude = 37.6074859,
                isOpen = "영업중"
            ),
            MapPlace(
                placeName = "반려동물 카페",
                placeType = "카페",
                placeDistance = "0.55km",
                placeLocation = "서울시 성북구 월곡동 77",
                placeCall = "02-1234-5678",
                char1Text = "중성화 수술",
                char2Text = "예방접종",
                char3Text = "24시",
                placeImg = null,
                placeImgUrl = null,
                placeReview = "리뷰(7)",
                longitude = 127.0495556,
                latitude = 37.6074859,
                isOpen = "영업중"
            ),
            MapPlace(
                placeName = "반려동물 식당",
                placeType = "식당",
                placeDistance = "0.55km",
                placeLocation = "서울시 성북구 월곡동 77",
                placeCall = "02-1234-5678",
                char1Text = "중성화 수술",
                char2Text = "예방접종",
                char3Text = "24시",
                placeImg = null,
                placeImgUrl = null,
                placeReview = "리뷰(7)",
                longitude = 127.0495556,
                latitude = 37.6074859,
                isOpen = "영업중"
            )
        )

        originalPlaceDatas.addAll(dummyPlaces)
        placeDatas.addAll(dummyPlaces)

        buttonDatas.apply {
            add(MapButton("전체"))
            add(MapButton("병원", R.drawable.btn_hospital))
            add(MapButton("산책로", R.drawable.btn_walk))
            add(MapButton("음식/카페", R.drawable.btn_restaurant))
            add(MapButton("호텔", R.drawable.btn_hotel))
        }

        val mapButtonRVAdapter = MapButtonRVAdapter(buttonDatas, object : MapButtonRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, buttonName: String) {
                Log.d("MapFragment", "버튼 클릭: $buttonName")
                Log.d("MapFragment", "원본 데이터 크기: ${originalPlaceDatas.size}")

                // 원본 데이터를 기준으로 필터링
                val filtered = when (buttonName) {
                    "전체" -> originalPlaceDatas
                    "병원" -> originalPlaceDatas.filter { it.placeType == "동물병원" }
                    "산책로" -> originalPlaceDatas.filter { it.placeType == "산책로" }
                    "음식/카페" -> originalPlaceDatas.filter { it.placeType in listOf("카페", "식당") }
                    "호텔" -> originalPlaceDatas.filter { it.placeType == "호텔" }
                    else -> originalPlaceDatas
                }

                Log.d("MapFragment", "필터링 결과 개수: ${filtered.size}")
                Log.d("MapFragment", "필터링된 장소들: ${filtered.map { it.placeName }}")

                // UI 업데이트
                (binding.mapPlaceRV.adapter as? MapPlaceRVAdapter)?.let { adapter ->
                    adapter.updateList(filtered)
                    Log.d("MapFragment", "어댑터 업데이트 완료")
                }
            }
        })

        binding.mapButtonRV.apply {
            adapter = mapButtonRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        val mapPlaceRVAdapter = MapPlaceRVAdapter(placeDatas, object : MapPlaceRVAdapter.OnItemClickListener {
            override fun onItemClick(place: MapPlace) {
                when (place.placeType) {
                    "동물병원" -> {
                        val fragment = MapDetailFragment().apply {
                            arguments = Bundle().apply {
                                putString("placeName", place.placeName)
                                putString("placeType", place.placeType)
                                putString("placeDistance", place.placeDistance)
                                putString("placeLocation", place.placeLocation)
                                putString("placeCall", place.placeCall)
                                putString("char1Text", place.char1Text)
                                putString("char2Text", place.char2Text)
                                putString("char3Text", place.char3Text)
                                place.placeImg?.let { putInt("placeImg", it) }
                            }
                        }
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                            )
                            .hide(this@MapFragment)  // 현재 Fragment 숨기기
                            .add(R.id.main_frm, fragment)  // 새 Fragment 추가
                            .addToBackStack(null)
                            .commit()
                    }
                    "산책로" -> {
                        val fragment = WalkingStartViewFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                            )
                            .hide(this@MapFragment)  // 현재 Fragment 숨기기
                            .add(R.id.main_frm, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    else -> {
                        val fragment = MapEtcFragment().apply {
                            arguments = Bundle().apply {
                                putString("placeName", place.placeName)
                                putString("placeType", place.placeType)
                                putString("placeDistance", place.placeDistance)
                                putString("placeLocation", place.placeLocation)
                                putString("placeCall", place.placeCall)
                                putString("char1Text", place.char1Text)
                                putString("char2Text", place.char2Text)
                                putString("char3Text", place.char3Text)
                                place.placeImg?.let { putInt("placeImg", it) }
                            }
                        }
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                            )
                            .hide(this@MapFragment)  // 현재 Fragment 숨기기
                            .add(R.id.main_frm, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
        })

        binding.mapPlaceRV.apply {
            adapter = mapPlaceRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun convertCategory(category: String): String {
        return when (category) {
            "HOSPITAL" -> "동물병원"
            "PARK" -> "산책로"
            "CAFE" -> "카페"
            "RESTAURANT" -> "식당"
            "HOTEL" -> "호텔"
            else -> category
        }
    }

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }


    private fun loadPlaces(keyword: String = "") {
        val token = getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!::naverMap.isInitialized) {
            Log.d("MapFragment", "지도가 아직 초기화되지 않았습니다.")
            return
        }

//        val regionId = 10000

        val regionId = getRegionId()
        if (regionId == -1) {
            Toast.makeText(requireContext(), "지역 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("MapFragment", "API 호출 - regionId: $regionId")

        lifecycleScope.launch {
            try {
                // 현재 지도의 중심 좌표 사용
                val mapCenter = naverMap.cameraPosition.target

                // 또는 현재 지도의 보이는 영역의 중심 좌표 사용
                val visibleRegion = naverMap.contentBounds
                val centerLatitude = (visibleRegion.northLatitude + visibleRegion.southLatitude) / 2
                val centerLongitude = (visibleRegion.eastLongitude + visibleRegion.westLongitude) / 2

                // 위치 정보 로깅
                Log.d("MapFragment", """
                ===== 위치 정보 =====
                지도 중심 좌표:
                - 위도: ${mapCenter.latitude}
                - 경도: ${mapCenter.longitude}
                
                보이는 영역 중심:
                - 위도: $centerLatitude
                - 경도: $centerLongitude
                
                현재 Region ID: $regionId
                ==================
            """.trimIndent())

                val searchRequest = SearchPlacesRequest(
                    userId = getUserId(),
                    // 지도의 현재 중심 좌표 사용
                    latitude = mapCenter.latitude,
                    longitude = mapCenter.longitude,
                    keyword = keyword
                )

                // API 요청 정보 로깅
                Log.d("MapFragment", """
                ===== API 요청 정보 =====
                Region ID: $regionId
                위도: ${searchRequest.latitude}
                경도: ${searchRequest.longitude}
                키워드: ${searchRequest.keyword}
                ====================
            """.trimIndent())

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.searchPlaces(
                        token = "Bearer $token",
                        regionId = regionId,
                        request = searchRequest
                    )
                }

                Log.d("MapFragment", """
                ===== API 응답 정보 =====
                성공 여부: ${response.isSuccess}
                응답 코드: ${response.code}
                메시지: ${response.message}
                데이터 개수: ${response.result?.size ?: 0}
                ====================
            """.trimIndent())

                when {
                    response.isSuccess -> {
                        // 응답 데이터 상세 로깅
                        Log.d("MapFragment", "받은 장소 개수: ${response.result?.size}")
                        response.result?.forEachIndexed { index, place ->
                            Log.d("MapFragment", "장소 $index: " +
                                    "이름=${place.name}, " +
                                    "카테고리=${place.category}, " +
                                    "주소=${place.address}, " +
                                    "거리=${place.distance}, " +
                                    "이미지URL=${place.imgUrl}")
                        }

                        val mapPlaces = response.result?.map { place ->
                            MapPlace(
                                placeName = place.name,
                                placeType = convertCategory(place.category),
                                placeDistance = "${String.format("%.2f", place.distance)}km",
                                placeLocation = place.address,
                                placeCall = place.phoneNumber,
                                char1Text = null,
                                char2Text = null,
                                char3Text = if (place.open) "영업중" else "영업종료",
                                placeImg = null,  // 서버에서 제공하는 이미지 URL 사용
                                placeImgUrl = place.imgUrl,  // 이미지 URL 추가
                                placeReview = null,
                                longitude = place.longitude,
                                latitude = place.latitude,
                                isOpen = if (place.open) "영업중" else "영업종료"
                            )
                        } ?: emptyList()

                        Log.d("MapFragment", "변환된 MapPlace 개수: ${mapPlaces.size}")

                        // UI 업데이트
                        withContext(Dispatchers.Main) {
                            // 원본 데이터 업데이트
                            originalPlaceDatas.clear()
                            originalPlaceDatas.addAll(mapPlaces)

                            // 현재 표시 중인 데이터 업데이트
                            placeDatas.clear()
                            placeDatas.addAll(mapPlaces)

                            // RecyclerView 어댑터 업데이트
                            binding.mapPlaceRV.adapter?.notifyDataSetChanged()

                            Toast.makeText(
                                requireContext(),
                                "총 ${mapPlaces.size}개의 장소를 불러왔습니다.",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.d("MapFragment", "데이터 업데이트 완료 - 원본: ${originalPlaceDatas.size}, 표시: ${placeDatas.size}")
                        }
                    }
                    else -> {
                        Log.e("MapFragment", "API 응답 실패: ${response.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "데이터 로드 실패: ${response.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun getUserId(): Int {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getInt("user_id", -1) ?: -1
    }

    private fun getRegionId(): Int {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val regionId = sharedPref?.getInt("region_id", -1) ?: -1

        Log.d("MapFragment", "Retrieved regionId from SharedPreferences: $regionId")

        return regionId
    }

    private fun handleError(e: Exception) {
        val errorMessage = when (e) {
            is HttpException -> {
                when (e.code()) {
                    401 -> "로그인이 필요합니다."
                    403 -> "권한이 없습니다."
                    404 -> "데이터를 찾을 수 없습니다."
                    else -> "서버 오류가 발생했습니다. (${e.code()})"
                }
            }
            is IOException -> "네트워크 연결을 확인해주세요."
            else -> "알 수 없는 오류가 발생했습니다: ${e.message}"
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        Log.e("MapFragment", "API 오류", e)
    }


    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        binding.root.post {
            val mapButtonBottom = binding.mapButtonRV.bottom +
                    (binding.mapButtonRV.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            bottomSheetBehavior.maxHeight = binding.root.height - mapButtonBottom
        }

        bottomSheetBehavior.apply {
            isDraggable = true
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // BottomBar 이미지만 변경
                        binding.bottomBar.setImageResource(R.drawable.ic_map_contour_place)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // BottomSheet가 접힐 때 원래 이미지로 복원
                        binding.bottomBar.setImageResource(R.drawable.ic_bar)
                        // MapView 표시
                        binding.mapView.animate()
                            .alpha(1f)
                            .setDuration(1000)
                            .start()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // MapView 페이드아웃 애니메이션
                binding.mapView.alpha = 1 - slideOffset

                // BottomBar 이미지 전환을 부드럽게 처리
                if (slideOffset > 0.5 && binding.bottomBar.tag != "changed") {
                    binding.bottomBar.setImageResource(R.drawable.ic_map_contour_place)
                    binding.bottomBar.tag = "changed"
                } else if (slideOffset <= 0.5 && binding.bottomBar.tag == "changed") {
                    binding.bottomBar.setImageResource(R.drawable.ic_bar)
                    binding.bottomBar.tag = null
                }
            }
        })
    }

    fun updateSortText(sortType: String) {
        currentSortType = sortType // 현재 정렬 상태 저장
        activity?.runOnUiThread {
            try {
                sortTextView.text = sortType
            } catch (e: Exception) {
                Log.e("MapFragment", "Error updating sort text: ${e.message}")
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // 상태가 복원된 후에 필요한 UI 업데이트
        savedInstanceState?.let { bundle ->
            currentSortType = bundle.getString("currentSortType", "주소기준")
            sortTextView.text = currentSortType
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 현재 상태 저장
        outState.putString("currentSortType", currentSortType)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
