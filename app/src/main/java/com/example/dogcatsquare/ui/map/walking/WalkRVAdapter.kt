package com.example.dogcatsquare.ui.map.walking

import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.home.Event
import com.example.dogcatsquare.data.model.walk.Walk
import com.example.dogcatsquare.databinding.ItemMapwalkingBinding
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions

class WalkRVAdapter(private val onItemClick: (Int) -> Unit, private var walkList: ArrayList<Walk>) : RecyclerView.Adapter<WalkRVAdapter.ViewHolder>() {
    private var walks: List<Walk> = emptyList()

    inner class ViewHolder(private val binding: ItemMapwalkingBinding) : RecyclerView.ViewHolder(binding.root) {

        private fun dpToPx(dp: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                itemView.resources.displayMetrics
            ).toInt()
        }

        fun bind(walk: Walk) {
            binding.apply {
                // 1. 유저 정보 설정
                userName.text = walk.createdBy.nickname
                petType.text = walk.createdBy.breed
                Glide.with(userImg)
                    .load(walk.createdBy.profileImageUrl)
                    .fallback(R.drawable.ic_profile_img_default)
                    .into(userImg)

                // 2. 산책 정보 설정
                walkTime.text = "소요시간 ${walk.time}h"
                walkKm.text = "| ${String.format("%.1f", walk.distance)}km"
                walkText.text = walk.description

                // 3. [중요] 기존에 추가된 이미지 뷰들 삭제 (재사용 시 이미지 겹침 방지)
                imageContainer.removeAllViews()

                // 4. 이미지 리스트 처리 (여러 장을 가로로 추가)
                if (walk.walkImageUrl.isNotEmpty()) {
                    walk.walkImageUrl.forEach { url ->
                        // 각 이미지를 감쌀 CardView 생성 (모서리 12dp 라운딩)
                        val cardView = CardView(root.context).apply {
                            radius = dpToPx(12f).toFloat() // 여기서 dpToPx 사용
                            cardElevation = 0f
                            val params = LinearLayout.LayoutParams(dpToPx(120f), dpToPx(120f))
                            params.marginEnd = dpToPx(8f) // 이미지 간 간격 8dp
                            layoutParams = params
                        }

                        // 실제 사진이 들어갈 ImageView 생성
                        val imageView = ImageView(root.context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }

                        // Glide로 이미지 로드
                        Glide.with(imageView)
                            .load(url)
                            .placeholder(R.drawable.ic_place_img_default)
                            .into(imageView)

                        cardView.addView(imageView)
                        imageContainer.addView(cardView)
                    }
                } else {
                    // 이미지가 없을 경우 회색 박스 하나 노출
                    addDefaultPlaceholder()
                }

                // 5. 구글 지도 설정 및 줌 범위 조정
                mapView.onCreate(null)
                mapView.getMapAsync { googleMap ->
                    // 리사이클러뷰 스크롤 시 지도 터치와 충돌하지 않도록 모든 제스처 막기
                    googleMap.uiSettings.apply {
                        isMapToolbarEnabled = false
                        setAllGesturesEnabled(false)
                    }

                    // 🌟 뷰홀더가 재사용될 때 이전 데이터가 겹치지 않게 지도 초기화
                    googleMap.clear()

                    // 서버에서 받은 좌표 데이터가 있다면 선을 그리고 줌을 맞춥니다
                    if (!walk.coordinates.isNullOrEmpty()) {
                        val boundsBuilder = LatLngBounds.Builder()
                        val polylineOptions = PolylineOptions()
                            .width(10f)
                            .color(Color.parseColor("#FFB200"))

                        // 모든 좌표를 돌면서 선(Polyline)에 점을 추가하고 상자(Bounds) 크기를 늘립니다.
                        walk.coordinates.forEach { coord ->
                            // TODO: coord.latitude, coord.longitude는 실제 DTO 변수명에 맞게 수정하세요
                            val latLng = LatLng(coord.latitude, coord.longitude)
                            polylineOptions.add(latLng)
                            boundsBuilder.include(latLng)
                        }

                        // 지도에 주황색 선 그리기
                        googleMap.addPolyline(polylineOptions)

                        // 🌟 방법 A: 경로 전체가 한눈에 다 들어오게 '자동 줌' (추천!)
                        try {
                            val bounds = boundsBuilder.build()
                            val padding = dpToPx(30f) // 테두리 여백 30dp
                            googleMap.setOnMapLoadedCallback {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                            }
                        } catch (e: Exception) {
                            Log.e("WalkRVAdapter", "Bounds 렌더링 에러", e)
                        }

                        /* // 🌟 방법 B: 무조건 특정 배율(예: 15f)로 '고정 줌' 하고 싶다면 위 방법 A를 지우고 아래 코드를 쓰세요!
                        val startLocation = LatLng(walk.coordinates[0].latitude, walk.coordinates[0].longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15f))
                        */
                    }
                }

                // 6. 날짜 포맷팅
                try {
                    val dateStr = walk.createdAt.split(".")[0]
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
                    val date = inputFormat.parse(dateStr)
                    walkDate.text = date?.let { outputFormat.format(it) } ?: ""
                } catch (e: Exception) {
                    walkDate.text = ""
                }

                // 7. 난이도 아이콘 설정
                val profileDrawable = when (walk.difficulty.lowercase()) {
                    "low" -> R.drawable.ic_easy
                    "high" -> R.drawable.ic_difficulty
                    else -> R.drawable.ic_normal
                }
                profileIv.setImageResource(profileDrawable)

                // 8. 아이템 클릭 리스너
                root.setOnClickListener {
                    onItemClick(walk.walkId)
                }
            }
        }

        // 이미지가 없을 때 보여줄 회색 박스 생성 함수
        private fun addDefaultPlaceholder() {
            val cardView = CardView(itemView.context).apply {
                radius = dpToPx(12f).toFloat()
                cardElevation = 0f
                val params = LinearLayout.LayoutParams(dpToPx(120f), dpToPx(120f))
                params.marginEnd = dpToPx(8f)
                layoutParams = params
            }
            val iv = ImageView(itemView.context).apply {
                setImageResource(R.drawable.ic_place_img_default)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(Color.parseColor("#F2F2F2"))
            }
            cardView.addView(iv)
            binding.imageContainer.addView(cardView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMapwalkingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val walk = walkList[position]
        holder.bind(walk)
    }

    override fun getItemCount(): Int = walkList.size

    fun updateData(newWalks: ArrayList<Walk>) {
        walkList = newWalks
        notifyDataSetChanged()
    }
}