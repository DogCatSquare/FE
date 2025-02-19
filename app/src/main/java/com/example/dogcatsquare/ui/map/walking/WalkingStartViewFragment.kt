package com.example.dogcatsquare.ui.map.walking

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.ui.map.location.MapFragment
import com.example.dogcatsquare.ui.map.walking.data.Response.Coordinate
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetail
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkDetailState
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkDetailViewModel
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.button.MaterialButton
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class WalkingStartViewFragment : Fragment() {

    private lateinit var reviewAdapter: ReviewAdapter
    private val walkDetailViewModel: WalkDetailViewModel by viewModels()
    private val walkReviewViewModel : WalkReviewViewModel by viewModels()

    // Naver Map 객체 선언
    var naverMap: NaverMap? = null
    private val coords = mutableListOf<LatLng>()
    private lateinit var userPolyline: Polyline

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("WalkingStartView", "Created")
        val view = inflater.inflate(R.layout.fragment_mapwalking_startview, container, false)

        val address = arguments?.getString("address", "서대문 안산지락길")
        val placeId = arguments?.getInt("placeId") ?: 3

        Log.d("WalkingStartView", placeId.toString())

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


        //산책로 위시 지정
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
        if (placeId != -1) {
            walkDetailViewModel.fetchWalkDetail(placeId)
            walkReviewViewModel.getWalkReviews(placeId)
            Log.d("WalkingStartView", placeId.toString())
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
                val recyclerView: RecyclerView = view.findViewById(R.id.review_recycler_view)
                recyclerView.adapter = reviewAdapter
                reviewAdapter.notifyDataSetChanged()

                val reviewSubtitle: TextView = view.findViewById(R.id.review_subtitle)
                val reviewCount = reviews.size
                reviewSubtitle.text = "$reviewCount"
            } else {
                Log.e("WalkingStartView", "Failed to load reviews.")
            }
        }

        val review_button : ImageButton = view.findViewById(R.id.review_button)
        review_button.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WalkingReviewAllFragment())
                .addToBackStack(null)
                .commit()
        }


        walkDetailViewModel.walkDetailState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WalkDetailState.Success -> {
                    Log.d("WalkingStartView", "Walk Detail Success: ${state.walkDetail}")
                    val walkDetail = state.walkDetail
                    if (walkDetail != null) {
                        updateUI(view, walkDetail)
                    } else {
                        Log.d("WalkingStartView", "데이터가 없습니다.")
                    }
                }
                is WalkDetailState.Error -> {
                    Log.e("WalkingStartView", "API 호출 실패: ${state.message}")
                }

                WalkDetailState.Loading -> TODO()
            }
        }


        // 리뷰 작성 버튼 설정
        val button: MaterialButton = view.findViewById(R.id.ReviewWriting_bt)
        button.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WalkingMapViewFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    // UI 업데이트 함수
    private fun updateUI(view: View, walkDetail: WalkDetail) {

        val descriptionTextView: TextView = view.findViewById(R.id.review_tv)
        val lengthTextView: TextView = view.findViewById(R.id.length_tv)
        val timeTextView: TextView = view.findViewById(R.id.time_tv)
        val profileIg: ImageView = view.findViewById(R.id.profile_ig)
        val profileNameTv: TextView = view.findViewById(R.id.profile_name_tv)
        val profileTv: TextView = view.findViewById(R.id.profile_tv)

        descriptionTextView.text = walkDetail.description
        lengthTextView.text = "${walkDetail.distance} km"
        timeTextView.text = "${walkDetail.time} 분"

        Glide.with(requireContext())
            .load(walkDetail.createdBy.profileImageUrl)
            .into(profileIg)

        profileNameTv.text = walkDetail.createdBy.nickname
        profileTv.text = walkDetail.createdBy.breed ?: "알 수 없음"

        // 난이도에 따라 이미지 변경
        val difficultyResId = when (walkDetail.difficulty) {
            "LOW" -> R.drawable.ic_easy
            "HIGH" -> R.drawable.ic_difficulty
            else -> R.drawable.ic_normal
        }
        val difficultyImageView: ImageView = view.findViewById(R.id.difficulty_iv)
        difficultyImageView.setImageResource(difficultyResId)

        // 좌표 받아서 마커 및 경로 추가
        val startCoordinate = walkDetail.startCoordinates.first()
        val endCoordinate = walkDetail.endCoordinates.first()
        setInitialMarker(startCoordinate, endCoordinate)
    }

    private fun setInitialMarker(startCoordinate: Coordinate, endCoordinate: Coordinate) {
        val startMarker = Marker()
        startMarker.icon = OverlayImage.fromResource(R.drawable.ic_start_marker)
        startMarker.position = LatLng(startCoordinate.latitude, startCoordinate.longitude)
        startMarker.map = naverMap

        val endMarker = Marker()
        endMarker.icon = OverlayImage.fromResource(R.drawable.ic_end_marker)
        endMarker.position = LatLng(endCoordinate.latitude, endCoordinate.longitude)
        endMarker.map = naverMap

        val startLatLng = LatLng(startCoordinate.latitude, startCoordinate.longitude)
        val endLatLng = LatLng(endCoordinate.latitude, endCoordinate.longitude)
        val bounds = LatLngBounds(startLatLng, endLatLng)

        // bounds에 맞춰 카메라를 이동
        val cameraUpdate = CameraUpdate.fitBounds(bounds, 100) // 100은 padding 값
        naverMap?.moveCamera(cameraUpdate)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        parentFragmentManager.fragments
            .filterIsInstance<MapFragment>()
            .firstOrNull()?.let { mapFragment ->
                parentFragmentManager.beginTransaction()
                    .show(mapFragment)
                    .commit()
            }
    }
}
