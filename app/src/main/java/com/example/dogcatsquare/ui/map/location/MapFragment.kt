package com.example.dogcatsquare.ui.map.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.map.MapButton
import com.example.dogcatsquare.data.map.MapPlace
import com.example.dogcatsquare.data.map.SearchPlacesRequest
import com.example.dogcatsquare.databinding.FragmentMapBinding
import com.example.dogcatsquare.ui.map.SearchFragment
import com.example.dogcatsquare.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val buttonDatas by lazy {
        arrayListOf(
            MapButton("전체"),
            MapButton("병원", R.drawable.btn_hospital),
            MapButton("산책로", R.drawable.btn_walk),
            MapButton("음식/카페", R.drawable.btn_restaurant),
            MapButton("호텔", R.drawable.btn_hotel),
            MapButton("기타", R.drawable.btn_etc)
        )
    }

    private val allPlaceDatas = ArrayList<MapPlace>()      // 전체 장소 데이터(페이징/필터링용)
    private val originalPlaceDatas = ArrayList<MapPlace>() // 현재 리스트(필터링/카테고리용)
    private val placeDatas = ArrayList<MapPlace>()         // RecyclerView에 표시될 데이터
    private lateinit var mapPlaceRVAdapter: MapPlaceRVAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private var currentCategory: String = "전체"
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    private lateinit var sortTextView: TextView
    private var currentSortType: String = "위치기준" // 기본값

    private var userAddress: String = "" // 서버에서 받아온 유저 기본 주소

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 유저 주소 정보 서버에서 받아와서 SharedPreferences에 저장
        lifecycleScope.launch {
            fetchUserAddress()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.mapFragment, it).commit()
            }
        mapFragment.getMapAsync(this)

        setupBottomSheet()
        setupMapButtonRV()
        setupMapPlaceRV()

        sortTextView = binding.sortButton.findViewById(R.id.sortText)
        binding.sortButton.setOnClickListener {
            val sortDialog = SortDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("currentSortType", currentSortType)
                }
            }
            sortDialog.show(childFragmentManager, "SortDialog")
        }

        binding.searchBox.setOnClickListener {
            googleMap?.let { map ->
                val currentLocation = map.cameraPosition.target
                val searchFragment = SearchFragment().apply {
                    arguments = Bundle().apply {
                        putDouble("latitude", currentLocation.latitude)
                        putDouble("longitude", currentLocation.longitude)
                    }
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .hide(this@MapFragment)
                    .add(R.id.main_frm, searchFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    /**
     * 서버에서 유저 주소 받아와서 SharedPreferences에 반드시 저장!
     */
    private suspend fun fetchUserAddress() {
        try {
            val token = getToken() ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "토큰이 없습니다.", Toast.LENGTH_SHORT).show()
                }
                return
            }
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
                        // ★★★ 반드시 SharedPreferences에 저장
                        saveUserAddress(userAddress)
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "주소 저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserAddress(address: String) {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_address", address)
            apply()
        }
    }

    private fun getSavedUserAddress(): String {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("user_address", "") ?: ""
    }

    fun updateSortText(sortType: String) {
        currentSortType = sortType
        sortTextView.text = sortType
        when (sortType) {
            "위치기준" -> {
                enableLocationTracking()
                moveToCurrentLocation()
            }
            "주소기준" -> {
                disableLocationTracking()
                moveToSavedAddressLocation()
            }
            // ... 기타 정렬 추가 시
        }
    }

    private fun moveToSavedAddressLocation() {
        val map = googleMap ?: return
        val savedAddress = getSavedUserAddress()
        if (savedAddress.isNullOrBlank()) {
            Toast.makeText(requireContext(), "저장된 주소가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val latLng = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocationName(savedAddress, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        LatLng(addresses[0].latitude, addresses[0].longitude)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            if (latLng != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                fetchPlacesFromApiByLatLng(latLng)
            } else {
                Toast.makeText(requireContext(), "주소를 좌표로 변환할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 실시간 위치 추적 활성화
    private fun enableLocationTracking() {
        googleMap?.isMyLocationEnabled = true
        // 위치 업데이트 콜백 다시 등록
        locationCallback?.let { callback ->
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 10000
                fastestInterval = 5000
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, callback, null)
        }
    }

    // 실시간 위치 추적 비활성화
    private fun disableLocationTracking() {
        googleMap?.isMyLocationEnabled = false
        // 위치 업데이트 콜백 해제
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    private fun fetchPlacesFromApiByLatLng(latLng: LatLng, page: Int = 0) {
        if (isLoading) return
        isLoading = true
        lifecycleScope.launch {
            try {
                val token = getToken() ?: ""
                val request = SearchPlacesRequest(
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                )
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.searchPlaces(
                        token = "Bearer $token",
                        page = page,
                        request = request
                    )
                }
                if (response.isSuccess && response.result != null) {
                    val pageResponse = response.result!!
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

                    if (page == 0) {
                        allPlaceDatas.clear()
                        placeDatas.clear()
                        originalPlaceDatas.clear()
                    }
                    allPlaceDatas.addAll(newPlaces)
                    placeDatas.addAll(newPlaces)
                    originalPlaceDatas.addAll(newPlaces)
                    mapPlaceRVAdapter.updateList(placeDatas)
                }
            } finally {
                isLoading = false
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                moveToCurrentLocation()
            }
        }
    }

    private fun moveToCurrentLocation() {
        val map = googleMap ?: return
        if (!checkLocationPermission()) {
            requestLocationPermission()
            return
        }
        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                fetchPlacesFromApiByLatLng(latLng)
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    fetchPlacesFromApiByLatLng(latLng)
                }
            }
        }
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            null
        )
    }

    private fun setupBottomSheet() {
        val whiteBackgroundOverlay = binding.whiteBackgroundOverlay
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        val bottomBarCollapsed = binding.bottomSheet.findViewById<ImageView>(R.id.bottomBarCollapsed)
        val bottomBarExpanded = binding.bottomSheet.findViewById<ImageView>(R.id.bottomBarExpanded)

        binding.root.post {
            val mapButtonBottom = binding.mapButtonRV.bottom +
                    (binding.mapButtonRV.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            val maxHeight = binding.root.height - mapButtonBottom
            binding.bottomSheet.layoutParams.height = maxHeight
            binding.bottomSheet.requestLayout()
        }

        bottomSheetBehavior.apply {
            isDraggable = true
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                whiteBackgroundOverlay.alpha = slideOffset
                val fadeOffset = when {
                    slideOffset < 0.3f -> 0f
                    slideOffset > 0.7f -> 1f
                    else -> (slideOffset - 0.3f) / 0.4f
                }
                bottomBarCollapsed?.alpha = 1 - fadeOffset
                bottomBarExpanded?.alpha = fadeOffset
            }
        })
    }

    private fun setupMapButtonRV() {
        val mapButtonRVAdapter = MapButtonRVAdapter(buttonDatas, object : MapButtonRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, buttonName: String) {
                currentCategory = buttonName

                val filtered = when (buttonName) {
                    "전체" -> allPlaceDatas
                    "병원" -> allPlaceDatas.filter { it.placeType == "동물병원" }
                    "산책로" -> allPlaceDatas.filter { it.placeType == "산책로" }
                    "음식/카페" -> allPlaceDatas.filter { it.placeType == "카페" || it.placeType == "식당" }
                    "호텔" -> allPlaceDatas.filter { it.placeType == "호텔" }
                    "기타" -> allPlaceDatas.filter { it.placeType == "기타" }
                    else -> allPlaceDatas
                }
                originalPlaceDatas.clear()
                originalPlaceDatas.addAll(filtered)

                placeDatas.clear()
                placeDatas.addAll(filtered)
                mapPlaceRVAdapter.updateList(placeDatas)
            }
        })
        binding.mapButtonRV.apply {
            adapter = mapButtonRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupMapPlaceRV() {
        mapPlaceRVAdapter = MapPlaceRVAdapter(placeDatas)
        binding.mapPlaceRV.apply {
            adapter = mapPlaceRVAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun fetchPlacesFromApi(page: Int) {
        val map = googleMap ?: return
        if (isLoading) return
        isLoading = true

        lifecycleScope.launch {
            try {
                val location = map.cameraPosition.target
                fetchPlacesFromApiByLatLng(location, page)
            } finally {
                isLoading = false
            }
        }
    }

    private fun convertCategory(category: String): String {
        return when (category) {
            "HOSPITAL" -> "동물병원"
            "PARK" -> "산책로"
            "CAFE" -> "카페"
            "RESTAURANT" -> "식당"
            "HOTEL" -> "호텔"
            "ETC" -> "기타"
            else -> category
        }
    }

    fun getMapCurrentPosition(): Pair<Double, Double> {
        return if (googleMap != null) {
            val center = googleMap!!.cameraPosition.target
            saveCurrentLocation(center.latitude, center.longitude) // (선택) 최신 위치 저장
            Pair(center.latitude, center.longitude)
        } else {
            // 구글맵이 아직 준비되지 않은 경우 기본값 반환 (서울시청)
            Pair(37.5664056, 126.9778222)
        }
    }

    private fun saveCurrentLocation(latitude: Double, longitude: Double) {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putFloat("current_latitude", latitude.toFloat())
            putFloat("current_longitude", longitude.toFloat())
            apply()
        }
    }

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (checkLocationPermission()) {
            moveToCurrentLocation()
        } else {
            val seoul = LatLng(37.5664056, 126.9778222)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 14f))
            fetchPlacesFromApiByLatLng(seoul)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        _binding = null
        googleMap = null
    }
}