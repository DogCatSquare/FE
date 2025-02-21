package com.example.dogcatsquare.ui.map.walking

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitClient.retrofit
import com.example.dogcatsquare.data.api.PlacesApiService
import com.example.dogcatsquare.ui.map.walking.data.Address
import com.example.dogcatsquare.ui.map.walking.data.Response.Coordinate
import com.example.dogcatsquare.ui.map.walking.data.Response.Walk
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetail
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkDetailState
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkDetailViewModel
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await

class WalkingStartViewFragment : Fragment(), OnMapReadyCallback {

    private lateinit var reviewAdapter: ReviewAdapter
    private val walkDetailViewModel: WalkDetailViewModel by viewModels()
    private val walkReviewViewModel: WalkReviewViewModel by viewModels()
    private var walkId: String? = null
    private var naverMap: NaverMap? = null
    private var userPolyline = PolylineOverlay()
    private val coords = mutableListOf<LatLng>()
    private var walk: Walk? = null
    private var lat : Double? = null
    private var lon : Double? = null

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mapwalking_startview, container, false)
        walkId = arguments?.getInt("walkId")?.toString()
        Log.d("WalkingStartViewFragment", "Received walkId: $walkId")

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
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as com.naver.maps.map.MapFragment?
            ?: com.naver.maps.map.MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.map_fragment, it).commit()
            }

        mapFragment?.getMapAsync { map ->
            naverMap = map
            Log.d("WalkingStartView", "NaverMap initialized")

            // 마커 추가 및 카메라 이동
            walkDetailViewModel.walkDetailState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is WalkDetailState.Success -> {
                        Log.d("WalkingStartView", "Walk Detail Success: ${state.walkDetail}")
                        val walkDetail = state.walkDetail
                        if (walkDetail != null) {
                            (activity as? AppCompatActivity)?.supportActionBar?.title = walkDetail.title
                            updateUI(view, walkDetail)

                            // first() 대신 firstOrNull() 사용
                            val startCoordinate = walkDetail.startCoordinates.firstOrNull()
                            val endCoordinate = walkDetail.endCoordinates.firstOrNull()

                            if (startCoordinate != null && endCoordinate != null) {
                                lat = startCoordinate.latitude
                                lon = startCoordinate.longitude

                                fetchAddress(lat!!, lon!!)
                            }

                            // 좌표가 null이 아닌 경우에만 마커 설정
                            if (startCoordinate != null && endCoordinate != null) {
                                setInitialMarker(startCoordinate, endCoordinate)
                            } else {
                                Log.e("WalkingStartView", "좌표가 유효하지 않습니다: 시작 좌표나 끝 좌표가 없음.")
                            }
                        }
                    }
                    is WalkDetailState.Error -> {
                        Log.e("WalkingStartView", "API 호출 실패: ${state.message}")
                    }
                    WalkDetailState.Loading -> TODO()
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
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WalkingReviewAllFragment())
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

        // 리스트가 비어있지 않은지 확인 후 첫 번째 좌표 가져오기
        val startCoordinate = walkDetail.startCoordinates.firstOrNull()
        val endCoordinate = walkDetail.endCoordinates.firstOrNull()

        if (startCoordinate != null && endCoordinate != null) {
            setInitialMarker(startCoordinate, endCoordinate)
        } else {
            Log.e("WalkingStartView", "좌표가 유효하지 않습니다: 시작 좌표나 끝 좌표가 없음.")
        }
    }


    private fun setInitialMarker(startCoordinate: Coordinate, endCoordinate: Coordinate) {
        if (naverMap == null) {
            Log.e("WalkingStartView", "NaverMap is not initialized.")
            return
        }

        if (startCoordinate != null && endCoordinate != null) {
            // Start Marker
            val startMarker = Marker().apply {
                icon = OverlayImage.fromResource(R.drawable.ic_start_marker)
                position = LatLng(startCoordinate.latitude, startCoordinate.longitude)
                map = naverMap
            }

            // End Marker
            val endMarker = Marker().apply {
                icon = OverlayImage.fromResource(R.drawable.ic_end_marker)
                position = LatLng(endCoordinate.latitude, endCoordinate.longitude)
                map = naverMap
            }

            coords.add(LatLng(startCoordinate.latitude, startCoordinate.longitude))
            coords.add(LatLng(endCoordinate.latitude, endCoordinate.longitude))

            userPolyline.coords = coords
            userPolyline.color = Color.parseColor("#FFB200")
            userPolyline.width = 10
            userPolyline.map = naverMap

            // bounds에 맞춰 카메라를 이동
            val bounds = LatLngBounds(LatLng(startCoordinate.latitude, startCoordinate.longitude), LatLng(endCoordinate.latitude, endCoordinate.longitude))
            val cameraUpdate = CameraUpdate.fitBounds(bounds, 200)
            naverMap?.moveCamera(cameraUpdate)
        } else {
            Log.e("WalkingStartView", "Invalid coordinates: start or end coordinate is null.")
        }
    }

    private fun fetchAddress(latitude: Double, longitude: Double) {
        apiService.getAddress(latitude, longitude).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val address = response.body()?.string() ?: "주소를 찾을 수 없습니다."
                    view?.let { fragmentView ->
                        val start_tv = fragmentView.findViewById<TextView>(R.id.start_tv)
                        val end_tv = fragmentView.findViewById<TextView>(R.id.end_tv)

                        start_tv.text = address
                        end_tv.text = address
                    }
                } else {
                    Log.e("WalkingStartView", "주소 요청 실패: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("WalkingStartView", "주소 요청 중 오류 발생: ${t.message}")
            }
        })
    }





    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        userPolyline.map = naverMap
    }
}
