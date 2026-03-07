package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitClient.retrofit
import com.example.dogcatsquare.ui.map.walking.data.Response.Coordinate
import com.example.dogcatsquare.ui.map.walking.data.Response.Walk
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetail
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkDetailState
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkDetailViewModel
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.naver.maps.geometry.LatLng
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor

class WalkingStartViewFragment : Fragment(), OnMapReadyCallback {

    private lateinit var reviewAdapter: ReviewAdapter
    private val walkDetailViewModel: WalkDetailViewModel by viewModels()
    private val walkReviewViewModel: WalkReviewViewModel by viewModels()
    private var walkId: String? = null
    private val coords = mutableListOf<LatLng>()
    private var walk: Walk? = null
    private var lat : Double? = null
    private var lon : Double? = null

    private lateinit var googleMap: GoogleMap
    private var placeLatitude: Double = 0.0
    private var placeLongitude: Double = 0.0
    private var currentMarker: com.google.android.gms.maps.model.Marker? = null
    private var actualPlaceLatitude: Double? = null
    private var actualPlaceLongitude: Double? = null

    val apiService: WalkApiService by lazy {
        retrofit.create(WalkApiService::class.java)
    }

    companion object {
        fun newInstance(walkId: Int): WalkingStartViewFragment {
            val fragment = WalkingStartViewFragment()
            val args = Bundle()
            args.putInt("walkId", walkId)
            fragment.arguments = args
            return fragment
        }
    }

