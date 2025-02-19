package com.example.dogcatsquare.ui.map.walking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.naver.maps.map.MapView
import kotlin.math.min


class WalkingReviewAdapter(
    private var items: MutableList<WalkMapReview>,
    private var maxItemCount : Int? = null,
    private val onItemClick: (WalkMapReview) -> Unit // 클릭 이벤트 처리 콜백 추가
) : RecyclerView.Adapter<WalkingReviewAdapter.WalkingReviewViewHolder>() {

    inner class WalkingReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImg: ImageView = itemView.findViewById(R.id.userImg)
        val userName: TextView = itemView.findViewById(R.id.userName)
        val petType: TextView = itemView.findViewById(R.id.petType)
        val walkTime: TextView = itemView.findViewById(R.id.walkTime)
        val walkKm: TextView = itemView.findViewById(R.id.walkKm)
        val walkImg: ImageView = itemView.findViewById(R.id.walkImg)
        val walkText: TextView = itemView.findViewById(R.id.walkText)
        val profileIv: ImageView = itemView.findViewById(R.id.profile_iv)
        val walkDate: TextView = itemView.findViewById(R.id.walkDate)
        val mapContainer: ViewGroup = itemView.findViewById(R.id.mapView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkingReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mapwalking, parent, false)
        return WalkingReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalkingReviewViewHolder, position: Int) {
        val item = items[position]

        // 데이터 설정
        holder.userName.text = item.userName
        holder.petType.text = item.petType
        holder.walkTime.text = item.walkTime
        holder.walkKm.text = item.walkKm
        holder.walkText.text = item.walkText
        holder.walkDate.text = item.walkDate

        // 이미지 로딩 (Glide 사용)
        Glide.with(holder.itemView.context)
            .load(item.userImgUrl)
            .placeholder(R.drawable.ic_profile_default) // 로딩 중 이미지
            .error(R.drawable.img_walk_default) // 오류 발생 시 이미지
            .into(holder.userImg)

        Glide.with(holder.itemView.context)
            .load(item.walkImgUrl)
            .placeholder(R.drawable.ic_profile_default) // 기본 이미지
            .error(R.drawable.img_walk_default) // 오류 시 이미지
            .into(holder.walkImg)

        Glide.with(holder.itemView.context)
            .load(item.profileImgUrl)
            .placeholder(R.drawable.ic_normal) // 기본 이미지
            .error(R.drawable.ic_normal) // 오류 시 이미지
            .into(holder.profileIv)

        // MapView 처리 (이전 MapView 제거 후 새로 추가)
        holder.mapContainer.removeAllViews() // 기존 맵 제거
        val mapView = MapView(holder.itemView.context)
        holder.mapContainer.addView(mapView)
        mapView.onCreate(null) // 라이프사이클 초기화
        mapView.getMapAsync { naverMap ->
            // 네이버 지도 설정 가능
        }

        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener {
            onItemClick(item)  // 아이템 클릭 시 전달할 콜백 호출
        }
    }

    override fun getItemCount(): Int {
        return maxItemCount?.let { min(it, items.size) } ?: items.size
    }

    // 새로운 리뷰 업데이트 함수
    fun updateReviews(newItems: List<WalkMapReview>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
