package com.example.dogcatsquare.ui.map.walking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ItemMapwalingReviewBinding
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import com.example.dogcatsquare.ui.map.walking.data.Review

class ReviewAdapter(private var reviews: List<WalkReview>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    // 클릭 리스너 변수 선언
    private var onItemClickListener: ((WalkReview) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mapwaling_review, parent, false)
        return ReviewViewHolder(view)
    }

    // 클릭 리스너 설정 메소드
    fun setOnItemClickListener(listener: (WalkReview) -> Unit) {
        onItemClickListener = listener
    }

    fun updateData(newReviews: List<WalkReview>) {
        reviews = newReviews
        notifyDataSetChanged()  // 변경 사항을 RecyclerView에 알림
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // 텍스트 뷰에 데이터 바인딩
        holder.nicknameTextView.text = review.createdBy.nickname
        holder.breedTextView.text = review.createdBy.breed
        holder.contentTextView.text = review.content

        // 프로필 이미지 로드
        Glide.with(holder.itemView.context)
            .load(review.createdBy.profileImageUrl)
            .into(holder.profileImageView)

        // 리뷰 이미지가 있으면 로드
        if (review.walkReviewImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(review.walkReviewImageUrl[0]) // 첫 번째 이미지만 예시로 사용
                .into(holder.reviewImageView)
        }

        // 아이템 클릭 시 리스너 호출
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(review)
        }
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.review_image)
        val nicknameTextView: TextView = itemView.findViewById(R.id.name_title)
        val breedTextView: TextView = itemView.findViewById(R.id.name_tv)
        val contentTextView: TextView = itemView.findViewById(R.id.review_tv)
        val reviewImageView: ImageView = itemView.findViewById(R.id.review_image)
    }
}
