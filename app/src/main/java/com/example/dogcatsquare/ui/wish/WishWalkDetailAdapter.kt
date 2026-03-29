package com.example.dogcatsquare.ui.wish

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.data.api.WishRetrofitObj
import com.example.dogcatsquare.data.model.wish.DeleteWalkWishResponse
import com.example.dogcatsquare.data.model.wish.MyWishWalkContent
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ItemWishWalkDetailBinding
import com.example.dogcatsquare.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.MarkerOptions
import android.util.TypedValue

class WishWalkDetailAdapter(
    private val token: String?,
    private val context: Context,
    private val onWalkRemoved: (MyWishWalkContent) -> Unit
) : RecyclerView.Adapter<WishWalkDetailAdapter.DetailViewHolder>() {
    private var details = mutableListOf<MyWishWalkContent>()

    inner class DetailViewHolder(private val binding: ItemWishWalkDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MyWishWalkContent, position: Int) {
            binding.apply {
                // View 태그 설정하여 재사용 뷰 불일치 방지
                root.tag = item.walkId
                
                // 기존 데이터 혹은 기본 데이터 세팅
                tvWalkHeaderText.text = item.title
                walkTimeDistance.text = "소요시간 ${item.time}분 · ${item.distance}km"
                
                // 이미지 세팅 (walkImageUrl 기반)
                val imageViews = listOf(walkImg2, walkImg3)
                val cardViews = listOf(
                    walkImg2.parent as androidx.cardview.widget.CardView,
                    walkImg3.parent as androidx.cardview.widget.CardView
                )
                cardViews.forEach { it.visibility = android.view.View.GONE }
                if (!item.walkImageUrl.isNullOrEmpty()) {
                    item.walkImageUrl.take(2).forEachIndexed { index, url ->
                        cardViews[index].visibility = android.view.View.VISIBLE
                        Glide.with(context).load(url).centerCrop().into(imageViews[index])
                    }
                }
                
                // 위시 아이콘 처리
                ivWishDetail.setImageResource(R.drawable.ic_wish_check)
                ivWishDetail.setOnClickListener {
                    if (token == null) return@setOnClickListener
                    RetrofitObj.getRetrofit(context).create(WishRetrofitObj::class.java)
                        .deleteWalkWish("Bearer $token", item.walkId.toInt()).enqueue(object: Callback<DeleteWalkWishResponse> {
                            override fun onResponse(call: Call<DeleteWalkWishResponse>, response: Response<DeleteWalkWishResponse>) {
                                if (response.isSuccessful && response.body()?.isSuccess == true) {
                                    Toast.makeText(context, "위시리스트에서 제외되었습니다.", Toast.LENGTH_SHORT).show()
                                    ivWishDetail.setImageResource(R.drawable.ic_wish)
                                    onWalkRemoved(item)
                                } else {
                                    Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<DeleteWalkWishResponse>, t: Throwable) {
                                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        })
                }

                // --------- 동적 데이터 조회를 위한 비동기 처리 --------- //
                // 로딩 중 표시 초기화
                userName.text = "로딩중..."
                petType.text = ""
                walkText.text = ""
                walkDate.text = ""
                userImg.setImageResource(R.drawable.ic_profile_placeholder) // 또는 기본 이미지
                tag1.visibility = View.GONE
                tag2.visibility = View.GONE
                tag3.visibility = View.GONE

                com.example.dogcatsquare.data.network.RetrofitClient.walkingApiService
                    .getWalkDetail(item.walkId.toInt())
                    .enqueue(object: Callback<com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse> {
                        override fun onResponse(
                            call: Call<com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse>,
                            response: Response<com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse>
                        ) {
                            if (response.isSuccessful && root.tag == item.walkId) {
                                val detail = response.body()?.result
                                if (detail != null) {
                                    userName.text = detail.createdBy.nickname
                                    petType.text = detail.createdBy.breed ?: ""
                                    walkText.text = detail.description
                                    
                                    // 기본 날짜 처리 (YYYY-MM-DD 만 추출 등)
                                    walkDate.text = detail.createdAt.take(10)

                                    if (!detail.createdBy.profileImageUrl.isNullOrEmpty()) {
                                        Glide.with(context)
                                            .load(detail.createdBy.profileImageUrl)
                                            .circleCrop()
                                            .into(userImg)
                                    }

                                    // 태그 추가 로직
                                    val tags = mutableListOf<String>()
                                    when (detail.difficulty) {
                                        "LOW" -> tags.add("난이도 하")
                                        "HIGH" -> tags.add("난이도 상")
                                        else -> tags.add("난이도 중")
                                    }
                                    
                                    detail.special.forEach {
                                        when (it.type) {
                                            "WASTEBASKET" -> tags.add("쓰레기통")
                                            "WATER" -> tags.add("음수대")
                                            "TOILET" -> tags.add("화장실")
                                            "PARKING" -> tags.add("주차장")
                                            "STAIRS" -> tags.add("계단")
                                        }
                                    }

                                    val tagViews = listOf(tag1, tag2, tag3)
                                    tags.take(3).forEachIndexed { index, text ->
                                        tagViews[index].text = text
                                        tagViews[index].visibility = View.VISIBLE
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse>, t: Throwable) {
                            if (root.tag == item.walkId) {
                                userName.text = "조회 실패"
                                walkText.text = "상세 정보를 불러오지 못했습니다."
                            }
                        }
                    })

                // --------- 지도(MapView) 표시 로직 --------- //
                mapView.onCreate(null)
                mapView.onResume() // liteMode에서는 onResume 호출로 즉시 렌더링 시작

                mapView.getMapAsync { googleMap ->
                    googleMap.uiSettings.apply {
                        isMapToolbarEnabled = false
                        setAllGesturesEnabled(false)
                    }
                    googleMap.clear()

                    val walkService = com.example.dogcatsquare.data.network.RetrofitClient.walkingApiService
                    walkService.getWalkDetail(item.walkId.toInt()).enqueue(object: Callback<com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse> {
                        override fun onResponse(call: Call<com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse>, response: Response<com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse>) {
                            if (response.isSuccessful && root.tag == item.walkId) {
                                val detail = response.body()?.result
                                if (detail != null) {
                                    val startCoords = detail.startCoordinates
                                    val endCoords = detail.endCoordinates
                                    
                                    val boundsBuilder = LatLngBounds.Builder()
                                    val polylineOptions = PolylineOptions().width(10f).color(Color.parseColor("#FFB200"))
                                    var hasPoints = false

                                    // 임시로 startCoordinates와 endCoordinates를 모두 이어그리는 로직 적용
                                    val allCoords = mutableListOf<com.example.dogcatsquare.ui.map.walking.data.Response.Coordinate>()
                                    startCoords?.let { allCoords.addAll(it) }
                                    endCoords?.let { allCoords.addAll(it) }

                                    // (0.0, 0.0)처럼 백엔드에서 잘못 넘어온 더미/빈 좌표 필터링
                                    val validCoords = allCoords.filter { it.latitude != 0.0 || it.longitude != 0.0 }

                                    validCoords.forEach { coord ->
                                        val latLng = LatLng(coord.latitude, coord.longitude)
                                        polylineOptions.add(latLng)
                                        boundsBuilder.include(latLng)
                                        hasPoints = true
                                    }

                                    if (hasPoints) {
                                        googleMap.addPolyline(polylineOptions)

                                        // 시작 마커 (유효한 첫 좌표)
                                        val first = validCoords.first()
                                        val startLatLng = LatLng(first.latitude, first.longitude)
                                        getMarkerIcon(context, R.drawable.ic_start_marker)?.let { icon ->
                                            googleMap.addMarker(MarkerOptions().position(startLatLng).icon(icon))
                                        }

                                        // 종료 마커 (유효한 마지막 좌표)
                                        val last = validCoords.last()
                                        val endLatLng = LatLng(last.latitude, last.longitude)
                                        getMarkerIcon(context, R.drawable.ic_end_marker)?.let { icon ->
                                            googleMap.addMarker(MarkerOptions().position(endLatLng).icon(icon))
                                        }

                                        try {
                                            val bounds = boundsBuilder.build()
                                            googleMap.setOnMapLoadedCallback {
                                                // start와 end가 같거나 아주 가까운 경우 Bounds 확장이 어려우므로,
                                                // 14.5f 정도의 고정 줌으로 예쁘게 포커싱합니다.
                                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 14.5f))
                                            }
                                        } catch (e: Exception) {
                                            // 좌표 범위 오류 무시
                                        }
                                    }
                                }
                            }
                        }
                        override fun onFailure(call: Call<com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse>, t: Throwable) {}
                    })
                }
            }
        }

        private fun getMarkerIcon(context: Context, resId: Int): BitmapDescriptor? {
            val vectorDrawable = ContextCompat.getDrawable(context, resId) ?: return null
            vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
            val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemWishWalkDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(details[position], position)
    }

    override fun getItemCount() = details.size

    fun submitList(newDetails: List<MyWishWalkContent>) {
        details.clear()
        details.addAll(newDetails)
        notifyDataSetChanged()
    }
}