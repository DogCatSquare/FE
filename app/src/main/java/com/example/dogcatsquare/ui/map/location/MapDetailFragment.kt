package com.example.dogcatsquare.ui.map.location

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.map.DetailImg
import com.example.dogcatsquare.data.model.map.MapPrice
import com.example.dogcatsquare.data.model.map.MapReview
import com.example.dogcatsquare.data.model.map.PlaceDetailRequest
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.databinding.FragmentMapDetailBinding
import com.example.dogcatsquare.ui.map.SearchFragment
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MapDetailFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapDetailBinding? = null
    private val binding get() = _binding!!

    private val imgDatas by lazy { ArrayList<DetailImg>() }
    private val priceDatas by lazy { ArrayList<MapPrice>() }
    private val reviewDatas by lazy { ArrayList<MapReview>() }

    private lateinit var googleMap: GoogleMap
    private var placeLatitude: Double = 0.0
    private var placeLongitude: Double = 0.0
    private var currentMarker: Marker? = null
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
        setupAddReviewButton()
        setupGoogleMap()

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
                        placeDetail.recentReviews?.forEach { review ->
                            Log.d("MapDetailFragment", """
            API 리뷰 응답:
            - 리뷰 ID: ${review.id}
            - 사용자 ID: ${review.userId}
            - 닉네임: ${review.nickname}
            - 내용: ${review.content}
        """.trimIndent())
                        }

                        actualPlaceLatitude = placeDetail.latitude
                        actualPlaceLongitude = placeDetail.longitude

                        // 카테고리에 따른 뷰 visibility 설정
                        val isHotel = placeDetail.category == "HOTEL"

                        binding.apply {
                            // 기본 정보 설정
                            placeName.text = placeDetail.name
                            placeLocationFull.text = placeDetail.address
                            placeLocation.text = placeDetail.address.split(" ").getOrNull(2) ?: ""
                            placeType.text = convertCategory(placeDetail.category)
                            placeCall.text = placeDetail.phoneNumber?.takeIf { it.isNotBlank() } ?: "정보가 없습니다"
                            copy.visibility = if (placeDetail.phoneNumber.isNullOrBlank()) View.GONE else View.VISIBLE
                            copy.setOnClickListener {
                                if (!placeDetail.phoneNumber.isNullOrBlank()) {
                                    val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = ClipData.newPlainText("phone_number", placeDetail.phoneNumber)
                                    clipboardManager.setPrimaryClip(clipData)
                                    Toast.makeText(requireContext(), "전화번호가 복사되었습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            direction.setOnClickListener {
                                val latitude = placeDetail.latitude
                                val longitude = placeDetail.longitude

                                if (latitude != null && longitude != null) {
                                    // 구글 지도 앱을 열기 위한 Uri 생성
                                    val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")

                                    try {
                                        // 구글 지도 앱이 설치되어 있으면 실행
                                        startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        // 구글 지도 앱이 없으면 웹 브라우저로 지도 열기
                                        try {
                                            val webIntent = Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"))
                                            startActivity(webIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(requireContext(), "지도를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "위치 정보가 없어 길찾기를 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            placeDistance.text = "${String.format("%.2f", placeDetail.distance)}km"
//                            placeStatus.text = if (placeDetail.open) "영업중" else "영업종료"

                            // 영업시간 설정
                            placeDetail.businessHours?.let { hours ->
                                placeTime.text = formatBusinessHours(hours)
                                // 영업 상태 업데이트
                                placeStatus.text = if (isCurrentlyOpen(hours)) "영업중" else "영업종료"
                            } ?: run {
                                placeTime.text = "영업시간 정보 없음"
                                placeStatus.text = "정보 없음"
                            }
                            // 홈페이지 설정
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

                            // additionalInfo 처리
                            placeDetail.additionalInfo?.let { info ->
                                binding.additionalInfo.visibility = View.VISIBLE
                                binding.additionalInfo.text = info
                            } ?: run {
                                binding.additionalInfo.visibility = View.GONE
                                val params = binding.imageView9.layoutParams as ConstraintLayout.LayoutParams
                                params.topMargin = (32 * resources.displayMetrics.density).toInt()
                                params.topToBottom = binding.cardView2.id
                                binding.imageView9.layoutParams = params
                            }

                            // 리뷰 관련 뷰들 항상 표시
                            textView7.visibility = View.VISIBLE
                            reviewRV.visibility = View.VISIBLE
                            reviewPlus.visibility = View.VISIBLE
                            addButton.visibility = View.VISIBLE
                            imageView3.visibility = View.VISIBLE

                            // 예약 버튼은 호텔일 때만 보이도록 설정
                            reserveButton.visibility = if (isHotel) View.VISIBLE else View.GONE

                            // 찜하기 버튼 설정
                            wishButton.setImageResource(
                                if (placeDetail.wished) R.drawable.ic_wish_check
                                else R.drawable.ic_wish
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

                            // 특성 카드 설정
                            placeDetail.keywords?.let { keywords ->
                                setupCharacteristicCards(placeDetail.category, keywords)
                            }

                            // 이미지 업데이트
                            updateImages(placeDetail.imageUrls)

                            // 모든 카테고리에서 리뷰 업데이트
                            reviewCount.text = placeDetail.reviewCount.toString()
                            updateReviews(placeDetail.recentReviews)

                            // 지도 위치 업데이트
                            if (::googleMap.isInitialized) {
                                updateMapLocation(placeDetail.latitude, placeDetail.longitude)
                            }
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

    private fun setupCharacteristicCards(category: String, keywords: List<String>) {
        val (backgroundColor, textColor) = when (category) {
            "HOSPITAL" -> "#EAF2FE" to "#276CCB"  // 동물병원용 색상
            "HOTEL" -> "#FEEEEA" to "#F36037"     // 호텔용 색상
            "RESTAURANT", "CAFE" -> "#FFFBF1" to "#FF8D41"  // 음식점/카페용 색상
            "ETC" -> "#F6F6F6" to "#9E9E9E" //기타용 색상
            else -> "#F6F6F6" to "#9E9E9E"  // 기본값
        }

        binding.apply {
            // 기존 카드들 모두 제거
            characteristicsContainer.removeAllViews()

            // 각 키워드에 대해 동적으로 카드 생성
            keywords.forEach { keyword ->
                val cardView = CardView(requireContext()).apply {
                    layoutParams = FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(
                            0,
                            0,
                            resources.getDimensionPixelSize(R.dimen.spacing_14),
                            resources.getDimensionPixelSize(R.dimen.spacing_8)
                        )
                    }
                    radius = resources.getDimension(R.dimen.radius_4)
                    cardElevation = 0f
                    setCardBackgroundColor(Color.parseColor(backgroundColor))
                    val strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_1)
                    setContentPadding(strokeWidth, strokeWidth, strokeWidth, strokeWidth)

                    val innerCard = CardView(requireContext()).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setCardBackgroundColor(Color.parseColor(backgroundColor))
                        radius = resources.getDimension(R.dimen.radius_4) - strokeWidth
                        cardElevation = 0f

                        val textView = TextView(context).apply {
                            layoutParams = ViewGroup.MarginLayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            text = keyword
                            setTextColor(Color.parseColor(textColor))
                            textSize = 14f
                            setPadding(
                                resources.getDimensionPixelSize(R.dimen.spacing_14),
                                resources.getDimensionPixelSize(R.dimen.spacing_3),
                                resources.getDimensionPixelSize(R.dimen.spacing_14),
                                resources.getDimensionPixelSize(R.dimen.spacing_3)
                            )
                        }
                        addView(textView)
                    }
                    addView(innerCard)
                }
                characteristicsContainer.addView(cardView)
            }

            // 정보추가하기 버튼 추가
            val addButton = CardView(requireContext()).apply {
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(
                        0,
                        0,
                        resources.getDimensionPixelSize(R.dimen.spacing_14),
                        resources.getDimensionPixelSize(R.dimen.spacing_8)
                    )
                }
                radius = resources.getDimension(R.dimen.radius_4)
                cardElevation = 0f
                setCardBackgroundColor(Color.parseColor(textColor))
                val strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_1)
                setContentPadding(strokeWidth, strokeWidth, strokeWidth, strokeWidth)

                val innerCard = CardView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setCardBackgroundColor(Color.WHITE)
                    radius = resources.getDimension(R.dimen.radius_4) - strokeWidth
                    cardElevation = 0f

                    val textView = TextView(context).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        text = "+ 정보추가하기"
                        setTextColor(Color.parseColor(textColor))
                        textSize = 14f
                        setPadding(
                            resources.getDimensionPixelSize(R.dimen.spacing_14),
                            resources.getDimensionPixelSize(R.dimen.spacing_3),
                            resources.getDimensionPixelSize(R.dimen.spacing_14),
                            resources.getDimensionPixelSize(R.dimen.spacing_3)
                        )
                    }
                    addView(textView)
                }
                addView(innerCard)

                setOnClickListener {
                    val placeId = arguments?.getInt("placeId") ?: return@setOnClickListener
                    val placeName = binding.placeName.text.toString()

                    // 카테고리에 따른 기본 키워드 설정
                    val defaultKeywords = when (category) {
                        "HOSPITAL" -> listOf("고양이친화", "중성화수술", "CCTV")
                        "HOTEL" -> listOf("고양이친화", "CCTV", "주차")
                        "RESTAURANT", "CAFE" -> listOf("예약", "포장", "주차")
                        else -> emptyList()
                    }

                    // MapAddKeywordFragment로 이동
                    val fragment = MapAddKeywordFragment.newInstance(
                        placeId = placeId,
                        placeName = placeName,
                        defaultKeywords = defaultKeywords.toTypedArray(),
                        category = category,
                        currentKeywords = keywords,
                        additionalInfo = binding.additionalInfo.text.toString()
                    )

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

            characteristicsContainer.addView(addButton)
        }
    }

    private fun setupGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapView2) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.uiSettings.apply {
            isZoomControlsEnabled = false
            isScrollGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isZoomGesturesEnabled = false
        }

        val latitude = actualPlaceLatitude ?: placeLatitude
        val longitude = actualPlaceLongitude ?: placeLongitude

        updateMapLocation(latitude, longitude)
    }

    private fun updateMapLocation(lat: Double, lng: Double) {
        val location = LatLng(lat, lng)
        Log.d("MapDetailFragment", "Updating map location to: lat=$lat, lng=$lng")

        currentMarker?.remove()

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                15.0f
            )
        )

        val markerIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker)

        currentMarker = googleMap.addMarker(
            MarkerOptions()
                .position(location)
                .icon(markerIcon)
        )
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun updateImages(imageUrls: List<String>?) {
        if (imageUrls.isNullOrEmpty()) {
            // RecyclerView 숨기고 기본 이미지 표시
            binding.detailImgRV.visibility = View.GONE
            binding.defaultDetailImage.visibility = View.VISIBLE
            binding.defaultImgText.visibility = View.VISIBLE

            // imageView7의 제약 조건 변경
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.scrollView3.getChildAt(0) as ConstraintLayout)
            constraintSet.connect(
                R.id.imageView7,
                ConstraintSet.TOP,
                R.id.defaultDetailImage,
                ConstraintSet.BOTTOM,
                (16 * resources.displayMetrics.density).toInt() // 16dp를 픽셀로 변환
            )
            constraintSet.applyTo(binding.scrollView3.getChildAt(0) as ConstraintLayout)
        } else {
            // 기본 이미지 숨기고 RecyclerView 표시
            binding.defaultDetailImage.visibility = View.GONE
            binding.detailImgRV.visibility = View.VISIBLE
            binding.defaultImgText.visibility = View.GONE

            // imageView7의 제약 조건을 원래대로 복원
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.scrollView3.getChildAt(0) as ConstraintLayout)
            constraintSet.connect(
                R.id.imageView7,
                ConstraintSet.TOP,
                R.id.detailImgRV,
                ConstraintSet.BOTTOM,
                (16 * resources.displayMetrics.density).toInt() // 16dp를 픽셀로 변환
            )
            constraintSet.applyTo(binding.scrollView3.getChildAt(0) as ConstraintLayout)

            // RecyclerView 데이터 업데이트
            imgDatas.clear()
            imageUrls.forEach { url ->
                if (!url.isNullOrBlank()) {
                    imgDatas.add(DetailImg(url))
                }
            }

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
                    binding.detailImgRV.adapter = DetailImgRVAdapter(ArrayList(imgDatas))
                }
            }
        }
    }

    private fun updateReviews(apiReviews: List<MapReview>?) {
        lifecycleScope.launch {
            try {
                val nickname = fetchUserNickname()

                reviewDatas.clear()

                if (!apiReviews.isNullOrEmpty()) {
                    // 리뷰가 있는 경우
                    reviewDatas.addAll(apiReviews)

                    // 리뷰 관련 뷰들 표시
                    binding.reviewRV.visibility = View.VISIBLE
                    binding.defaultReviewImage.visibility = View.GONE
                    binding.defaultReviewText.visibility = View.GONE
                    binding.reviewPlus.visibility = View.VISIBLE
                    binding.addButton.visibility = View.VISIBLE
                    binding.imageView3.visibility = View.VISIBLE

                    // 최대 2개의 리뷰만 표시
                    val displayedReviews = ArrayList<MapReview>().apply {
                        addAll(reviewDatas.take(2))
                    }

                    // 어댑터 설정 및 데이터 업데이트
                    if (binding.reviewRV.adapter == null) {
                        val mapReviewRVAdapter = MapReviewRVAdapter(
                            displayedReviews,
                            nickname
                        ) {
                            // 리뷰가 삭제되면 장소 상세 정보를 새로고침
                            arguments?.getInt("placeId")?.let { placeId ->
                                loadPlaceDetails(placeId)
                            }
                        }
                        binding.reviewRV.apply {
                            adapter = mapReviewRVAdapter
                            layoutManager = LinearLayoutManager(context).apply {
                                orientation = LinearLayoutManager.VERTICAL
                            }
                        }

                        // "더보기" 버튼 클릭 이벤트 설정
                        binding.reviewPlus.setOnClickListener {
                            val placeId = arguments?.getInt("placeId") ?: return@setOnClickListener
                            val mapReviewFragment = MapReviewFragment.newInstance(placeId)
                            requireActivity().supportFragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left,
                                    R.anim.slide_in_left,
                                    R.anim.slide_out_right
                                )
                                .hide(this@MapDetailFragment)
                                .add(R.id.main_frm, mapReviewFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    } else {
                        (binding.reviewRV.adapter as MapReviewRVAdapter).updateReviews(displayedReviews)
                    }

                    // 리뷰 작성 버튼 클릭 이벤트 설정
                    binding.addButton.setOnClickListener {
                        arguments?.getInt("placeId")?.let { placeId ->
                            val mapAddReviewFragment = MapAddReviewFragment.newInstance(placeId)
                            requireActivity().supportFragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left,
                                    R.anim.slide_in_left,
                                    R.anim.slide_out_right
                                )
                                .hide(this@MapDetailFragment)
                                .add(R.id.main_frm, mapAddReviewFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                } else {
                    // 리뷰가 없는 경우
                    binding.reviewCount.text = "0"
                    binding.reviewRV.visibility = View.GONE
                    binding.defaultReviewImage.visibility = View.VISIBLE
                    binding.defaultReviewText.visibility = View.VISIBLE
                    binding.reviewPlus.visibility = View.GONE
                    binding.addButton.visibility = View.VISIBLE
                    binding.imageView3.visibility = View.VISIBLE

                    // 제약 조건 변경
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(binding.scrollView3.getChildAt(0) as ConstraintLayout)
                    constraintSet.connect(
                        R.id.addButton,
                        ConstraintSet.TOP,
                        R.id.defaultReviewImage,
                        ConstraintSet.BOTTOM,
                        (24 * resources.displayMetrics.density).toInt()
                    )
                    constraintSet.applyTo(binding.scrollView3.getChildAt(0) as ConstraintLayout)

                    // 리뷰가 없는 경우의 리뷰 작성 버튼 클릭 이벤트
                    binding.addButton.setOnClickListener {
                        arguments?.getInt("placeId")?.let { placeId ->
                            val mapAddReviewFragment = MapAddReviewFragment.newInstance(placeId)
                            requireActivity().supportFragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left,
                                    R.anim.slide_in_left,
                                    R.anim.slide_out_right
                                )
                                .hide(this@MapDetailFragment)
                                .add(R.id.main_frm, mapAddReviewFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleError(e)
                    Log.e("MapDetailFragment", "리뷰 업데이트 중 오류 발생", e)
                }
            }
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

    private fun isCurrentlyOpen(businessHours: String): Boolean {
        // 한국 시간대 설정
        val koreaTimeZone = TimeZone.getTimeZone("Asia/Seoul")
        val calendar = Calendar.getInstance(koreaTimeZone)

        // 디버깅을 위한 로그 추가
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        Log.d("TimeCheck", "현재 시간: $currentHour:$currentMinute")

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentTime = currentHour * 60 + currentMinute // 현재 시간을 분 단위로 변환

        val koreanDayName = when (dayOfWeek) {
            Calendar.MONDAY -> "월요일"
            Calendar.TUESDAY -> "화요일"
            Calendar.WEDNESDAY -> "수요일"
            Calendar.THURSDAY -> "목요일"
            Calendar.FRIDAY -> "금요일"
            Calendar.SATURDAY -> "토요일"
            Calendar.SUNDAY -> "일요일"
            else -> return false
        }

        val todayHours = businessHours.split(", ")
            .find { it.startsWith(koreanDayName) }
            ?.substringAfter(": ")
            ?: return false

        // 디버깅을 위한 로그 추가
        Log.d("TimeCheck", "영업시간: $todayHours")

        return when {
            todayHours.contains("24시간") -> true
            todayHours.contains("휴무") -> false
            else -> {
                try {
                    val timeRange = todayHours.split(" ~ ")
                    if (timeRange.size != 2) return false

                    // 시작 시간과 종료 시간을 분 단위로 변환
                    val startMinutes = convertTimeToMinutes(timeRange[0])
                    var endMinutes = convertTimeToMinutes(timeRange[1])

                    // 디버깅을 위한 로그 추가
                    Log.d("TimeCheck", "시작 시간(분): $startMinutes, 종료 시간(분): $endMinutes, 현재 시간(분): $currentTime")

                    // 종료 시간이 시작 시간보다 작은 경우 (다음날로 이어지는 경우)
                    if (endMinutes < startMinutes) {
                        endMinutes += 24 * 60 // 24시간을 더해줌
                    }

                    // 현재 시간이 영업 시간 내에 있는지 확인
                    val isOpen = currentTime in startMinutes..endMinutes
                    Log.d("TimeCheck", "영업 여부: $isOpen")
                    isOpen
                } catch (e: Exception) {
                    Log.e("TimeCheck", "시간 변환 중 오류 발생", e)
                    false
                }
            }
        }
    }

    private fun convertTimeToMinutes(timeStr: String): Int {
        try {
            // 디버깅을 위한 로그 추가
            Log.d("TimeCheck", "시간 문자열 변환 시도: $timeStr")

            val (hours, minutes) = when {
                timeStr.contains("오전") -> {
                    val time = timeStr.replace("오전 ", "").split(":")
                    if (time[0] == "12") {
                        Pair(0, time.getOrElse(1) { "0" }.toInt())
                    } else {
                        Pair(time[0].toInt(), time.getOrElse(1) { "0" }.toInt())
                    }
                }
                timeStr.contains("오후") -> {
                    val time = timeStr.replace("오후 ", "").split(":")
                    if (time[0] == "12") {
                        Pair(12, time.getOrElse(1) { "0" }.toInt())
                    } else {
                        Pair(time[0].toInt() + 12, time.getOrElse(1) { "0" }.toInt())
                    }
                }
                else -> {
                    val time = timeStr.split(":")
                    Pair(time[0].toInt(), time.getOrElse(1) { "0" }.toInt())
                }
            }
            val totalMinutes = hours * 60 + minutes

            // 디버깅을 위한 로그 추가
            Log.d("TimeCheck", "변환된 시간(분): $totalMinutes (${hours}시 ${minutes}분)")

            return totalMinutes
        } catch (e: Exception) {
            Log.e("TimeCheck", "시간 문자열 변환 실패: $timeStr", e)
            return 0
        }
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private suspend fun fetchUserNickname(): String {
        try {
            val token = getToken() ?: run {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return ""
            }

            val response = withContext(Dispatchers.IO) {
                RetrofitClient.userApiService.getUser("Bearer $token").execute()
            }

            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null && userResponse.isSuccess) {
                    return userResponse.result?.nickname ?: ""
                }
            }
        } catch (e: Exception) {
            Log.e("MapDetailFragment", "사용자 정보 가져오기 중 예외 발생", e)
        }
        return ""
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

    // 기존 함수들은 유지
    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        // 기본 RecyclerView 설정
        val detailImgRVAdapter = DetailImgRVAdapter(imgDatas)
        binding.detailImgRV.apply {
            adapter = detailImgRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.reviewPlus.setOnClickListener {
            val placeId = arguments?.getInt("placeId") ?: return@setOnClickListener
            val mapReviewFragment = MapReviewFragment.newInstance(placeId)
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .hide(this@MapDetailFragment)
                .add(R.id.main_frm, mapReviewFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupAddReviewButton() {
        binding.addButton.setOnClickListener {
            arguments?.getInt("placeId")?.let { placeId ->
                val mapAddReviewFragment = MapAddReviewFragment.newInstance(placeId)
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,  // 새 프래그먼트가 오른쪽에서 들어옴
                        R.anim.slide_out_left,  // 현재 프래그먼트가 왼쪽으로 나감
                        R.anim.slide_in_left,   // 뒤로가기 시 현재 프래그먼트가 왼쪽에서 들어옴
                        R.anim.slide_out_right  // 새 프래그먼트가 오른쪽으로 나감
                    )
                    .hide(this)  // replace 대신 hide 사용
                    .add(R.id.main_frm, mapAddReviewFragment)  // add로 새 프래그먼트 추가
                    .addToBackStack(null)
                    .commit()
            }
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

    fun refreshPlaceDetails() {
        arguments?.getInt("placeId")?.let { id ->
            loadPlaceDetails(id)
        }
    }

    companion object {
        fun newInstance(placeId: Int, latitude: Double, longitude: Double): MapDetailFragment {
            return MapDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("placeId", placeId)
                    putDouble("latitude", latitude)
                    putDouble("longitude", longitude)
                }
            }
        }
    }
}