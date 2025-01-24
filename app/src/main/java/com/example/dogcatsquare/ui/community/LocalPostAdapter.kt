package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R

class LocalPostAdapter(private val localPosts: List<LocalPost>) :
    RecyclerView.Adapter<LocalPostAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val dogBreed: TextView = itemView.findViewById(R.id.tvDogBreed)
        val content: TextView = itemView.findViewById(R.id.tvContent)
        val image1: ImageView = itemView.findViewById(R.id.ivPostImage1)
        val image2: ImageView = itemView.findViewById(R.id.ivPostImage2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_compact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = localPosts[position]

        holder.username.text = post.username
        holder.dogBreed.text = post.dogbreed
        holder.content.text = post.content

        // 이미지 설정
        if (post.images.isNotEmpty()) {
            holder.image1.setImageResource(post.images[0])
            holder.image1.visibility = View.VISIBLE

            if (post.images.size > 1) {
                holder.image2.setImageResource(post.images[1])
                holder.image2.visibility = View.VISIBLE
            } else {
                holder.image2.visibility = View.GONE
            }
        } else {
            holder.image1.visibility = View.GONE
            holder.image2.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = localPosts.size
}
