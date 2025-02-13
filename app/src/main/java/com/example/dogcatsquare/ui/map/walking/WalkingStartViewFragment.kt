package com.example.dogcatsquare.ui.map.walking

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.ui.map.location.MapFragment
import com.example.dogcatsquare.ui.map.walking.data.Response.Coordinate
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkDetailViewModel
import com.google.android.gms.maps.model.Polyline
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class WalkingStartViewFragment : Fragment() {

    private lateinit var reviewAdapter: ReviewAdapter
    private val walkDetailViewModel: WalkDetailViewModel by viewModels()

    // Naver Map 객체 선언
    private lateinit var naverMap: NaverMap
    private val coords = mutableListOf<LatLng>()
    private lateinit var userPolyline: Polyline

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Log.d("WalkingStartViewFragment", "onCreateView 실행됨")
        val view = inflater.inflate(R.layout.fragment_mapwalking_startview, container, false)

        val address = arguments?.getString("address", "서대문 안산지락길")
        val walkId = arguments?.getLong("walkId", -1) ?: -1 // 받아온 산책로 ID

        // Toolbar 설정
        (activity as? AppCompatActivity)?.apply {
            val toolbar: Toolbar = view.findViewById(R.id.walking_start_toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.title = address
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            toolbar.setNavigationOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, MapFragment())
                    .commit()
            }
        }

        // 리사이클러뷰 설정
        val recyclerView: RecyclerView = view.findViewById(R.id.review_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val reviewSubtitle: TextView = view.findViewById(R.id.review_subtitle)
//        val titleTextView: TextView = view.findViewById(R.id.walking_start_toolbar)
        val descriptionTextView: TextView = view.findViewById(R.id.review_tv)
        val lengthTextView: TextView = view.findViewById(R.id.length_tv)
        val timeTextView: TextView = view.findViewById(R.id.time_tv)
        val profileIg: ImageView = view.findViewById(R.id.profile_ig)
        val profileNameTv: TextView = view.findViewById(R.id.profile_name_tv)
        val profileTv: TextView = view.findViewById(R.id.profile_tv)

        reviewAdapter = ReviewAdapter(emptyList())
        recyclerView.adapter = reviewAdapter

        // API 요청
        if (walkId != -1L) {
            walkDetailViewModel.fetchWalkDetail(walkId)
        }

        val difficultyImageView: ImageView = view.findViewById(R.id.difficulty_iv)

        walkDetailViewModel.walkDetail.observe(viewLifecycleOwner) { walkDetail ->
            if (walkDetail == null) {
                Log.d("API_RESPONSE", "walkDetail 데이터 없음")
            } else {
                Log.d("API_RESPONSE", "walkDetail: $walkDetail")  // 정상적으로 데이터가 전달되면 이 로그가 출력됩니다.
            }
            walkDetail?.let {
//                titleTextView.text = it.title
                (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = it.title

                descriptionTextView.text = it.description
//                reviewSubtitle.text = "${it.special.size} 개의 특징"

                // 난이도에 따라 이미지 변경
                val difficultyResId = when (it.difficulty) {
                    "easy" -> R.drawable.ic_easy
                    "normal" -> R.drawable.ic_normal
                    "difficult" -> R.drawable.ic_difficulty
                    else -> R.drawable.ic_normal // 기본값 (예외 처리)
                }
                difficultyImageView.setImageResource(difficultyResId)

                lengthTextView.text = "${it.distance} km"
                timeTextView.text = "${it.time} 분"

                Glide.with(requireContext())
                    .load(it.createdBy.profileImageUrl)
                    .into(profileIg)

                profileNameTv.text = it.createdBy.nickname
                profileTv.text = it.createdBy.breed ?: "알 수 없음"

                // 좌표 받아서 마커 및 경로 추가
                val startCoordinate = it.startCoordinates.first()
                val endCoordinate = it.endCoordinates.first()
                setInitialMarker(startCoordinate, endCoordinate)
//                drawRoute(it.startCoordinates, it.endCoordinates)
            }
        }

        // 리뷰 작성 버튼 설정
        val button: Button = view.findViewById(R.id.ReviewWriting_bt)
        button.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WalkingMapViewFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    // 시작점과 끝점에 마커를 추가하는 함수
    private fun setInitialMarker(startCoordinate: Coordinate, endCoordinate: Coordinate) {
        // 시작점 마커 추가
        val startMarker = Marker()
        startMarker.icon = OverlayImage.fromResource(R.drawable.ic_start_marker)
        startMarker.position = LatLng(startCoordinate.latitude, startCoordinate.longitude)
        startMarker.map = naverMap

        // 끝점 마커 추가
        val endMarker = Marker()
        endMarker.icon = OverlayImage.fromResource(R.drawable.ic_end_marker)
        endMarker.position = LatLng(endCoordinate.latitude, endCoordinate.longitude)
        endMarker.map = naverMap
    }

//    private fun drawRoute(startCoordinates: List<Coordinate>, endCoordinates: List<Coordinate>) {
//        val routeCoordinates = mutableListOf<LatLng>()
//
//        // 경로 좌표 추가 (시작과 끝 좌표 외에도 중간 좌표를 추가할 수 있습니다)
//        startCoordinates.forEach {
//            routeCoordinates.add(LatLng(it.latitude, it.longitude))
//        }
//        endCoordinates.forEach {
//            routeCoordinates.add(LatLng(it.latitude, it.longitude))
//        }
//
//        // Polyline 객체 초기화
//        if (!::userPolyline.isInitialized) {
//            userPolyline = Polyline()
//            userPolyline.map = naverMap // NaverMap 객체와 연결
//        }
//
//        // Polyline의 좌표 업데이트
//        userPolyline.setCoords(routeCoordinates) // setCoords 메서드 사용
//        userPolyline.width = 10F // 선의 두께
//        userPolyline.color = Color.parseColor("#FFB200") // 선의 색상
//    }
}


