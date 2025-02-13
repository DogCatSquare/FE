package com.example.dogcatsquare.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.data.model.mypage.MyReview
import com.example.dogcatsquare.databinding.ItemMyReviewBinding

class MyReviewRVAdapter(private val myReviewList: ArrayList<MyReview>) : RecyclerView.Adapter<MyReviewRVAdapter.MyReviewAdapterViewHolder>() {
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
        fun bind(myReview: MyReview) {
        }
    }
}