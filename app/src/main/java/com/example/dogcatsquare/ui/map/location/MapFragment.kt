package com.example.dogcatsquare.ui.map.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsAnimation
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.PlacesResponse
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
import org.json.JSONObject
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.dogcatsquare.BaseResponse
import com.example.dogcatsquare.PlaceRequest
import com.example.dogcatsquare.RegionRequest
import com.example.dogcatsquare.SearchPlacesRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.await

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val buttonDatas by lazy { ArrayList<MapButton>() }
    private val placeDatas by lazy { ArrayList<MapPlace>() }
    private val placeList = ArrayList<MapPlace>()

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
        createRegionAndPlace()

        // Set up filter button click listener
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
                .replace(R.id.main_frm, searchFragment)
                .addToBackStack(null)  // 뒤로 가기를 위해 백스택에 추가
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
        placeList.clear()

        buttonDatas.apply {
            add(MapButton("전체"))
            add(MapButton("병원", R.drawable.btn_hospital))
            add(MapButton("산책로", R.drawable.btn_walk))
            add(MapButton("음식/카페", R.drawable.btn_restaurant))
            add(MapButton("호텔", R.drawable.btn_hotel))
        }

        val mapButtonRVAdapter = MapButtonRVAdapter(buttonDatas, object : MapButtonRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, buttonName: String) {
                // 각 버튼별 처리
                when (buttonName) {
//                    "전체" -> {
//                        placeDatas.clear()
//                        placeDatas.addAll(getAllPlaces())
//                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
//                    }
//                    "병원" -> {
//                        placeDatas.clear()
//                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "동물병원" })
//                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
//                    }
//                    "산책로" -> {
//                        placeDatas.clear()
//                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "산책로" })
//                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
//                    }
//                    "음식/카페" -> {
//                        placeDatas.clear()
//                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "식당" || it.placeType == "카페" })
//                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
//                    }
//                    "호텔" -> {
//                        placeDatas.clear()
//                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "호텔" })
//                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
//                    }
                }
            }
        })

        binding.mapButtonRV.apply {
            adapter = mapButtonRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        getAllPlaces()

        val mapPlaceRVAdapter = MapPlaceRVAdapter(placeDatas, object : MapPlaceRVAdapter.OnItemClickListener {
            override fun onItemClick(place: MapPlace) {
                when (place.placeType) {
                    "산책로" -> {
                        // WalkingStartViewFragment로 전환
                        val fragment = WalkingStartViewFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
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
                        // Fragment 전환
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, fragment)
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
                        // Fragment 전환
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, fragment)
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

    private fun getAllPlaces() {
        val currentLocation = locationSource.lastLocation
        val longitude = currentLocation?.longitude ?: 127.0495556
        val latitude = currentLocation?.latitude ?: 37.6074859

        Log.d("MapFragment", "getAllPlaces 호출 - longitude: $longitude, latitude: $latitude")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 요청 객체 생성
                val searchRequest = SearchPlacesRequest(
                    userId = 1,
                    longitude = longitude,
                    latitude = latitude
                )

                val response = RetrofitClient.placesApiService.getAllPlaces(
                    regionId = 1,
                    request = searchRequest
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccess) {
                        val mappedPlaces = response.result.map { place ->
                            MapPlace(
                                placeName = place.name,
                                placeType = place.category,
                                placeDistance = String.format("%.2fkm", place.distance),
                                placeLocation = place.address,
                                placeCall = place.phoneNumber,
                                char1Text = when(place.category) {
                                    "HOSPITAL" -> "중성화 수술"
                                    "PARK" -> "난이도 하"
                                    "HOTEL" -> "반려동물 호텔"
                                    "RESTAURANT", "CAFE" -> "반려동물 동반"
                                    else -> null
                                },
                                char2Text = when(place.category) {
                                    "HOSPITAL" -> "예방접종"
                                    "PARK" -> "쓰레기통"
                                    "HOTEL" -> "반려동물 유치원"
                                    "RESTAURANT", "CAFE" -> "야외좌석"
                                    else -> null
                                },
                                char3Text = if (place.category == "HOSPITAL" && place.open) "24시" else null,
                                placeImg = R.drawable.ic_place_img_default,
                                placeReview = null,
                                longitude = longitude,
                                latitude = latitude,
                                isOpen = if (place.open) "영업중" else "영업종료"
                            )
                        }

                        placeList.clear()
                        placeList.addAll(mappedPlaces)
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()

                        if (mappedPlaces.isEmpty()) {
                            Toast.makeText(context, "주변에 장소가 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "데이터 로드 실패: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("MapFragment", "HTTP 오류: ${e.code()}, Body: $errorBody")
                    Toast.makeText(context, "서버 오류 (${e.code()}): ${e.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MapFragment", "API 오류", e)
                    Toast.makeText(context, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createRegionAndPlace() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val regionRequest = RegionRequest(
                    regionId = 1,
                    do_ = "경기도",
                    si = "수원시",
                    gu = "영통구"
                )

                Log.d("MapFragment", "지역 생성 요청: $regionRequest")

                // suspend 함수 직접 호출
                val response = RetrofitClient.placesApiService.createRegion(regionRequest)

                withContext(Dispatchers.Main) {
                    if (response.status == 200) {
                        Log.d("MapFragment", "지역 생성 성공")
                        createPlace()
                    } else {
                        Log.e("MapFragment", "지역 생성 실패: ${response.message}")
                        Toast.makeText(
                            requireContext(),
                            "지역 생성에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MapFragment", "API 오류", e)
                    Toast.makeText(
                        requireContext(),
                        "네트워크 오류: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // createPlace() 함수도 코루틴으로 변경
    private fun createPlace() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val placeRequest = PlaceRequest(
                    name = "멍냥동물병원",
                    address = "경기도 수원시 영통구 123",
                    category = "hospital",
                    phoneNum = "031-123-4567",
                    open = true,
                    longitude = 127.0495556,
                    latitude = 37.6074859
                )

                Log.d("MapFragment", "장소 생성 요청: $placeRequest")

                val response = RetrofitClient.placesApiService.createPlace(
                    regionId = 1,  // regionId 추가
                    place = placeRequest
                )

                withContext(Dispatchers.Main) {
                    if (response.status == 200) {
                        Log.d("MapFragment", "장소 생성 성공")
                        Toast.makeText(requireContext(), "장소가 성공적으로 생성되었습니다.", Toast.LENGTH_SHORT).show()
                        getAllPlaces()  // 목록 새로고침
                    } else {
                        Log.e("MapFragment", "장소 생성 실패: ${response.message}")
                        Toast.makeText(requireContext(), "장소 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MapFragment", "API 오류", e)
                    Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
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
            } catch (e: Exception) {
                Log.e("MapFragment", "Error updating sort text: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}