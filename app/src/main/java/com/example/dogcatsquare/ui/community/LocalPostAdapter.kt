package com.example.dogcatsquare.ui.community

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R

class LocalPostAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<LocalPostAdapter.LocalPostViewHolder>() {

    inner class LocalPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val location: TextView = itemView.findViewById(R.id.tvLocation)
        val content: TextView = itemView.findViewById(R.id.tvContent)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val likeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val commentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        val profile: ImageView = itemView.findViewById(R.id.ivProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_local_post, parent, false)
        return LocalPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocalPostViewHolder, position: Int) {
        val post = posts[position]
        holder.username.text = post.username
        holder.location.text = post.location
        holder.content.text = post.content ?: "내용을 입력해주세요"
        holder.date.text = post.date
        holder.likeCount.text = post.likeCount.toString()
        holder.commentCount.text = post.commentCount.toString()

        holder.profile.setImageResource(R.drawable.ic_profile_placeholder)

        // 썸네일 설정
        if (post.thumbnail != null) {
            holder.profile.setImageBitmap(post.thumbnail)
        } else {
            holder.profile.setImageResource(R.drawable.ic_placeholder) // 회색 배경 이미지
        }
    }

    override fun getItemCount(): Int = posts.size
}
