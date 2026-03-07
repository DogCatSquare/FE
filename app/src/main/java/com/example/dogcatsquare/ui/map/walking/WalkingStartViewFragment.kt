package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
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
import com.example.dogcatsquare.databinding.FragmentMapwalkingStartviewBinding
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
    private var _binding: FragmentMapwalkingStartviewBinding? = null
    private val binding get() = _binding!!

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

    val apiService: WalkingApiService by lazy {
        retrofit.create(WalkingApiService::class.java)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleMap()
        setupButtons()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
//        walkRVAdapter = WalkRVAdapter (
//            onItemClick = { walkId ->
//                val fragment = WalkingStartViewFragment.newInstance(walkId)
//                parentFragmentManager.beginTransaction()
//                    .replace(R.id.main_frm, fragment)
//                    .addToBackStack(null)
//                    .commit()
//            },
//            walkList = arrayListOf() // 초기 데이터로 빈 리스트 전달
//        )
//        binding.reviewRv.apply {
//            adapter = walkRVAdapter
//            layoutManager = LinearLayoutManager(context)
//        }
        // 리사이클러뷰 설정
        val recyclerView: RecyclerView = binding.reviewRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        reviewAdapter = ReviewAdapter(emptyList())
        recyclerView.adapter = reviewAdapter

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

                val reviewSubtitle: TextView = binding.reviewSubtitle
                reviewSubtitle.text = "${reviews.size}"
            } else {
                Log.e("WalkingStartView", "Failed to load reviews.")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapwalkingStartviewBinding.inflate(inflater, container, false)

        walkId = arguments?.getInt("walkId")?.toString()
        Log.d("WalkingStartViewFragment", "Received walkId: $walkId")

        val address = arguments?.getString("address", "서대문 안산지락길")
        val walkId = walkId?.toIntOrNull()

        // Toolbar 설정
        (activity as? AppCompatActivity)?.apply {
            val toolbar: Toolbar = binding.walkingStartToolbar
            setSupportActionBar(toolbar)
            supportActionBar?.title = address
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        // MapFragment 설정
        walkDetailViewModel.walkDetailState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WalkDetailState.Loading -> {
                    // 필요 시 로딩 프로그래스바 표시
                }
                is WalkDetailState.Success -> {
                    val walkDetail = state.walkDetail

                    // UI 텍스트 및 이미지 업데이트 (이미 만들어두신 updateUI 호출)
                    updateUI(walkDetail)

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
        val wishBt: ImageButton = binding.wishBt
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
        walkId?.let {
            walkDetailViewModel.fetchWalkDetail(it)
            walkReviewViewModel.getWalkReviews(it)
            Log.d("WalkingStartView", it.toString())
        }

        return binding.root
    }

    private fun setupButtons() {
        binding.apply {
            // 후기 목록 버튼
            val reviewButton = binding.reviewButton
            reviewButton.setOnClickListener {
                val fragment = WalkingReviewListFragment().apply {
                    arguments = Bundle().apply {
                        putInt("walkId", walkId?.toInt() ?: -1)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .hide(this@WalkingStartViewFragment)
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            // 후기 작성 버튼
            val reviewListBtn = binding.reviewBt
            reviewListBtn.setOnClickListener {
                val fragment = WalkingReviewAddFragment().apply {
                    arguments = Bundle().apply {
                        putInt("walkId", walkId?.toInt() ?: -1)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun updateUI(walkDetail: WalkDetail) {
        val descriptionTextView: TextView = binding.reviewTv
        val lengthTextView: TextView = binding.lengthTv
        val timeTextView: TextView = binding.timeTv
        val profileIg: ImageView = binding.profileIg
        val profileNameTv: TextView = binding.profileNameTv
        val profileTv: TextView = binding.profileTv
        val startTv: TextView = binding.startTv
        val endTv: TextView = binding.endTv
        val toolbar: Toolbar = binding.walkingStartToolbar

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
        val filter1 = binding.filter1 // XML에 ID 추가 필요
        filter1.setImageResource(difficultyResId)

        val filter2 = binding.filter2     // XML에 ID 추가 필요
        val filter3 = binding.filter3

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
            binding.walkImg1,
            binding.walkImg2,
            binding.walkImg3,
            binding.walkImg4,
            binding.walkImg5,
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

    fun refreshPlaceDetails() {
        arguments?.getInt("walkId")?.let { id ->
            // MapFragment 설정
            walkDetailViewModel.walkDetailState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is WalkDetailState.Loading -> {
                        // 필요 시 로딩 프로그래스바 표시
                    }

                    is WalkDetailState.Success -> {
                        val walkDetail = state.walkDetail

                        // UI 텍스트 및 이미지 업데이트 (이미 만들어두신 updateUI 호출)
                        updateUI(walkDetail)

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
        }
    }
}
