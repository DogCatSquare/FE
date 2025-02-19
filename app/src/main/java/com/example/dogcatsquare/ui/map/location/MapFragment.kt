package com.example.dogcatsquare.ui.map.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.FilterPlacesRequest
import com.example.dogcatsquare.LocationViewModel
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.map.SearchPlacesRequest
import com.example.dogcatsquare.data.map.WalkListRequest
import com.example.dogcatsquare.ui.map.walking.WalkingMapFragment
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val buttonDatas by lazy { ArrayList<MapButton>() }
    private val allPlaceDatas = ArrayList<MapPlace>()
    private val originalPlaceDatas = ArrayList<MapPlace>()  // 원본 데이터 저장용
    private val placeDatas by lazy { ArrayList<MapPlace>() }
    private val markers = mutableListOf<Marker>()

    // 위치 관련 변수
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private lateinit var sortTextView: TextView
    private var currentSortType = "위치기준"

    private var currentLocation: LatLng? = null
    private lateinit var locationCallback: LocationCallback
    private val locationViewModel: LocationViewModel by activityViewModels()

    private var selectedMarker: Marker? = null

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private val ITEMS_PER_PAGE = 20

    var shouldRefresh = false

    private var currentCategory = "전체"

    private var userAddress: String = ""

    private var filterOptions = FilterOptions(false, false, false)
    private data class FilterOptions(
        var isCurrentlyOpen: Boolean,
        var is24Hours: Boolean,
        var hasParking: Boolean
    )

    // RecyclerView 스크롤 리스너
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (currentCategory != "전체") return

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount = layoutManager.childCount            // 현재 화면에 보이는 아이템 수
            val totalItemCount = layoutManager.itemCount              // 전체 아이템 수
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()  // 첫 번째로 보이는 아이템 위치

            // 현재 화면에 보이는 아이템 중 마지막 아이템의 position
            val lastVisibleItemPosition = firstVisibleItemPosition + visibleItemCount - 1

            // 페이지 로드 트리거 포지션 (현재 페이지의 6-7번째 아이템)
            val loadTriggerPosition = (currentPage * ITEMS_PER_PAGE) + 15 // 16번째 아이템부터

            if (!isLoading && !isLastPage) {
                // 현재 보이는 아이템들 중에 트리거 포지션이 포함되어 있는지 확인
                if (firstVisibleItemPosition <= loadTriggerPosition && loadTriggerPosition <= lastVisibleItemPosition) {
                    loadMorePlaces()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        setupLocationCallback()

        lifecycleScope.launch {
            fetchUserAddress()
        }

        // 프래그먼트 백스택 변경 리스너 등록
        requireActivity().supportFragmentManager.addOnBackStackChangedListener {
            if (isVisible && shouldRefresh) {
                lifecycleScope.launch {
                    loadAllCategories()
                }
                shouldRefresh = false
            }
        }
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

        userAddress = getSavedUserAddress()

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

        binding.researchButton.setOnClickListener {
            refreshMap()
        }

    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val newLocation = LatLng(location.latitude, location.longitude)
                    Log.d("MapFragment", "==== 위치 업데이트 발생 ====")
                    Log.d("MapFragment", "이전 위치(currentLocation): $currentLocation")
                    Log.d("MapFragment", "새로운 위치: lat=${newLocation.latitude}, lng=${newLocation.longitude}")

                    currentLocation = newLocation
                    locationViewModel.updateLocation(newLocation)

                    // 위치 마커는 표시하되 지도는 이동하지 않도록 설정
                    if (::naverMap.isInitialized && naverMap.locationTrackingMode == LocationTrackingMode.None) {
                        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
                    }

                    Log.d("MapFragment", "위치 업데이트 완료: currentLocation = $currentLocation")
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            Log.d("MapFragment", "위치 권한 없음 - 권한 요청 시작")
            requestLocationPermission()
            return
        }

        try {
            Log.d("MapFragment", "위치 업데이트 시작")
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = TimeUnit.SECONDS.toMillis(10)
                fastestInterval = TimeUnit.SECONDS.toMillis(5)
            }

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d("MapFragment", "위치 업데이트 요청 완료")

        } catch (e: SecurityException) {
            Log.e("MapFragment", "위치 권한 오류", e)
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSearchOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_search_option, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // CheckBox 참조
        val openCheckBox = bottomSheetView.findViewById<CheckBox>(R.id.open_btn)
        val hours24CheckBox = bottomSheetView.findViewById<CheckBox>(R.id.hours24_btn)
        val parkCheckBox = bottomSheetView.findViewById<CheckBox>(R.id.park_btn)

        // 현재 필터 상태 적용
        openCheckBox.isChecked = filterOptions.isCurrentlyOpen
        hours24CheckBox.isChecked = filterOptions.is24Hours
        parkCheckBox.isChecked = filterOptions.hasParking

        // 완료 버튼 클릭 리스너
        bottomSheetView.findViewById<MaterialButton>(R.id.confirm_button).setOnClickListener {
            val previousFilters = filterOptions.copy() // 이전 필터 상태 저장

            // 새로운 필터 옵션 설정
            filterOptions = FilterOptions(
                isCurrentlyOpen = openCheckBox.isChecked,
                is24Hours = hours24CheckBox.isChecked,
                hasParking = parkCheckBox.isChecked
            )

            lifecycleScope.launch {
                resetPagingState() // 페이징 상태 초기화

                // 모든 필터가 해제되었는지 확인
                if (!filterOptions.isCurrentlyOpen && !filterOptions.is24Hours && !filterOptions.hasParking) {
                    // 모든 필터가 해제된 경우 기본 데이터 로드
                    loadAllCategories()
                } else {
                    // 필터가 적용된 경우 필터링된 데이터 로드
                    loadFilteredPlaces()
                }
            }

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private suspend fun loadFilteredPlaces(page: Int = 0) {
        try {
            val token = getToken() ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
                return
            }

            // 현재 위치 가져오기
            val userLocation = currentLocation ?: naverMap.cameraPosition.target

            // 필터링 요청 생성
            val filterRequest = FilterPlacesRequest(
                location = FilterPlacesRequest.Location(
                    latitude = userLocation.latitude,
                    longitude = userLocation.longitude
                ),
                is24Hours = filterOptions.is24Hours,
                hasParking = filterOptions.hasParking,
                isCurrentlyOpen = filterOptions.isCurrentlyOpen
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "필터링된 장소를 불러오는 중...", Toast.LENGTH_SHORT).show()
            }

            // API 호출
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.placesApiService.getFilteredPlaces(
                    token = "Bearer $token",
                    page = page,
                    request = filterRequest
                )
            }

            if (response.isSuccess) {
                response.result?.let { pageResponse ->
                    isLastPage = pageResponse.last
                    currentPage = page

                    val newPlaces = pageResponse.content.map { place ->
                        MapPlace(
                            id = place.id,
                            placeName = place.name,
                            placeType = convertCategory(place.category),
                            placeDistance = "${String.format("%.2f", place.distance)}km",
                            placeLocation = place.address,
                            placeCall = place.phoneNumber,
                            isOpen = if (place.open) "영업중" else "영업종료",
                            placeImgUrl = place.imgUrl,
                            reviewCount = place.reviewCount,
                            latitude = place.latitude,
                            longitude = place.longitude,
                            keywords = place.keywords
                        )
                    }

                    withContext(Dispatchers.Main) {
                        if (page == 0) {
                            allPlaceDatas.clear()
                            placeDatas.clear()
                            clearMarkers()
                        }

                        allPlaceDatas.addAll(newPlaces)
                        placeDatas.addAll(newPlaces)
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()

                        // 마커 생성
                        newPlaces.forEach { place ->
                            createMarker(place)
                        }

                        Toast.makeText(
                            requireContext(),
                            "필터링된 ${newPlaces.size}개의 장소를 찾았습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                handleError(e)
            }
        }
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

        naverMap.minZoom = 11.0
        naverMap.moveCamera(CameraUpdate.zoomTo(14.0))

        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
        naverMap.uiSettings.isLocationButtonEnabled = true

        // 위치 권한 확인 및 위치 업데이트 시작
        if (hasLocationPermission()) {
            enableCurrentLocation()
            startLocationUpdates()
        } else {
            requestLocationPermission()
        }

        naverMap.setOnMapClickListener { _, _ ->
            // 선택된 마커가 있다면 원래 상태로 복원
            selectedMarker?.apply {
                width = 70
                height = 70
                zIndex = 0
            }
            selectedMarker = null

            // 줌 레벨을 원래대로 복원
            naverMap.moveCamera(
                CameraUpdate.zoomTo(14.0)  // 기본 줌 레벨
                    .animate(CameraAnimation.Easing)
            )

            // 전체 리스트로 복원
            (binding.mapPlaceRV.adapter as? MapPlaceRVAdapter)?.let { adapter ->
                adapter.updateList(originalPlaceDatas)
            }
        }

        // 초기 장소 검색 수행 (선택적)
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
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (locationSource.isActivated) {
                startLocationUpdates()
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            } else {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupRecyclerView() {
        buttonDatas.clear()
        placeDatas.clear()
        originalPlaceDatas.clear()

        buttonDatas.apply {
            add(MapButton("전체"))
            add(MapButton("병원", R.drawable.btn_hospital))
            add(MapButton("산책로", R.drawable.btn_walk))
            add(MapButton("음식/카페", R.drawable.btn_restaurant))
            add(MapButton("호텔", R.drawable.btn_hotel))
        }

        val mapButtonRVAdapter = MapButtonRVAdapter(buttonDatas, object : MapButtonRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, buttonName: String) {
                currentCategory = buttonName

                Log.d("MapFragment", "버튼 클릭: $buttonName")
                Log.d("MapFragment", "현재까지 불러온 데이터 크기: ${allPlaceDatas.size}")

                // 현재까지 불러온 데이터에서만 필터링
                val filtered = when (buttonName) {
                    "전체" -> allPlaceDatas
                    "병원" -> allPlaceDatas.filter { it.placeType == "동물병원" }
                    "산책로" -> allPlaceDatas.filter { it.placeType == "산책로" }
                    "음식/카페" -> allPlaceDatas.filter { it.placeType in listOf("카페", "식당") }
                    "호텔" -> allPlaceDatas.filter { it.placeType == "호텔" }
                    else -> allPlaceDatas
                }

                Log.d("MapFragment", "필터링 결과 개수: ${filtered.size}")
                Log.d("MapFragment", "필터링된 장소들: ${filtered.map { it.placeName }}")

                // UI 업데이트
                originalPlaceDatas.clear()
                originalPlaceDatas.addAll(filtered)

                placeDatas.clear()
                placeDatas.addAll(filtered)
                binding.mapPlaceRV.adapter?.notifyDataSetChanged()

                // 마커 업데이트
                clearMarkers()
                filtered.forEach { place ->
                    createMarker(place)
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
                    "산책로" -> {
                        val (currentLat, currentLng) = getMapCurrentPosition()
                        val fragment = WalkingMapFragment().apply {
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
                    else -> {
                        // 산책로를 제외한 모든 카테고리는 MapDetailFragment로 이동
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
                }
            }
        })

        binding.mapPlaceRV.apply {
            adapter = mapPlaceRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addOnScrollListener(scrollListener)
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

    @SuppressLint("MissingPermission")
    private suspend fun loadPlacesData(page: Int = 0): List<MapPlace> {
        val token = getToken() ?: run {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return emptyList()
        }

        // 위치 정보 획득
        val userLocation = currentLocation ?: run {
            try {
                suspendCancellableCoroutine { continuation ->
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                val newLocation = LatLng(location.latitude, location.longitude)
                                currentLocation = newLocation
                                continuation.resume(newLocation)
                            } else {
                                val defaultLocation = naverMap.cameraPosition.target
                                continuation.resume(defaultLocation)
                            }
                        }
                        .addOnFailureListener { e ->
                            val defaultLocation = naverMap.cameraPosition.target
                            continuation.resume(defaultLocation)
                        }
                }
            } catch (e: Exception) {
                naverMap.cameraPosition.target
            }
        }

        val searchRequest = SearchPlacesRequest(
            latitude = userLocation.latitude,
            longitude = userLocation.longitude
        )

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.placesApiService.searchPlaces(
                    token = "Bearer $token",
                    page = page,
                    request = searchRequest
                )
            }

            if (response.isSuccess) {
                // 페이징 정보 업데이트
                response.result?.let { pageResponse ->
                    isLastPage = pageResponse.last
                }

                val newPlaces = response.result?.content?.map { place ->
                    MapPlace(
                        id = place.id,
                        placeName = place.name,
                        placeType = convertCategory(place.category),
                        placeDistance = "${String.format("%.2f", place.distance)}km",
                        placeLocation = place.address,
                        placeCall = place.phoneNumber,
                        isOpen = if (place.open) "영업중" else "영업종료",
                        placeImgUrl = place.imgUrl,
                        reviewCount = place.reviewCount,
                        latitude = place.latitude,
                        longitude = place.longitude,
                        keywords = place.keywords
                    )
                } ?: emptyList()

                if (page == 0) {
                    allPlaceDatas.clear()
                }
                allPlaceDatas.addAll(newPlaces)

                return newPlaces

            } else {
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                return emptyList()
            }
        } catch (e: Exception) {
            handleError(e)
            return emptyList()
        }
    }

    private fun loadMorePlaces() {
        if (isLoading) return

        isLoading = true
        lifecycleScope.launch {
            try {
                if (filterOptions.isCurrentlyOpen || filterOptions.is24Hours || filterOptions.hasParking) {
                    loadFilteredPlaces(currentPage + 1)
                } else {
                    // 기존 일반 데이터 로드
                    val newPlaces = loadPlacesData(currentPage + 1)
                    if (newPlaces.isNotEmpty()) {
                        currentPage++
                        placeDatas.addAll(newPlaces)
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
                        newPlaces.forEach { place ->
                            createMarker(place)
                        }
                    } else {
                        isLastPage = true
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                isLoading = false
            }
        }
    }

    // 데이터 초기화 함수
    private fun resetPagingState() {
        currentPage = 0
        isLastPage = false
        isLoading = false
        allPlaceDatas.clear()
        placeDatas.clear()
        clearMarkers()
        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
    }

    private fun resetCategoryAndPaging() {
        (binding.mapButtonRV.adapter as? MapButtonRVAdapter)?.resetSelection()
        resetPagingState()
    }

    @SuppressLint("MissingPermission")
    private suspend fun loadWalkData(radius: Double = 0.0): List<MapPlace> {
        // 시작 시 토스트 메시지
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), "산책로 데이터를 불러오는 중...", Toast.LENGTH_SHORT).show()
        }

        val token = getToken() ?: run {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
            return emptyList()
        }

        // 위치 정보 획득
        val userLocation = currentLocation ?: run {
            try {
                suspendCancellableCoroutine { continuation ->
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                val newLocation = LatLng(location.latitude, location.longitude)
                                currentLocation = newLocation
                                continuation.resume(newLocation)
                            } else {
                                val defaultLocation = naverMap.cameraPosition.target
                                continuation.resume(defaultLocation)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("MapFragment", "위치 정보 획득 실패", e)
                            val defaultLocation = naverMap.cameraPosition.target
                            continuation.resume(defaultLocation)
                        }
                }
            } catch (e: Exception) {
                Log.e("MapFragment", "위치 정보 획득 중 예외 발생", e)
                naverMap.cameraPosition.target
            }
        }

        val walkRequest = WalkListRequest(
            latitude = userLocation.latitude,
            longitude = userLocation.longitude,
            radius = radius
        )

        return try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.placesApiService.getWalkList(
                    token = "Bearer $token",
                    request = walkRequest
                )
            }

            if (!response.isSuccess) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "산책로 데이터를 불러오는데 실패했습니다: ${response.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return emptyList()
            }

            val walks = response.result?.walks?.mapNotNull { walk ->
                val coordinates = walk.coordinates.firstOrNull() ?: return@mapNotNull null

                MapPlace(
                    id = walk.walkId,
                    placeName = walk.title,
                    placeType = "산책로",
                    placeDistance = String.format("%.2f", walk.distance) + "km",
                    placeLocation = walk.description,
                    placeCall = "", // 산책로는 전화번호 없음
                    isOpen = "이용가능",
                    placeImgUrl = walk.walkImageUrl.firstOrNull(),
                    reviewCount = walk.reviewCount,
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude,
                    walkTime = walk.time,
                    walkDifficulty = walk.difficulty,
                    walkSpecial = walk.special,
                    walkCoordinates = walk.coordinates,
                    createdBy = walk.createdBy
                )
            } ?: emptyList()

            // 성공적으로 데이터를 불러왔을 때 토스트 메시지
            withContext(Dispatchers.Main) {
                val message = if (walks.isEmpty()) {
                    "주변에 산책로가 없습니다."
                } else {
                    "총 ${walks.size}개의 산책로를 불러왔습니다."
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            walks

        } catch (e: Exception) {
            Log.e("MapFragment", "산책로 데이터 로드 중 오류 발생", e)
            withContext(Dispatchers.Main) {
                handleError(e)
                Toast.makeText(
                    requireContext(),
                    "산책로 데이터 로드 중 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            emptyList()
        }
    }

    private suspend fun updateUI(places: List<MapPlace>) {
        withContext(Dispatchers.Main) {
            // 기존 마커 제거
            clearMarkers()

            allPlaceDatas.clear()
            allPlaceDatas.addAll(places)

            // RecyclerView 데이터 업데이트
            originalPlaceDatas.clear()
            originalPlaceDatas.addAll(places)

            placeDatas.clear()
            placeDatas.addAll(places)
            binding.mapPlaceRV.adapter?.notifyDataSetChanged()

            // 새로운 마커 생성
            places.forEach { place ->
                createMarker(place)
            }

            // 데이터 로드 완료 메시지
            Toast.makeText(
                requireContext(),
                "총 ${places.size}개의 장소를 불러왔습니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private suspend fun loadAllCategories() {
        if (!::naverMap.isInitialized) {
            Log.d("MapFragment", "지도가 아직 초기화되지 않았습니다.")
            return
        }

        try {
            withContext(Dispatchers.Main) {
                // 로딩 표시
                Toast.makeText(requireContext(), "데이터를 불러오는 중...", Toast.LENGTH_SHORT).show()
            }

            // 순차적으로 데이터 로드 (병렬 처리 대신)
            val places = withContext(Dispatchers.IO) {
                try {
                    loadPlacesData()
                } catch (e: Exception) {
                    Log.e("MapFragment", "일반 장소 로드 실패", e)
                    emptyList()
                }
            }

            val walks = withContext(Dispatchers.IO) {
                try {
                    loadWalkData()
                } catch (e: Exception) {
                    Log.e("MapFragment", "산책로 로드 실패", e)
                    emptyList()
                }
            }

            val allPlaces = walks + places

            // UI 업데이트
            updateUI(allPlaces)

            Log.d("MapFragment", "총 ${allPlaces.size}개의 장소 로드 완료 " +
                    "(일반 장소: ${places.size}, 산책로: ${walks.size})")

        } catch (e: Exception) {
            Log.e("MapFragment", "데이터 로드 중 오류 발생", e)
            withContext(Dispatchers.Main) {
                handleError(e)
            }
        }
    }



    // 카테고리별 마커 아이콘 설정
    private fun getMarkerIconForCategory(placeType: String?): Int {
        return when (placeType) {
            "동물병원" -> R.drawable.ic_marker_hospital// 병원 마커 아이콘
            "산책로" -> R.drawable.ic_marker_park // 공원 마커 아이콘
            "카페" -> R.drawable.ic_marker_cafe // 카페 마커 아이콘
            "식당" -> R.drawable.ic_marker_cafe // 식당 마커 아이콘
            "호텔" -> R.drawable.ic_marker_hotel // 호텔 마커 아이콘
            else -> R.drawable.ic_marker // 기본 마커 아이콘
        }
    }

    // 마커 생성 함수
    private fun createMarker(place: MapPlace) {
        val marker = Marker().apply {
            position = LatLng(place.latitude ?: 0.0, place.longitude ?: 0.0)
            map = naverMap

            // 마커 아이콘 설정
            icon = OverlayImage.fromResource(getMarkerIconForCategory(place.placeType))

            // 마커 크기 설정
            width = 70
            height = 70

            // 마커 클릭 이벤트
            setOnClickListener { overlay ->
                // 이전에 선택된 마커가 있다면 원래 크기로 복원
                selectedMarker?.apply {
                    width = 70
                    height = 70
                    zIndex = 0
                }

                // 현재 마커를 선택된 상태로 변경
                if (selectedMarker !== this) {  // marker 대신 this 사용
                    // 마커 크기 증가
                    width = 90
                    height = 90
                    zIndex = 1
                    selectedMarker = this  // marker 대신 this 사용

                    // 카메라를 해당 위치로 이동하며 줌 레벨 증가
                    naverMap.moveCamera(
                        CameraUpdate.scrollAndZoomTo(
                            position,
                            16.0
                        ).animate(CameraAnimation.Easing)
                    )
                } else {
                    // 같은 마커를 다시 클릭한 경우
                    selectedMarker = null
                }

                // RecyclerView에 해당 장소만 표시
                (binding.mapPlaceRV.adapter as? MapPlaceRVAdapter)?.let { adapter ->
                    adapter.updateList(listOf(place))
                }

                true
            }
        }
        markers.add(marker)
    }



    // 모든 마커 제거
    private fun clearMarkers() {
        markers.forEach { it.map = null }
        markers.clear()
    }

    // 장소 정보 표시
    private fun showPlaceInfo(place: MapPlace) {
        // 카메라를 해당 위치로 이동
        place.latitude?.let { lat ->
            place.longitude?.let { lng ->
                naverMap.moveCamera(
                    CameraUpdate.scrollTo(LatLng(lat, lng))
                        .animate(CameraAnimation.Easing)
                )
            }
        }

        // RecyclerView에 해당 장소만 표시
        (binding.mapPlaceRV.adapter as? MapPlaceRVAdapter)?.let { adapter ->
            adapter.updateList(listOf(place))
        }
    }

    private suspend fun handleError(e: Exception) {
        withContext(Dispatchers.Main) {
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

            context?.let {
                Toast.makeText(it, errorMessage, Toast.LENGTH_SHORT).show()
            } ?: run {
                Log.w("MapFragment", "Fragment가 Activity에 연결되지 않아 Toast를 표시할 수 없습니다.")
            }
            Log.e("MapFragment", "API 오류", e)
        }
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

                // MapButtonRVAdapter의 선택 상태를 "전체"로 리셋
                (binding.mapButtonRV.adapter as? MapButtonRVAdapter)?.let { adapter ->
                    adapter.resetSelection()  // 모든 버튼 선택 해제
                    adapter.updateSelectedButton(0)  // "전체" 버튼 선택 (첫 번째 버튼)
                }

                when (sortType) {
                    "주소기준" -> moveToUserAddress()
                    "위치기준" -> moveToCurrentLocation()
                }
            } catch (e: Exception) {
                Log.e("MapFragment", "Error updating sort text: ${e.message}")
            }
        }
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // 상태가 복원된 후에 필요한 UI 업데이트
        savedInstanceState?.let { bundle ->
            currentSortType = bundle.getString("currentSortType", "위치기준")
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

    // 사용자 정보를 가져오는 함수
    private fun fetchUserAddress() {
        lifecycleScope.launch {
            try {
                val token = getToken() ?: run {
                    Log.e("MapFragment", "토큰이 없습니다.")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "토큰이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // API 호출
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.userApiService.getUser("Bearer $token").execute()
                }

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null && userResponse.isSuccess) {
                        userResponse.result?.let { userInfo ->
                            userAddress = buildString {
                                append(userInfo.doName)
                                if (userInfo.si.isNotEmpty()) {
                                    append(" ")
                                    append(userInfo.si)
                                }
                                if (userInfo.gu.isNotEmpty()) {
                                    append(" ")
                                    append(userInfo.gu)
                                }
                            }

                            withContext(Dispatchers.Main) {
                                saveUserAddress(userAddress)
                                Log.d("MapFragment", "사용자 주소 설정 완료: $userAddress")
                                Toast.makeText(
                                    requireContext(),
                                    "사용자 주소: $userAddress",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } ?: run {
                            Log.w("MapFragment", "사용자 정보가 없습니다.")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireContext(),
                                    "사용자 정보가 없습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Log.e("MapFragment", "사용자 정보 조회 실패: ${userResponse?.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "사용자 정보 조회 실패: ${userResponse?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Log.e("MapFragment", "서버 응답 실패: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "서버 응답 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MapFragment", "사용자 주소 가져오기 중 예외 발생", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "오류 발생: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // SharedPreferences에 주소 저장하는 함수
    private fun saveUserAddress(address: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_address", address)
            apply()
        }
        Toast.makeText(
            requireContext(),
            "주소 저장됨: $address",
            Toast.LENGTH_SHORT
        ).show()
    }

    // SharedPreferences에서 주소 가져오는 함수
    private fun getSavedUserAddress(): String {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("user_address", "") ?: ""
    }

    private fun moveToUserAddress() {
        lifecycleScope.launch {
            try {
                val savedAddress = getSavedUserAddress()
                if (savedAddress.isEmpty()) {
                    Toast.makeText(requireContext(), "저장된 주소가 없습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "주소 기준으로 이동 중...", Toast.LENGTH_SHORT).show()
                }

                try {
                    // Naver Geocoding API로 주소를 좌표로 변환
                    val geocodeResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.naverGeocodeService.geocode(savedAddress)
                    }

                    val address = geocodeResponse.addresses.firstOrNull()
                    if (address != null) {
                        val latitude = address.latitude.toDouble()
                        val longitude = address.longitude.toDouble()

                        // 새로운 위치 정보 저장
                        currentLocation = LatLng(latitude, longitude)

                        withContext(Dispatchers.Main) {
                            if (::naverMap.isInitialized) {
                                // 지도 이동
                                naverMap.moveCamera(
                                    CameraUpdate.scrollAndZoomTo(
                                        LatLng(latitude, longitude),
                                        13.0
                                    ).animate(CameraAnimation.Easing)
                                )

                                Toast.makeText(
                                    requireContext(),
                                    "위치로 이동 완료: $savedAddress",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // 위치가 업데이트된 후에 데이터 다시 로드
                                resetPagingState() // 페이징 상태 초기화
                                loadAllCategories() // 새로운 위치 기준으로 데이터 로드
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "주소를 찾을 수 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "주소 변환 중 오류가 발생했습니다: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.e("MapFragment", "Geocoding 오류", e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "오류가 발생했습니다: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("MapFragment", "moveToUserAddress 오류", e)
            }
        }
    }

    private fun moveToCurrentLocation() {
        lifecycleScope.launch {
            try {
                if (!hasLocationPermission()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    }
                    requestLocationPermission()
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "현재 위치로 이동 중...", Toast.LENGTH_SHORT).show()

                    // 위치 추적 모드 활성화
                    naverMap.locationTrackingMode = LocationTrackingMode.Follow

                    // 지도 줌 레벨 설정
                    naverMap.moveCamera(CameraUpdate.zoomTo(14.0))

                    // 위치 업데이트 시작
                    startLocationUpdates()

                    // 한 번만 데이터 새로고침
                    resetPagingState()
                    loadAllCategories()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "현재 위치로 이동 중 오류가 발생했습니다: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("MapFragment", "moveToCurrentLocation 오류", e)
            }
        }
    }

    private fun refreshMap() {
        lifecycleScope.launch {
            try {
                // 로딩 표시
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "지도를 새로고침하는 중...", Toast.LENGTH_SHORT).show()
                }

                // 페이징 상태 초기화
                resetPagingState()

                // 카테고리 선택 초기화
                resetCategoryAndPaging()

                // 현재 정렬 방식에 따라 다른 처리
                when (currentSortType) {
                    "위치기준" -> moveToCurrentLocation()
                    "주소기준" -> moveToUserAddress()
                    else -> {
                        // 데이터 새로 로드
                        loadAllCategories()
                    }
                }

                // 성공 메시지
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "새로고침 완료", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleError(e)
                    Toast.makeText(
                        requireContext(),
                        "새로고침 중 오류가 발생했습니다: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 위치 업데이트 중지
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .removeLocationUpdates(locationCallback)
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        // 이전 화면에서 돌아왔을 때 데이터 새로고침
        if (shouldRefresh) {
            lifecycleScope.launch {
                loadAllCategories()  // 모든 카테고리 데이터 새로고침
            }
            shouldRefresh = false
        }
    }

    override fun onPause() {
        super.onPause()
        // 다른 프래그먼트로 이동할 때 새로고침 플래그 설정
        shouldRefresh = true
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