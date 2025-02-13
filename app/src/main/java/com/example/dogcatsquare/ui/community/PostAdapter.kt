package com.example.dogcatsquare.ui.community

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.Post

class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.tvTitle)
        private val contentPreview: TextView = itemView.findViewById(R.id.tvContentPreview)
        private val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val likeCountText: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val commentCountText: TextView = itemView.findViewById(R.id.tvCommentCount)

        fun bind(post: Post) {
            titleText.text = post.title ?: "제목 없음"
            contentPreview.text = post.content ?: "내용 없음"
            likeCountText.text = post.likeCount.toString()
            commentCountText.text = post.commentCount.toString()

            if (!post.thumbnailUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(post.thumbnailUrl)
                    .placeholder(R.drawable.ic_sample_thumbnail)
                    .into(thumbnail)
            } else {
                thumbnail.setImageResource(R.drawable.ic_sample_thumbnail)
            }

            itemView.setOnClickListener {
                Log.d("PostAdapter", "Clicked post: id=${post.id}, title=${post.title}")
                val context = itemView.context
                val intent = Intent(context, PostDetailActivity::class.java).apply {
                    putExtra("postId", post.id)
                    putExtra("board", post.board)
                    putExtra("username", post.username)
                    putExtra("title", post.title ?: "제목 없음")
                    putExtra("content", post.content ?: "내용 없음")
                    putExtra("videoUrl", post.videoUrl)
                    putExtra("thumbnailUrl", post.thumbnailUrl)
                    putExtra("profileImageUrl", post.profileImageUrl)
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
