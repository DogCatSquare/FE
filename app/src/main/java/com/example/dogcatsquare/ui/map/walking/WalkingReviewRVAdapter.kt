package com.example.dogcatsquare.ui.map.walking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.map.MapReview
import com.example.dogcatsquare.databinding.ItemMapReviewBinding

class WalkingReviewRVAdapter(private val reviewList: ArrayList<MapReview>) :
    RecyclerView.Adapter<WalkingReviewRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMapReviewBinding = ItemMapReviewBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reviewList[position])
    }

    override fun getItemCount(): Int = reviewList.size

    inner class ViewHolder(private val binding: ItemMapReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: MapReview) {
            // 프로필 이미지 설정
            Glide.with(itemView.context)
                .load(review.userImageUrl)
                .fallback(R.drawable.ic_profile_img_default)
                .error(R.drawable.ic_profile_img_default)
                .into(binding.reviewProfileImg)

            // 텍스트 정보 설정
            binding.reviewName.text = review.nickname ?: "알 수 없음"
            binding.petType.text = review.breed ?: ""
            binding.reviewText.text = review.content ?: ""

            // 날짜 포맷 변환 (YYYY-MM-DD -> YYYY.MM.DD)
            binding.reviewDate.text = review.createdAt?.split("T")?.get(0)?.replace("-", ".") ?: ""

            // 리뷰 이미지 설정
            val imageUrl = review.placeReviewImageUrl?.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .fallback(R.drawable.ic_place_img_default)
                    .error(R.drawable.ic_place_img_default)
                    .into(binding.reviewImg)
            } else {
                binding.reviewImg.setImageResource(R.drawable.ic_place_img_default)
            }

            // 더보기 버튼(etcButton) 클릭 리스너
            binding.etcButton.setOnClickListener { view ->
                showPopupMenu(view)
            }
        }

        private fun showPopupMenu(view: View) {
            // 커스텀 팝업 레이아웃 inflate
            val popupView = LayoutInflater.from(view.context)
                .inflate(R.layout.popup_menu_custom, null)
        }
    }
}
