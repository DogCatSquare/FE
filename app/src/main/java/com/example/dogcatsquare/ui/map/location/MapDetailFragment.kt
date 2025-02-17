package com.example.dogcatsquare.ui.map.location

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.MapAddKeywordFragment
import com.example.dogcatsquare.data.map.DetailImg
import com.example.dogcatsquare.data.map.MapPrice
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.databinding.FragmentMapDetailBinding
import com.example.dogcatsquare.ui.map.SearchFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import com.example.dogcatsquare.data.map.PlaceDetailRequest
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.OverlayImage

class MapDetailFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapDetailBinding? = null
    private val binding get() = _binding!!

    private val imgDatas by lazy { ArrayList<DetailImg>() }
    private val priceDatas by lazy { ArrayList<MapPrice>() }

    private lateinit var naverMap: NaverMap
    private var placeLatitude: Double = 0.0
    private var placeLongitude: Double = 0.0
    private var currentMarker: Marker? = null
    private var isInitialLoad = true

    private var actualPlaceLatitude: Double? = null
    private var actualPlaceLongitude: Double? = null

    private var isWished = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 위치 정보 저장
        placeLatitude = arguments?.getDouble("latitude") ?: 37.5665
        placeLongitude = arguments?.getDouble("longitude") ?: 126.9780

        setupBackButton()
        setupRecyclerView()
        setupNaverMap()

        binding.filter.setOnClickListener {
            showSearchOptions()
        }

        binding.searchBox.setOnClickListener {
            val searchFragment = SearchFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, searchFragment)
                .addToBackStack(null)
                .commit()
        }

        arguments?.getInt("placeId")?.let { placeId ->
            loadPlaceDetails(placeId)
        }
    }

    private fun setupNaverMap() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.mapView2) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.mapView2, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        naverMap.uiSettings.apply {
            isZoomControlEnabled = false
            isScrollGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isZoomGesturesEnabled = false
        }

        // 실제 장소 위치가 있으면 그 위치를, 없으면 초기 위치를 사용
        val latitude = actualPlaceLatitude ?: placeLatitude
        val longitude = actualPlaceLongitude ?: placeLongitude

        updateMapLocation(latitude, longitude)
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
                    latitude = placeLatitude,
                    longitude = placeLongitude
                )

                Log.d("MapDetailFragment", "Requesting place details with coordinates: lat=$placeLatitude, lng=$placeLongitude")

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.getPlaceById(
                        token = "Bearer $token",
                        placeId = placeId,
                        request = request
                    )
                }

                if (response.isSuccess) {
                    response.result?.let { placeDetail ->
                        Log.d("MapDetailFragment", "Received place coordinates: lat=${placeDetail.latitude}, lng=${placeDetail.longitude}")

                        actualPlaceLatitude = placeDetail.latitude
                        actualPlaceLongitude = placeDetail.longitude

                        binding.apply {
                            placeName.text = placeDetail.name
                            placeLocationFull.text = placeDetail.address
                            placeLocation.text = placeDetail.address.split(" ").getOrNull(2) ?: ""
                            placeType.text = convertCategory(placeDetail.category)
                            placeCall.text = placeDetail.phoneNumber
                            placeDistance.text = "${String.format("%.2f", placeDetail.distance)}km"
                            placeStatus.text = if (placeDetail.open) "영업중" else "영업종료"
                            placeDetail.businessHours?.let { hours ->
                                placeTime.text = formatBusinessHours(hours)
                            }
                            placeDetail.homepageUrl?.let { url ->
                                if (url.isNotEmpty()) {
                                    placeUrl.text = url
                                    placeUrl.setOnClickListener {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(requireContext(), "URL을 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } ?: run {
                                placeUrl.text = "정보가 없습니다."
                                placeUrl.setOnClickListener(null)
                            }
                            placeIntro.text = placeDetail.description
                            placeFacility.text = formatFacilities(placeDetail.facilities)
                            wishButton.setImageResource(
                                if (placeDetail.wished) R.drawable.ic_wish_check // 찜한 상태의 이미지
                                else R.drawable.ic_wish // 찜하지 않은 상태의 이미지
                            )
                            wishButton.setOnClickListener {
                                lifecycleScope.launch {
                                    try {
                                        val token = getToken()
                                        if (token == null) {
                                            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }

                                        // API 호출
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitClient.placesApiService.toggleWish(
                                                token = "Bearer $token",
                                                placeId = placeDetail.id
                                            )
                                        }

                                        if (response.isSuccess) {
                                            // 로컬 상태 업데이트
                                            isWished = response.result ?: !isWished

                                            // 이미지 업데이트
                                            wishButton.setImageResource(
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

                            placeDetail.keywords?.let { keywords ->
                                setupCharacteristicCards(keywords)
                            } ?: setupCharacteristicCards(emptyList())
                            // 지도 위치 업데이트
                            if (::naverMap.isInitialized) {
                                updateMapLocation(placeDetail.latitude, placeDetail.longitude)
                            }

                            updateImages(placeDetail.imageUrls)

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

    private fun setupCharacteristicCards(keywords: List<String>) {
        val backgroundColor = "#EAF2FE"  // 동물병원용 배경색
        val textColor = "#276CCB"       // 동물병원용 텍스트색

        binding.apply {
            // 기존 카드들 모두 제거
            characteristicsContainer.removeAllViews()

            // 각 키워드에 대해 동적으로 카드 생성 및 추가
            keywords.forEach { keyword ->
                // CardView 생성
                val cardView = MaterialCardView(requireContext()).apply {
                    layoutParams = FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        // marginEnd와 marginBottom을 setMargins 메소드로 설정
                        val margin = resources.getDimensionPixelSize(R.dimen.spacing_8)
                        setMargins(0, 0, margin, margin)  // left, top, right, bottom
                    }
                    radius = resources.getDimension(R.dimen.radius_4)
                    cardElevation = 0f
                    setCardBackgroundColor(Color.parseColor(backgroundColor))
                }

                // TextView 생성
                val textView = TextView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    text = keyword
                    setTextColor(Color.parseColor(textColor))
                    textSize = 12f
                    setPadding(
                        resources.getDimensionPixelSize(R.dimen.spacing_14),
                        resources.getDimensionPixelSize(R.dimen.spacing_3),
                        resources.getDimensionPixelSize(R.dimen.spacing_14),
                        resources.getDimensionPixelSize(R.dimen.spacing_3)
                    )
                }

                // TextView를 CardView에 추가
                cardView.addView(textView)

                // CardView를 FlexboxLayout에 추가
                characteristicsContainer.addView(cardView)
            }

            // 정보추가하기 버튼 추가
            val addButton = MaterialCardView(requireContext()).apply {
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    val margin = resources.getDimensionPixelSize(R.dimen.spacing_8)
                    setMargins(0, 0, margin, margin)
                }
                radius = resources.getDimension(R.dimen.radius_4)
                cardElevation = 0f
                strokeColor = Color.parseColor(textColor)
                strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_1)
                setCardBackgroundColor(Color.WHITE)

                setOnClickListener {
                    val placeId = arguments?.getInt("placeId") ?: return@setOnClickListener
                    val placeName = binding.placeName.text.toString()

                    // MapAddKeywordFragment로 이동
                    val fragment = MapAddKeywordFragment().apply {
                        arguments = Bundle().apply {
                            putInt("placeId", placeId)
                            putString("placeName", placeName)
                        }
                    }

                    requireActivity().supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                        .hide(this@MapDetailFragment)
                        .add(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            val addButtonText = TextView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = "+ 정보추가하기"
                setTextColor(Color.parseColor(textColor))
                textSize = 12f
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.spacing_14),
                    resources.getDimensionPixelSize(R.dimen.spacing_3),
                    resources.getDimensionPixelSize(R.dimen.spacing_14),
                    resources.getDimensionPixelSize(R.dimen.spacing_3)
                )
            }

            addButton.addView(addButtonText)
            characteristicsContainer.addView(addButton)
        }
    }



    private fun formatBusinessHours(businessHours: String): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val koreanDayName = when (dayOfWeek) {
            Calendar.MONDAY -> "월요일"
            Calendar.TUESDAY -> "화요일"
            Calendar.WEDNESDAY -> "수요일"
            Calendar.THURSDAY -> "목요일"
            Calendar.FRIDAY -> "금요일"
            Calendar.SATURDAY -> "토요일"
            Calendar.SUNDAY -> "일요일"
            else -> return "영업시간 정보 없음"
        }

        return businessHours.split(", ")
            .find { it.startsWith(koreanDayName) }
            ?.substringAfter(": ")
            ?.let { hours ->
                when {
                    hours.contains("24시간") -> "24시간 영업"
                    hours.contains("휴무") -> "휴무일"
                    else -> hours
                }
            } ?: "영업시간 정보 없음"
    }

    private fun formatFacilities(facilities: List<String>?): String {
        return facilities
            ?.takeIf { it.isNotEmpty() }
            ?.let { facilityList ->
                facilityList.map { facility ->
                    when (facility.toLowerCase()) {
                        "parking" -> "주차장"
                        "wifi" -> "와이파이"
                        "pet_friendly" -> "반려동물 동반 가능"
                        else -> facility
                    }
                }.joinToString(" • ")
            } ?: "정보가 없습니다."
    }

    private fun updateImages(imageUrls: List<String>?) {
        imgDatas.clear()

        if (imageUrls.isNullOrEmpty()) {
            Log.d("MapDetailFragment", "이미지 URL이 없어서 기본 이미지를 사용합니다")
            imgDatas.add(DetailImg(R.drawable.ic_place_img_default))
        } else {
            Log.d("MapDetailFragment", "수신된 이미지 URL 개수: ${imageUrls.size}")
            imageUrls.forEach { url ->
                if (!url.isNullOrBlank()) {
                    Log.d("MapDetailFragment", "이미지 URL 추가: $url")
                    imgDatas.add(DetailImg(url))
                }
            }
        }

        // UI 업데이트는 메인 스레드에서 실행되도록 보장
        binding.detailImgRV.post {
            if (binding.detailImgRV.adapter == null) {
                val detailImgRVAdapter = DetailImgRVAdapter(imgDatas)
                binding.detailImgRV.apply {
                    adapter = detailImgRVAdapter
                    layoutManager = LinearLayoutManager(
                        context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }
            } else {
                // RecyclerView 완전 새로고침
                binding.detailImgRV.adapter = DetailImgRVAdapter(ArrayList(imgDatas))
            }
        }
    }

    private fun updateMapLocation(lat: Double, lng: Double) {
        val location = LatLng(lat, lng)
        Log.d("MapDetailFragment", "Updating map location to: lat=$lat, lng=$lng")

        // 기존 마커 제거
        currentMarker?.setMap(null)

        // 카메라 이동
        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(
            location,
            15.0
        ))

        // 새 마커 추가
        currentMarker = Marker().apply {
            position = location
            icon = OverlayImage.fromResource(R.drawable.ic_marker)
            setMap(naverMap)
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
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        // 이미지 리사이클러뷰 설정
        val detailImgRVAdapter = DetailImgRVAdapter(imgDatas)
        binding.detailImgRV.apply {
            adapter = detailImgRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun showSearchOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_search_option, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().supportFragmentManager.fragments
            .filterIsInstance<MapFragment>()
            .firstOrNull()?.let { mapFragment ->
                requireActivity().supportFragmentManager.beginTransaction()
                    .show(mapFragment)
                    .commit()
            }
        _binding = null
    }

    companion object {
        fun newInstance(placeId: Int, latitude: Double, longitude: Double): MapEtcFragment {
            return MapEtcFragment().apply {
                arguments = Bundle().apply {
                    putInt("placeId", placeId)
                    putDouble("latitude", latitude)
                    putDouble("longitude", longitude)
                }
            }
        }
    }

}
