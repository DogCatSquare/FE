package com.example.dogcatsquare.ui.map.walking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R

data class WalkingAddress(val reviewText: String)

class WalkingReviewAdapter(private val reviewList: List<WalkingAddress>) :
    RecyclerView.Adapter<WalkingReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewTextView: TextView = itemView.findViewById(R.id.address_et)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mapwalking_address, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.reviewTextView.text = reviewList[position].reviewText
    }

    override fun getItemCount(): Int = reviewList.size
}