    private fun setupGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
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
        val location = com.google.android.gms.maps.model.LatLng(lat, lng)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mapwalking_startview, container, false)
        walkId = arguments?.getInt("walkId")?.toString()
        Log.d("WalkingStartViewFragment", "Received walkId: $walkId")
        setupGoogleMap()

        val address = arguments?.getString("address", "서대문 안산지락길")
        val placeId = walkId?.toIntOrNull()

        // Toolbar 설정
        (activity as? AppCompatActivity)?.apply {
            val toolbar: Toolbar = view.findViewById(R.id.walking_start_toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.title = address
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        // 리사이클러뷰 설정
        val recyclerView: RecyclerView = view.findViewById(R.id.review_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        reviewAdapter = ReviewAdapter(emptyList())
        recyclerView.adapter = reviewAdapter

        // MapFragment 설정
        walkDetailViewModel.walkDetailState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WalkDetailState.Loading -> {
                    // 필요 시 로딩 프로그래스바 표시
                }
                is WalkDetailState.Success -> {
                    val walkDetail = state.walkDetail

                    // UI 텍스트 및 이미지 업데이트 (이미 만들어두신 updateUI 호출)
                    updateUI(view, walkDetail)

                    // 지도 관련 로직: 시작 좌표가 있으면 지도를 이동시키고 마커를 찍음
                    val startCoord = walkDetail.startCoordinates?.firstOrNull()
                    if (startCoord != null) {
                        // 구글맵이 준비된 상태인지 확인 후 업데이트
                        if (::googleMap.isInitialized) {
                            updateMapLocation(startCoord.latitude, startCoord.longitude)
                        } else {
                            // 구글맵이 아직 준비 안됐다면 좌표만 저장해둠 (onMapReady에서 사용)
                            actualPlaceLatitude = startCoord.latitude
                            actualPlaceLongitude = startCoord.longitude
                        }
                    }
                }
                is WalkDetailState.Error -> {
                    Log.e("WalkingStartView", "상세 정보 로드 실패: ${state.message}")
                }
            }
        }

        // 산책로 위시 버튼 설정
        val wishBt: ImageButton = view.findViewById(R.id.wish_bt)
        var isChecked = false
        wishBt.setOnClickListener {
            isChecked = !isChecked
            if (isChecked) {
                wishBt.setImageResource(R.drawable.ic_wish_check)
            } else {
                wishBt.setImageResource(R.drawable.ic_wish)
            }
        }

        // API 호출
        placeId?.let {
            walkDetailViewModel.fetchWalkDetail(it)
            walkReviewViewModel.getWalkReviews(it)
            Log.d("WalkingStartView", it.toString())
        }

        // 산책로 후기
        walkReviewViewModel.reviewResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                val reviews = response.result?.walkReviews?.map { walkReview ->
                    WalkReview(
                        reviewId = walkReview.reviewId,
                        walkId = walkReview.walkId,
                        content = walkReview.content,
                        walkReviewImageUrl = walkReview.walkReviewImageUrl,
                        createdAt = walkReview.createdAt,
                        updatedAt = walkReview.updatedAt,
                        createdBy = walkReview.createdBy
                    )
                } ?: emptyList()

                reviewAdapter = ReviewAdapter(reviews)
                recyclerView.adapter = reviewAdapter
                reviewAdapter.notifyDataSetChanged()

                val reviewSubtitle: TextView = view.findViewById(R.id.review_subtitle)
                reviewSubtitle.text = "${reviews.size}"
            } else {
                Log.e("WalkingStartView", "Failed to load reviews.")
            }
        }

        // 리뷰 버튼 설정
        val reviewButton: ImageButton = view.findViewById(R.id.review_button)
        reviewButton.setOnClickListener {
            val fragment = WalkingReviewAllFragment().apply {
                arguments = Bundle().apply {
                    putInt("walkId", walkId?.toInt() ?: -1)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }
        return view
    }

    private fun updateUI(view: View, walkDetail: WalkDetail) {
        val descriptionTextView: TextView = view.findViewById(R.id.review_tv)
        val lengthTextView: TextView = view.findViewById(R.id.length_tv)
        val timeTextView: TextView = view.findViewById(R.id.time_tv)
        val profileIg: ImageView = view.findViewById(R.id.profile_ig)
        val profileNameTv: TextView = view.findViewById(R.id.profile_name_tv)
        val profileTv: TextView = view.findViewById(R.id.profile_tv)
        val startTv: TextView = view.findViewById(R.id.start_tv)
        val endTv: TextView = view.findViewById(R.id.end_tv)
        val toolbar: Toolbar = view.findViewById(R.id.walking_start_toolbar)

        toolbar.title = walkDetail.title // API 응답의 title로 변경
        descriptionTextView.text = walkDetail.description
        lengthTextView.text = "${walkDetail.distance} km"
        timeTextView.text = "${walkDetail.time} 분"
        startTv.text = "${walkDetail.startAddress}"
        endTv.text = "${walkDetail.endAddress}"

        Glide.with(requireContext())
            .load(walkDetail.createdBy.profileImageUrl)
            .into(profileIg)

        profileNameTv.text = walkDetail.createdBy.nickname
        profileTv.text = walkDetail.createdBy.breed ?: ""

        // 난이도에 따라 이미지 변경
        val difficultyResId = when (walkDetail.difficulty) {
            "LOW" -> R.drawable.ic_easy
            "HIGH" -> R.drawable.ic_difficulty
            else -> R.drawable.ic_normal
        }

        // 2. 편의시설(Special) 이미지 설정
        // walkDetail.special 이 List<String> 형태라고 가정합니다.
        val filter1 = view.findViewById<ImageView>(R.id.filter1) // XML에 ID 추가 필요
        filter1.setImageResource(difficultyResId)

        val filter2 = view.findViewById<ImageView>(R.id.filter2)     // XML에 ID 추가 필요
        val filter3 = view.findViewById<ImageView>(R.id.filter3)

        // 초기에는 안 보이게 설정하거나 기본값 세팅
        filter2.visibility = View.GONE
        filter3.visibility = View.GONE

        walkDetail.special?.take(2)?.forEachIndexed { index, type ->
            // 인덱스에 따라 어떤 ImageView에 넣을지 결정합니다.
            val targetImageView = if (index == 0) filter2 else filter3

            when (type.type) {
                "WASTEBASKET" -> {
                    targetImageView.visibility = View.VISIBLE
                    targetImageView.setImageResource(R.drawable.ic_garbage)
                }
                "WATER" -> {
                    targetImageView.visibility = View.VISIBLE
                    targetImageView.setImageResource(R.drawable.ic__water)
                }
                "TOILET" -> {
                    targetImageView.visibility = View.VISIBLE
                    targetImageView.setImageResource(R.drawable.ic_toilet)
                }
                "PARKING" -> {
                    targetImageView.visibility = View.VISIBLE
                    targetImageView.setImageResource(R.drawable.ic_park)
                }
                "STAIRS" -> {
                    targetImageView.visibility = View.VISIBLE
                    targetImageView.setImageResource(R.drawable.ic_stairs)
                }
            }
        }

        val imageButtons = listOf(
            view.findViewById<ImageButton>(R.id.walk_img1),
            view.findViewById<ImageButton>(R.id.walk_img2),
            view.findViewById<ImageButton>(R.id.walk_img3),
            view.findViewById<ImageButton>(R.id.walk_img4),
            view.findViewById<ImageButton>(R.id.walk_img5)
        )

        // 모든 버튼 초기화 (숨기기)
        imageButtons.forEach { it.visibility = View.GONE }

        // 데이터 로드 (최대 5개)
        walkDetail.walkImageUrl?.take(5)?.forEachIndexed { index, url ->
            val button = imageButtons[index]
            button.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(url)
                .into(button)
        }

        // 리스트가 비어있지 않은지 확인 후 첫 번째 좌표 가져오기
//        val startCoordinate = walkDetail.startCoordinates?.firstOrNull()
//        val endCoordinate = walkDetail.endCoordinates?.firstOrNull()

//        if (startCoordinate != null && endCoordinate != null) {
//            setInitialMarker(startCoordinate, endCoordinate)
//        } else {
//            Log.e("WalkingStartView", "좌표가 유효하지 않습니다: 시작 좌표나 끝 좌표가 없음.")
//        }
    }
}
