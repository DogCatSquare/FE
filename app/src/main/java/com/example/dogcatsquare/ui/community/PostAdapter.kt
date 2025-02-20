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
import com.bumptech.glide.request.RequestOptions
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.GetAllPostResult
import com.example.dogcatsquare.data.community.Post
import com.example.dogcatsquare.ui.home.HomeHotPostRVAdapter.OnItemClickListener

class PostAdapter(private val hotPostList: ArrayList<GetAllPostResult>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(post: com.example.dogcatsquare.data.model.post.Post)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.tvTitle)
        private val contentPreview: TextView = itemView.findViewById(R.id.tvContentPreview)
        private val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val likeCountText: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val commentCountText: TextView = itemView.findViewById(R.id.tvCommentCount)
        private val username: TextView = itemView.findViewById(R.id.tvUsername)
        private val breed: TextView = itemView.findViewById(R.id.tvBreed)
        private val profile: ImageView = itemView.findViewById(R.id.post_profile_iv)

        fun bind(post: GetAllPostResult) {
            titleText.text = post.title ?: "제목 없음"
            contentPreview.text = post.content ?: "내용 없음"
            likeCountText.text = post.likeCount.toString()
            commentCountText.text = post.commentCount.toString()
            username.text = post.username
//            breed.text = post.

            Glide.with(itemView.context)
                .load(post.profileImageURL)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(profile)

            if (!post.thumbnailURL.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(post.thumbnailURL)
                    .placeholder(R.drawable.ic_profile_default)
                    .into(thumbnail)
            } else {
                thumbnail.setImageResource(R.drawable.ic_sample_thumbnail)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(hotPostList[position])
    }

    override fun getItemCount(): Int = hotPostList.size
}