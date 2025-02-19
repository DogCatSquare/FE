package com.example.dogcatsquare.ui.map.walking

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkDetailState
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkDetailViewModel
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.button.MaterialButton
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class WalkingMapFragment : Fragment() {

    private lateinit var reviewAdapter: ReviewAdapter
    private val walkReviewViewModel: WalkReviewViewModel by viewModels()

    val address = arguments?.getString("address", "서대문 안산지락길")
    private var placeId : Int = 3

    private val walkDetailViewModel: WalkDetailViewModel by viewModels()

//    // Naver Map 객체 선언
    var naverMap: NaverMap? = null
    private val coords = mutableListOf<LatLng>()
    private lateinit var userPolyline: Polyline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            placeId = it.getInt("placeId", 3)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_mapwalking, container, false)

        // API 호출
        if (placeId != -1) {
            walkReviewViewModel.getWalkReviews(placeId)
            walkDetailViewModel.fetchWalkDetail(placeId)
            Log.d("WalkingMapFragment", placeId.toString())
        }

        // MapFragment 설정
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView3) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.mapView3, it).commit()
            }

        mapFragment.getMapAsync { naverMapInstance ->
            naverMap = naverMapInstance
            walkDetailViewModel.walkDetailState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is WalkDetailState.Success -> {
                        Log.d("WalkingMapFragment", "Walk Detail Success: ${state.walkDetail}")
                        val walkDetail = state.walkDetail
                        if (walkDetail != null) {
                            val placeName : TextView = view.findViewById(R.id.placeName)
                            placeName.text = walkDetail.title

                            // 좌표 가져오기
                            val startCoordinate = walkDetail.startCoordinates.first()
                            val latitude = startCoordinate.latitude
                            val longitude = startCoordinate.longitude

                            Log.d("WalkingMapFragment", "${latitude + longitude}")

                            // 지도에 마커 찍기
                            setMarker(latitude, longitude)
                            // 카메라 이동
                            val startLatLng = LatLng(latitude, longitude)
                            val cameraUpdate = CameraUpdate.scrollTo(startLatLng)
                            naverMap!!.moveCamera(cameraUpdate)
                        } else {
                            Log.d("WalkingMapFragment", "데이터가 없습니다.")
                        }
                    }
                    is WalkDetailState.Error -> {
                        Log.e("WalkingMapFragment", "API 호출 실패: ${state.message}")
                    }

                    WalkDetailState.Loading -> TODO()
                }
            }
        }

        // 산책로 위시 지정
        val wishBt: ImageButton = view.findViewById(R.id.wishButton)
        var isChecked = false

        wishBt.setOnClickListener {
            isChecked = !isChecked
            wishBt.setImageResource(if (isChecked) R.drawable.ic_wish_check else R.drawable.ic_wish)
        }

        // RecyclerView 설정
        val recyclerView: RecyclerView = view.findViewById(R.id.review_rv)
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

                val reviewSubtitle: TextView = view.findViewById(R.id.rightText)
                val reviewCount = reviews.size
                reviewSubtitle.text = "$reviewCount"

                Log.d("WalkingMapFragment", "Reviews: ${reviews.size}")  // 리뷰 개수 로그 확인

                reviewAdapter.updateData(reviews)  // 데이터 업데이트

                reviewAdapter.setOnItemClickListener { walkReview ->
                    val fragment = WalkingStartViewFragment()

                    val bundle = Bundle().apply {
                        putString("walkId", walkReview.walkId.toString())
                    }
                    fragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commit()
                }


            } else {
                Log.e("WalkingMapFragment", "Failed to load reviews.")
            }
        }

        val reviewAll : ImageButton = view.findViewById(R.id.reviewAll_bt)
        reviewAll.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WalkingReviewAllFragment())
                .addToBackStack(null)
                .commit()
        }

        val button: MaterialButton = view.findViewById(R.id.addButton)
        button.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WalkingMapViewFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    // 마커 추가 함수
    private fun setMarker(latitude: Double, longitude: Double) {
        val startLatLng = LatLng(latitude, longitude)
        Log.d("WalkingMapFragment", "setMarker실행")
        val marker = Marker()
        marker.position = startLatLng
        marker.icon = OverlayImage.fromResource(R.drawable.ic_marker_park)  // 마커 아이콘
        marker.map = naverMap
    }

}
