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
import org.json.JSONObject
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.dogcatsquare.BaseResponse
import com.example.dogcatsquare.PlaceRequest
import com.example.dogcatsquare.RegionRequest
import com.example.dogcatsquare.SearchPlacesRequest
import com.example.dogcatsquare.data.community.SharedPrefManager.getUserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.await
import java.io.EOFException
import java.io.IOException

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
//        createRegion()
//        createRegionAndPlace()

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
                placeImg = R.drawable.ic_place_img_default,
                placeReview = null,
                longitude = 127.0495556,  // 적절한 값으로 수정
                latitude = 37.6074859,    // 적절한 값으로 수정
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
                placeImg = R.drawable.ic_place_img_default,
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
                placeImg = R.drawable.ic_place_img_default,
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
                placeImg = R.drawable.ic_place_img_default,
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
                placeImg = R.drawable.ic_place_img_default,
                placeReview = "리뷰(7)",
                longitude = 127.0495556,
                latitude = 37.6074859,
                isOpen = "영업중"
            )
        )

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
                // 각 버튼별 필터링 처리
                val filteredPlaces = when (buttonName) {
                    "전체" -> dummyPlaces
                    "병원" -> dummyPlaces.filter { it.placeType == "동물병원" }
                    "산책로" -> dummyPlaces.filter { it.placeType == "산책로" }
                    "음식/카페" -> dummyPlaces.filter { it.placeType in listOf("카페", "식당") }
                    "호텔" -> dummyPlaces.filter { it.placeType == "호텔" }
                    else -> dummyPlaces
                }
                (binding.mapPlaceRV.adapter as? MapPlaceRVAdapter)?.updateList(filteredPlaces)
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

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    private fun createRegion() {
        // 사용자의 토큰을 가져옵니다
        val token = getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 지역 생성 요청을 시작합니다
        lifecycleScope.launch {
            try {
                val regionRequest = RegionRequest(
                    doName = "경기도",
                    si = "성남시",
                    gu = "분당구"
                )

                withContext(Dispatchers.IO) {
                    val response = RetrofitClient.placesApiService.createRegion(
                        token = "Bearer $token",
                        region = regionRequest
                    )

                    withContext(Dispatchers.Main) {
                        when {
                            response.isSuccess -> {
                                val regionId = response.result
                                Log.d("MapFragment", "지역 생성 성공. Region ID: $regionId")
                                Toast.makeText(
                                    requireContext(),
                                    "지역이 성공적으로 생성되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()


                                regionId?.let {
                                    saveRegionId(it) // 성공 시 지역 ID를 SharedPreferences에 저장
                                    createPlace(it)  // 지역 생성 성공 후 장소 생성 호출
                                }
                            }
                            else -> {
                                Log.e("MapFragment", "지역 생성 실패: ${response.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "지역 생성 실패: ${response.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MapFragment", "API 오류", e)
                    val errorMessage = when (e) {
                        is IOException -> "네트워크 연결을 확인해주세요."
                        is HttpException -> "서버 오류가 발생했습니다. (${e.code()})"
                        else -> "알 수 없는 오류가 발생했습니다."
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveRegionId(regionId: Int) {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref?.edit()?.apply {
            putInt("region_id", regionId)
            apply()
        }
    }

    private fun createPlace(regionId: Int) {
        val token = getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val placeRequest = PlaceRequest(
                    name = "탄천1234",
                    address = "경기도 성남시 분당구 정자1동",
                    category = "PARK",
                    phoneNumber = "031-729-4344",
                    open = true,
                    longitude = 127.0495556,
                    latitude = 37.6074859,
                    businessHours = "24시",  // 더 짧은 형식으로 변경
                    homepageUrl = "null",
                    description = "산책로",  // 설명도 간단하게 수정
                    facilities = listOf("주차장")  // 시설 목록도 단순화
                )

                Log.d("MapFragment", "장소 생성 요청 데이터: $placeRequest")

                withContext(Dispatchers.IO) {
                    val response = RetrofitClient.placesApiService.createPlace(
                        token = "Bearer $token",
                        regionId = regionId,
                        place = placeRequest
                    )

                    withContext(Dispatchers.Main) {
                        when {
                            response.isSuccess -> {  // status 대신 isSuccess 사용
                                val placeId = response.result
                                Log.d("MapFragment", "장소 생성 성공. Place ID: $placeId")
                                Toast.makeText(
                                    requireContext(),
                                    "장소가 성공적으로 생성되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                Log.e("MapFragment", "장소 생성 실패: ${response.message}")
                                Toast.makeText(
                                    requireContext(),
                                    "장소 생성 실패: ${response.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MapFragment", "API 오류", e)
                    val errorMessage = when (e) {
                        is HttpException -> {
                            val errorBody = e.response()?.errorBody()?.string()
                            Log.e("MapFragment", "HTTP 에러 상세: $errorBody")
                            "서버 오류: ${e.response()?.message() ?: e.message()}"
                        }
                        is IOException -> "네트워크 연결을 확인해주세요."
                        else -> "알 수 없는 오류가 발생했습니다: ${e.message}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadPlaces() {
        val token = getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!::naverMap.isInitialized) {
            Log.d("MapFragment", "지도가 아직 초기화되지 않았습니다.")
            return
        }

        val regionId = getRegionId()
        if (regionId == -1) {
            Toast.makeText(requireContext(), "지역 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val currentLocation = naverMap.locationOverlay.position

                val searchRequest = SearchPlacesRequest(
                    userId = getUserId(),
                    longitude = currentLocation.longitude,
                    latitude = currentLocation.latitude
                )

                Log.d("MapFragment", "요청 데이터: $searchRequest")
                Log.d("MapFragment", "regionId: $regionId")

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.searchPlaces(
                        token = "Bearer $token",
                        regionId = regionId,
                        request = searchRequest
                    )
                }

                when {
                    response.isSuccess -> {
                        Log.d("MapFragment", "장소 목록 로드 성공: ${response.result}")
                        Toast.makeText(
                            requireContext(),
                            "장소 목록을 성공적으로 불러왔습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // PlaceItem을 MapPlace로 변환하여 리스트 생성
                        val mapPlaces = response.result?.map { place ->
                            MapPlace(
                                placeName = place.name,
                                placeType = place.category,
                                placeDistance = "${place.distance}km",
                                placeLocation = place.address,
                                placeCall = place.phoneNumber,
                                char1Text = null,    // 필요한 경우 추가 정보 설정
                                char2Text = null,    // 필요한 경우 추가 정보 설정
                                char3Text = if (place.open) "영업중" else "영업종료",
                                placeImg = R.drawable.ic_place_img_default,
                                placeReview = null,  // 리뷰 데이터가 있는 경우 추가
                                isOpen = if (place.open) "영업중" else "영업종료"
                            )
                        } ?: emptyList()

                        // RecyclerView 어댑터 업데이트
                        (binding.mapPlaceRV.adapter as? MapPlaceRVAdapter)?.updateList(mapPlaces)

                        response.result?.forEach { place ->
                            Log.d("MapFragment", """
                            장소 정보:
                            - ID: ${place.id}
                            - 이름: ${place.name}
                            - 주소: ${place.address}
                            - 카테고리: ${place.category}
                            - 전화번호: ${place.phoneNumber}
                            - 영업상태: ${if (place.open) "영업중" else "영업종료"}
                            - 거리: ${place.distance}
                            - 지역ID: ${place.regionId}
                        """.trimIndent())
                        }
                    }
                    else -> {
                        Log.e("MapFragment", "장소 목록 로드 실패: ${response.message}")
                        Toast.makeText(
                            requireContext(),
                            "장소 목록을 불러오는데 실패했습니다: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
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
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getInt("region_id", -1) ?: -1
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}