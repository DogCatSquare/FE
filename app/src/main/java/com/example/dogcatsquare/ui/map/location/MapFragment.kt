package com.example.dogcatsquare.ui.map.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.dogcatsquare.LocationViewModel
import com.example.dogcatsquare.data.map.SearchPlacesRequest
import com.example.dogcatsquare.data.map.WalkListRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val buttonDatas by lazy { ArrayList<MapButton>() }
    private val originalPlaceDatas = ArrayList<MapPlace>()  // 원본 데이터 저장용
    private val placeDatas by lazy { ArrayList<MapPlace>() }
    private val markers = mutableListOf<Marker>()

    // 위치 관련 변수
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private lateinit var sortTextView: TextView
    private var currentSortType = "주소기준"

    private var currentLocation: LatLng? = null
    private lateinit var locationCallback: LocationCallback
    private val locationViewModel: LocationViewModel by activityViewModels()

    private var selectedMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        setupLocationCallback()
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
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    Log.d("MapFragment", "최초 위치 받아옴: lat=${it.latitude}, lng=${it.longitude}")
                    currentLocation = LatLng(it.latitude, it.longitude)
                    if (::naverMap.isInitialized) {
                        Log.d("MapFragment", "지도를 현재 위치로 이동")
                        naverMap.moveCamera(
                            CameraUpdate.scrollTo(currentLocation!!)
                                .animate(CameraAnimation.Easing)
                        )
                    }
                } ?: Log.d("MapFragment", "최초 위치를 받아올 수 없음")
            }

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

        naverMap.minZoom = 11.0
        naverMap.moveCamera(CameraUpdate.zoomTo(12.0))

        naverMap.locationSource = locationSource
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
                CameraUpdate.zoomTo(12.0)  // 기본 줌 레벨
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

                // 마커 업데이트
                clearMarkers() // 기존 마커 모두 제거
                filtered.forEach { place -> // 필터링된 장소들에 대해서만 마커 생성
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
                        val (currentLat, currentLng) = getMapCurrentPosition()
                        val fragment = WalkingStartViewFragment().apply {
                            arguments = Bundle().apply {
                                putInt("placeId", place.id)
                                putDouble("latitude", currentLat)
                                putDouble("longitude", currentLng) // 이건 필요한 정보들에 따라 수정
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

    @SuppressLint("MissingPermission")
    private suspend fun loadPlacesData(): List<MapPlace> {
        val token = getToken() ?: run {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return emptyList()
        }

//        val cityId = getCityId()
        val cityId = 1
        if (cityId == -1) {
            Toast.makeText(requireContext(), "도시 정보가 필요합니다.", Toast.LENGTH_SHORT).show()
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

        val categories = listOf(" ", "PARK", "CAFE", "RESTAURANT", "HOTEL")
        val allPlaces = mutableListOf<MapPlace>()

        try {
            withContext(Dispatchers.IO) {
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
                            null
                        }
                    }
                }

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
                                    reviewCount = place.reviewCount,
                                    latitude = place.latitude,
                                    longitude = place.longitude
                                )
                            }?.let { allPlaces.addAll(it) }
                        }
                    }
            }
            return allPlaces
        } catch (e: Exception) {
            handleError(e)
            return emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun loadWalkData(): List<MapPlace> {
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

        val walkRequest = WalkListRequest(
            latitude = userLocation.latitude,
            longitude = userLocation.longitude,
            radius = 5.0 // 기본 반경 5km로 설정 (필요에 따라 조정 가능)
        )

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.placesApiService.getWalkList(
                    token = "Bearer $token",
                    request = walkRequest
                )
            }

            if (response.isSuccess) {
                return response.result?.walks?.map { walk ->
                    MapPlace(
                        id = walk.walkId,
                        placeName = walk.title,
                        placeType = "산책로",
                        placeDistance = "${String.format("%.2f", walk.distance)}km",
                        placeLocation = walk.description,
                        placeCall = "", // 산책로는 전화번호 없음
                        isOpen = "이용가능", // 산책로는 항상 이용 가능으로 설정
                        placeImgUrl = walk.walkImageUrl.firstOrNull(),
                        reviewCount = walk.reviewCount,
                        latitude = walk.coordinates.firstOrNull()?.latitude,
                        longitude = walk.coordinates.firstOrNull()?.longitude,
                        // 산책로 전용 추가 정보
                        walkTime = walk.time,
                        walkDifficulty = walk.difficulty,
                        walkSpecial = walk.special,
                        walkCoordinates = walk.coordinates,
                        createdBy = walk.createdBy
                    )
                } ?: emptyList()
            } else {
                Toast.makeText(requireContext(), "산책로 데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                return emptyList()
            }
        } catch (e: Exception) {
            handleError(e)
            return emptyList()
        }
    }

    private suspend fun updateUI(places: List<MapPlace>) {
        withContext(Dispatchers.Main) {
            // 기존 마커 제거
            clearMarkers()

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
            // 병렬로 두 API 호출 실행
            val allPlaces = withContext(Dispatchers.IO) {
                val placesDeferred = async { loadPlacesData() }
                val walksDeferred = async { loadWalkData() }

                // 두 결과를 기다리고 합치기
                val places = placesDeferred.await()
                val walks = walksDeferred.await()

                // 두 리스트 합치기
                places + walks
            }

            // UI 업데이트
            updateUI(allPlaces)

            Log.d("MapFragment", "총 ${allPlaces.size}개의 장소 로드 완료 " +
                    "(일반 장소: ${allPlaces.count { it.placeType != "산책로" }}, " +
                    "산책로: ${allPlaces.count { it.placeType == "산책로" }})")

        } catch (e: Exception) {
            Log.e("MapFragment", "데이터 로드 중 오류 발생", e)
            handleError(e)
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
                            15.0
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
        // 위치 업데이트 중지
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .removeLocationUpdates(locationCallback)
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
