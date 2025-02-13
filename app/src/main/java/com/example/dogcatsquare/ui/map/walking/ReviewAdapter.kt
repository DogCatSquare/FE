package com.example.dogcatsquare.ui.map.walking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.databinding.ItemMapwalingReviewBinding
import com.example.dogcatsquare.ui.map.walking.data.Review

class ReviewAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemMapwalingReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int = reviews.size

    inner class ReviewViewHolder(private val binding: ItemMapwalingReviewBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            // 프로필 이미지 설정
            Glide.with(binding.root.context)
                .load(review.profileImageUrl)
                .circleCrop()
                .into(binding.reviewImage)

            // 닉네임, 품종 설정
            binding.nameTitle.text = review.nickname
            binding.nameTv.text = review.breed ?: "미정"

            // 리뷰 내용 설정
            binding.reviewTv.text = review.reviewContent

            // 작성일 설정
            binding.dateTv.text = review.reviewDate

            // 리뷰 이미지 설정 (있으면)
            if (review.reviewImageUrl != null) {
                Glide.with(binding.root.context)
                    .load(review.reviewImageUrl)
                    .into(binding.reviewIv)
            }
        }
    }
}

