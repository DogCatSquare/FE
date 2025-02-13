package com.example.dogcatsquare.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.mypage.MyReview
import com.example.dogcatsquare.data.model.mypage.ReviewContent
import com.example.dogcatsquare.databinding.ItemMyReviewBinding

class MyReviewRVAdapter(private val myReviewList: ArrayList<ReviewContent>) : RecyclerView.Adapter<MyReviewRVAdapter.MyReviewAdapterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewAdapterViewHolder {
        val binding = ItemMyReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyReviewAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyReviewAdapterViewHolder, position: Int) {
        val myReview = myReviewList[position]
        holder.bind(myReview)
    }

    override fun getItemCount(): Int = myReviewList.size

    inner class MyReviewAdapterViewHolder(val binding: ItemMyReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(myReview: ReviewContent) {
//            binding.placeName.text = myReview.
            binding.reviewText.text = myReview.content
            binding.reviewDate.text = myReview.createdAt
            Glide.with(itemView.context)
                .load(myReview.imageUrls)
                .placeholder(R.drawable.ic_profile_default)
                .into(binding.reviewImg)

            // 설정 클릭(수정, 삭제)
//            binding.settingIv.setOnClickListener {
//
//            }
        }
    }
}