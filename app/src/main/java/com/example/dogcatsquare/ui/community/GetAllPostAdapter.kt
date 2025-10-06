package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.GetAllPostResult
import com.example.dogcatsquare.util.DateFmt

class GetAllPostAdapter(
    private val allPostList: ArrayList<GetAllPostResult>
) : RecyclerView.Adapter<GetAllPostAdapter.PostViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(post: GetAllPostResult)
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
        private val username: TextView = itemView.findViewById(R.id.tvNickname)
        private val breed: TextView = itemView.findViewById(R.id.tvDogBreed)
        private val profile: ImageView = itemView.findViewById(R.id.post_profile_iv)
        private val dateText: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(post: GetAllPostResult) {
            // 텍스트
            titleText.text = post.title ?: "제목 없음"
            contentPreview.text = post.content ?: "내용 없음"
            username.text = post.username ?: ""
            breed.text = post.animal_type ?: ""

            likeCountText.text = (post.likeCount ?: 0).toString()
            commentCountText.text = (post.commentCount ?: 0).toString()

            dateText.text = DateFmt.format(post.createdAt)

            // 프로필 이미지
            Glide.with(itemView.context)
                .load(post.profileImageURL)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(profile)

            // 썸네일 이미지
            if (!post.thumbnailURL.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(post.thumbnailURL.first())
                    .placeholder(R.drawable.ic_profile_default)
                    .error(R.drawable.ic_profile_default)
                    .into(thumbnail)
            } else {
                thumbnail.setImageResource(R.drawable.ic_profile_default)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = allPostList[position]
        holder.bind(post)
        holder.itemView.setOnClickListener { mItemClickListener.onItemClick(post) }
    }

    override fun getItemCount(): Int = allPostList.size
}