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

class ReviewAdapter(private val reviews: List<WalkReview>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mapwaling_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        holder.nicknameTextView.text = review.createdBy.nickname
        holder.breedTextView.text = review.createdBy.breed
        holder.contentTextView.text = review.content

        Glide.with(holder.itemView.context)
            .load(review.createdBy.profileImageUrl)
            .into(holder.profileImageView)

        if (review.walkReviewImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(review.walkReviewImageUrl[0]) // 첫 번째 이미지만 예시로 사용
                .into(holder.reviewImageView)
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
