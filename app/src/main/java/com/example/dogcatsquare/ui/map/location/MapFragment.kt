package com.example.dogcatsquare.ui.map.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.FilterPlacesRequest
import com.example.dogcatsquare.LoadingDialog
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.map.MapButton
import com.example.dogcatsquare.data.model.map.MapPlace
import com.example.dogcatsquare.data.model.map.SearchPlacesRequest
import com.example.dogcatsquare.databinding.FragmentMapBinding
import com.example.dogcatsquare.ui.map.SearchFragment
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.ui.map.walking.WalkingMapFragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

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

    private val allPlaceDatas = ArrayList<MapPlace>()
    private lateinit var mapPlaceRVAdapter: MapPlaceRVAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private var currentCategory: String = "전체"
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false

    private lateinit var sortTextView: TextView
    private var currentSortType: String = "위치기준"

    private var userAddress: String = ""

    private val mapMarkers = mutableListOf<Marker>()

    // 필터 옵션 상태 저장을 위한 데이터 클래스
    private data class FilterOptions(
        var isCurrentlyOpen: Boolean,
        var is24Hours: Boolean,
        var hasParking: Boolean
    )
    private var filterOptions = FilterOptions(false, false, false)

    // 로딩 다이얼로그 추가
    private lateinit var loadingDialog: LoadingDialog


    // [수정] Vector Drawable을 BitmapDescriptor로 변환하는 헬퍼 함수
    private fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun getMarkerIconForCategory(placeType: String?): Int {
        return when (placeType) {
            "동물병원" -> R.drawable.ic_marker_hospital
            "산책로"   -> R.drawable.ic_marker_park
            "카페"     -> R.drawable.ic_marker_cafe
            "식당"     -> R.drawable.ic_marker_cafe
            "호텔"     -> R.drawable.ic_marker_hotel
            "기타"     -> R.drawable.ic_marker_etc
            else      -> R.drawable.ic_marker
        }
    }

    private fun clearMapMarkers() {
        mapMarkers.forEach { it.remove() }
        mapMarkers.clear()
    }

    // [수정] 마커 추가 시 bitmapDescriptorFromVector 함수 사용하도록 변경
    private fun updateMapMarkers(placesToMark: List<MapPlace>) {
        googleMap?.let { map ->
            clearMapMarkers()
            val defaultIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker)

            for (place in placesToMark) {
                val lat = place.latitude
                val lng = place.longitude
                if (lat != null && lng != null) {
                    val iconResId = getMarkerIconForCategory(place.placeType)
                    val customIcon = bitmapDescriptorFromVector(requireContext(), iconResId)

                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lng))
                            .title(place.placeName)
                            .icon(customIcon ?: defaultIcon) // 변환 실패 시 기본 아이콘 사용
                    )
                    marker?.let { mapMarkers.add(it) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 로딩 다이얼로그 초기화
        loadingDialog = LoadingDialog(requireContext())
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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

        // 필터 버튼 클릭 리스너 추가
        binding.filter.setOnClickListener {
            showSearchOptions()
        }
    }

    // 필터 옵션 BottomSheetDialog 표시
    private fun showSearchOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_search_option, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val openCheckBox = bottomSheetView.findViewById<CheckBox>(R.id.open_btn)
        val hours24CheckBox = bottomSheetView.findViewById<CheckBox>(R.id.hours24_btn)
        val parkCheckBox = bottomSheetView.findViewById<CheckBox>(R.id.park_btn)

        // 현재 필터 상태를 체크박스에 반영
        openCheckBox.isChecked = filterOptions.isCurrentlyOpen
        hours24CheckBox.isChecked = filterOptions.is24Hours
        parkCheckBox.isChecked = filterOptions.hasParking

        // 완료 버튼 클릭 리스너
        bottomSheetView.findViewById<MaterialButton>(R.id.confirm_button).setOnClickListener {
            // 선택된 값으로 필터 옵션 업데이트
            filterOptions = FilterOptions(
                isCurrentlyOpen = openCheckBox.isChecked,
                is24Hours = hours24CheckBox.isChecked,
                hasParking = parkCheckBox.isChecked
            )

            // 필터가 적용된 데이터 로드
            val currentLatLng = googleMap?.cameraPosition?.target ?: return@setOnClickListener
            resetAndLoadPlaces(currentLatLng)

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

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
        }
    }

    private fun moveToSavedAddressLocation() {
        val map = googleMap ?: return
        val savedAddress = getSavedUserAddress()
        if (savedAddress.isBlank()) {
            Toast.makeText(requireContext(), "저장된 주소가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val latLng = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(savedAddress, 1)
                    if (addresses?.isNotEmpty() == true) {
                        LatLng(addresses[0].latitude, addresses[0].longitude)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            if (latLng != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                resetAndLoadPlaces(latLng)
            } else {
                Toast.makeText(requireContext(), "주소를 좌표로 변환할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enableLocationTracking() {
        if (!checkLocationPermission()) {
            requestLocationPermission()
            return
        }
        googleMap?.isMyLocationEnabled = true
        locationCallback?.let { callback ->
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 10000
                fastestInterval = 5000
            }
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, callback, null)
        }
    }

    private fun disableLocationTracking() {
        googleMap?.isMyLocationEnabled = false
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    private fun refreshUiWithPlaces(places: List<MapPlace>) {
        mapPlaceRVAdapter.submitList(places)
        updateMapMarkers(places)
    }

    // 데이터를 로드하기 전에 페이징 및 리스트 상태를 초기화하는 함수
    private fun resetAndLoadPlaces(latLng: LatLng, page: Int = 0) {
        currentPage = page
        isLastPage = false
        if (page == 0) {
            allPlaceDatas.clear()
        }
        loadPlaces(latLng, page)
    }

    // 필터 적용 여부에 따라 다른 API를 호출하는 로직
    private fun loadPlaces(latLng: LatLng, page: Int = 0) {
        val isFilterOn = filterOptions.isCurrentlyOpen || filterOptions.is24Hours || filterOptions.hasParking
        if (isFilterOn) {
            loadFilteredPlaces(latLng, page)
        } else {
            fetchPlacesFromApiByLatLng(latLng, page)
        }
    }

    // 필터링된 장소 데이터를 불러오는 함수
    private fun loadFilteredPlaces(latLng: LatLng, page: Int = 0) {
        if (isLoading) return
        isLoading = true
        lifecycleScope.launch {
            if (page == 0) loadingDialog.show()
            try {
                val token = getToken() ?: ""
                val request = FilterPlacesRequest(
                    location = FilterPlacesRequest.Location(
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    ),
                    is24Hours = filterOptions.is24Hours,
                    hasParking = filterOptions.hasParking,
                    isCurrentlyOpen = filterOptions.isCurrentlyOpen
                )
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.getFilteredPlaces(
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
                    }
                    allPlaceDatas.addAll(newPlaces)
                    filterAndDisplayPlaces()
                }
            } finally {
                isLoading = false
                if (loadingDialog.isDialogShowing) loadingDialog.dismiss()
            }
        }
    }


    private fun fetchPlacesFromApiByLatLng(latLng: LatLng, page: Int = 0) {
        if (isLoading) return
        isLoading = true
        lifecycleScope.launch {
            if (page == 0) loadingDialog.show()
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
                    }
                    allPlaceDatas.addAll(newPlaces)
                    filterAndDisplayPlaces()
                }
            } finally {
                isLoading = false
                if (loadingDialog.isDialogShowing) loadingDialog.dismiss()
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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                resetAndLoadPlaces(latLng)
            }
        }

        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                        resetAndLoadPlaces(latLng)
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }
        }
        enableLocationTracking()
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
                filterAndDisplayPlaces()
            }
        })
        binding.mapButtonRV.apply {
            adapter = mapButtonRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun filterAndDisplayPlaces() {
        val filtered = when (currentCategory) {
            "전체" -> allPlaceDatas
            "병원" -> allPlaceDatas.filter { it.placeType == "동물병원" }
            "산책로" -> allPlaceDatas.filter { it.placeType == "산책로" }
            "음식/카페" -> allPlaceDatas.filter { it.placeType == "카페" || it.placeType == "식당" }
            "호텔" -> allPlaceDatas.filter { it.placeType == "호텔" }
            "기타" -> allPlaceDatas.filter { it.placeType == "기타" }
            else -> allPlaceDatas
        }
        refreshUiWithPlaces(filtered)
    }

    private fun setupMapPlaceRV() {
        // 어댑터를 초기화하고, 아이템 클릭 리스너를 설정합니다.
        // 람다 대신 object 키워드를 사용하여 OnItemClickListener의 익명 객체를 생성합니다.
        mapPlaceRVAdapter = MapPlaceRVAdapter(object : MapPlaceRVAdapter.OnItemClickListener {
            override fun onItemClick(place: MapPlace) {
                // 클릭된 아이템의 유형에 따라 분기
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
                        parentFragmentManager.beginTransaction()
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
                        // 산책로 이외의 모든 장소는 MapDetailFragment로 이동
                        val (currentLat, currentLng) = getMapCurrentPosition()
                        val fragment = MapDetailFragment().apply {
                            arguments = Bundle().apply {
                                putInt("placeId", place.id)
                                putDouble("latitude", currentLat)
                                putDouble("longitude", currentLng)
                            }
                        }
                        parentFragmentManager.beginTransaction()
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
            layoutManager = LinearLayoutManager(context)
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
            saveCurrentLocation(center.latitude, center.longitude)
            Pair(center.latitude, center.longitude)
        } else {
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
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false

        if (checkLocationPermission()) {
            moveToCurrentLocation()
        } else {
            val seoul = LatLng(37.5664056, 126.9778222)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 14f))
            resetAndLoadPlaces(seoul)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        _binding = null
        googleMap = null
        clearMapMarkers()
    }

    override fun onStop() {
        super.onStop()
        // 프래그먼트가 중지될 때 로딩 다이얼로그가 보여지고 있다면 닫아줍니다.
        if (::loadingDialog.isInitialized && loadingDialog.isDialogShowing) {
            loadingDialog.dismiss()
        }
    }
}