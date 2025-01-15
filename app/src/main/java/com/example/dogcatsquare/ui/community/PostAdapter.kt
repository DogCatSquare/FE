package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R

class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val content: TextView = itemView.findViewById(R.id.tvContentPreview)
        val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val likeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val commentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        // 제목 설정 (입력된 데이터가 없으면 기본값 유지)
        holder.title.text = post.title ?: "제목을 입력해주세요"

        // 내용 설정 (입력된 데이터가 없으면 기본값 유지)
        holder.content.text = post.content ?: "내용을 입력해주세요"

        holder.likeCount.text = post.likeCount.toString()
        holder.commentCount.text = post.commentCount.toString()

        // 썸네일 설정 (입력된 이미지가 없으면 기본 회색 배경 유지)
        if (post.thumbnail == null) {
            holder.thumbnail.setBackgroundColor(Color.parseColor("#E0E0E0")) // 회색
            holder.thumbnail.setImageDrawable(null)
        } else {
            holder.thumbnail.setImageBitmap(post.thumbnail) // 입력된 이미지
        }
    }

    override fun getItemCount(): Int = postList.size
}
