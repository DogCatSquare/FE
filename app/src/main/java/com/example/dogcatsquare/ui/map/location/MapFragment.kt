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
import com.example.dogcatsquare.data.map.SearchPlacesRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
            val currentLocation = naverMap.cameraPosition.target
            val searchFragment = SearchFragment().apply {
                arguments = Bundle().apply {
                    putDouble("latitude", currentLocation.latitude)
                    putDouble("longitude", currentLocation.longitude)
                }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .hide(this)
                .add(R.id.main_frm, searchFragment)
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

        lifecycleScope.launch {
            loadAllCategories()
        }
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
                id = 0,
                placeName = "가나다 동물병원",
                placeType = "동물병원",
                placeDistance = "0.55km",
                placeLocation = "서울시 성북구 월곡동 77",
                placeCall = "02-1234-5678",
                placeImgUrl = null,
                isOpen = "영업중",
            ),
            MapPlace(
                id = 0,
                placeName = "서대문 안산자락길",
                placeType = "산책로",
                placeDistance = "0.55km",
                placeLocation = "서울시 서대문구 봉원사길 75-66",
                placeCall = "02-1234-5678",
                placeImgUrl = null,
                isOpen = "영업중",
                reviewCount = 2
            ),
            MapPlace(
                id = 0,
                placeName = "고양이호텔",
                placeType = "호텔",
                placeDistance = "0.55km",
                placeLocation = "서울시 성북구 월곡동 77",
                placeCall = "02-1234-5678",
                placeImgUrl = null,
                isOpen = "영업중",
                reviewCount = 13
            ),
            MapPlace(
                id = 0,
                placeName = "반려동물 카페",
                placeType = "카페",
                placeDistance = "0.55km",
                placeLocation = "서울시 성북구 월곡동 77",
                placeCall = "02-1234-5678",
                placeImgUrl = null,
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
                        val (currentLat, currentLng) = getMapCurrentPosition()
                        val fragment = MapDetailFragment().apply {
                            arguments = Bundle().apply {
                                putInt("placeId", place.id)
                                putDouble("latitude", currentLat)
                                putDouble("longitude", currentLng)
                            }
                        }
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_right
                            )
                            .hide(this@MapFragment)
                            .add(R.id.main_frm, fragment)
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
                        val (currentLat, currentLng) = getMapCurrentPosition()
                        val fragment = MapEtcFragment().apply {
                            arguments = Bundle().apply {
                                putInt("placeId", place.id)
                                putDouble("latitude", currentLat)
                                putDouble("longitude", currentLng)
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

    private fun getCityId(): Int {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getInt("city_id", -1) ?: -1
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

        val cityId = 1 // cityId 가져오기
        if (cityId == -1) {
            Toast.makeText(requireContext(), "도시 정보가 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val mapCenter = naverMap.cameraPosition.target

                val searchRequest = SearchPlacesRequest(
                    latitude = mapCenter.latitude,
                    longitude = mapCenter.longitude
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.searchPlaces(
                        token = "Bearer $token",
                        cityId = cityId,
                        keyword = keyword,
                        request = searchRequest
                    )
                }

                when {
                    response.isSuccess -> {
                        // 성공 처리 로직
                        val pageResponse = response.result
                        val mapPlaces = pageResponse?.content?.map { place ->
                            MapPlace(
                                id = place.id,
                                placeName = place.name,
                                placeType = convertCategory(place.category),
                                placeDistance = "${String.format("%.2f", place.distance)}km",
                                placeLocation = place.address,
                                placeCall = place.phoneNumber,
                                isOpen = if (place.open) "영업중" else "영업종료",
                                placeImgUrl = place.imgUrl
                            )
                        } ?: emptyList()

                        withContext(Dispatchers.Main) {
//                            originalPlaceDatas.clear()
                            originalPlaceDatas.addAll(mapPlaces)

//                            placeDatas.clear()
                            placeDatas.addAll(mapPlaces)
                            binding.mapPlaceRV.adapter?.notifyDataSetChanged()

                            val totalElements = pageResponse?.totalElements ?: 0
                            val currentPage = pageResponse?.number?.plus(1) ?: 0
                            val totalPages = pageResponse?.totalPages ?: 0

                            Toast.makeText(
                                requireContext(),
                                "총 ${mapPlaces.size}개의 장소를 불러왔습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    else -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                response.message ?: "데이터를 불러오는데 실패했습니다.",
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

    private suspend fun loadAllCategories() {
        val token = getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!::naverMap.isInitialized) {
            Log.d("MapFragment", "지도가 아직 초기화되지 않았습니다.")
            return
        }

        val cityId = 1
        if (cityId == -1) {
            Toast.makeText(requireContext(), "도시 정보가 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val mapCenter = naverMap.cameraPosition.target
        val searchRequest = SearchPlacesRequest(
            latitude = mapCenter.latitude,
            longitude = mapCenter.longitude
        )

        val categories = listOf(" ", "PARK", "CAFE", "RESTAURANT", "HOTEL")

        try {
            val allPlaces = mutableListOf<MapPlace>()

            withContext(Dispatchers.IO) {
                // 모든 카테고리를 병렬로 호출
                val deferredResults = categories.map { category ->
                    async {
                        try {
                            RetrofitClient.placesApiService.searchPlaces(
                                token = "Bearer $token",
                                cityId = cityId,
                                keyword = category,
                                request = searchRequest
                            )
                        } catch (e: Exception) {
                            Log.e("MapFragment", "Failed to load category $category", e)
                            null
                        }
                    }
                }

                // 모든 결과 수집
                deferredResults.awaitAll()
                    .filterNotNull()
                    .forEach { response ->
                        if (response.isSuccess) {
                            response.result?.content?.map { place ->
                                MapPlace(
                                    id = place.id,
                                    placeName = place.name,
                                    placeType = convertCategory(place.category),
                                    placeDistance = "${String.format("%.2f", place.distance)}km",
                                    placeLocation = place.address,
                                    placeCall = place.phoneNumber,
                                    isOpen = if (place.open) "영업중" else "영업종료",
                                    placeImgUrl = place.imgUrl,
                                    reviewCount = place.reviewCount
                                )
                            }?.let { allPlaces.addAll(it) }
                        }
                    }
            }

            // UI 업데이트는 한 번만 수행
            withContext(Dispatchers.Main) {
//                originalPlaceDatas.clear()
                originalPlaceDatas.addAll(allPlaces)

//                placeDatas.clear()
                placeDatas.addAll(allPlaces)
                binding.mapPlaceRV.adapter?.notifyDataSetChanged()

                Toast.makeText(
                    requireContext(),
                    "총 ${allPlaces.size}개의 장소를 불러왔습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            handleError(e)
        }
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
        context?.let { // context가 null이 아닌 경우에만 Toast를 표시
            Toast.makeText(it, errorMessage, Toast.LENGTH_SHORT).show()
        } ?: run {
            Log.w("MapFragment", "Fragment가 Activity에 연결되지 않아 Toast를 표시할 수 없습니다.")
        }
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

    private fun getMapCurrentPosition(): Pair<Double, Double> {
        // 지도가 초기화되었는지 확인
        if (::naverMap.isInitialized) {
            val mapCenter = naverMap.cameraPosition.target

            saveCurrentLocation(mapCenter.latitude, mapCenter.longitude) // 현재 위치 저장
            return Pair(mapCenter.latitude, mapCenter.longitude)
        }
        // 지도가 초기화되지 않은 경우 기본값 반환
        return Pair(37.5665, 126.9780) // 서울 시청 기본값
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveCurrentLocation(latitude: Double, longitude: Double) {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putFloat("current_latitude", latitude.toFloat())
            putFloat("current_longitude", longitude.toFloat())
            apply()
        }
        Log.d("MapFragment", "위치 저장: 위도 $latitude, 경도 $longitude")
    }
}
