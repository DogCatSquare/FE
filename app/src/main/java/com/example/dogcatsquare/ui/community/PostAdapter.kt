package com.example.dogcatsquare.ui.community

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R

class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val content: TextView = itemView.findViewById(R.id.tvContentPreview)
        private val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val likeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val commentCount: TextView = itemView.findViewById(R.id.tvCommentCount)

        fun bind(post: Post) {
            title.text = post.title ?: "제목 없음"
            content.text = post.content ?: "내용 없음"
            likeCount.text = post.likeCount.toString()
            commentCount.text = post.commentCount.toString()
            thumbnail.setImageResource(post.thumbnailResId ?: R.drawable.ic_sample_thumbnail)

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, PostDetailActivity::class.java).apply {
                    putExtra("username", post.username ?: "익명 사용자")
                    putExtra("title", post.title ?: "제목 없음")
                    putExtra("content", post.content ?: "내용 없음")
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int = postList.size
}
