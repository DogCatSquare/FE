package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.GetAllPostResult

class PostAdapter(
    private val hotPostList: ArrayList<GetAllPostResult>
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(post: GetAllPostResult)
    }

    private var mItemClickListener: OnItemClickListener? = null
    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = hotPostList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            mItemClickListener?.onItemClick(item)
        }
    }

    override fun getItemCount(): Int = hotPostList.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.tvTitle)
        private val contentPreview: TextView = itemView.findViewById(R.id.tvContentPreview)
        private val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val likeCountText: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val commentCountText: TextView = itemView.findViewById(R.id.tvCommentCount)
        private val username: TextView = itemView.findViewById(R.id.tvNickname)
        private val breed: TextView = itemView.findViewById(R.id.tvDogBreed)
        private val profile: ImageView = itemView.findViewById(R.id.post_profile_iv)

        private fun String?.orDash(): String = if (this.isNullOrBlank()) "—" else this
        private fun String?.nonBlankOrNull(): String? = this?.takeIf { it.isNotBlank() }

        fun bind(post: GetAllPostResult) {
            // 텍스트 기본값 처리
            titleText.text = post.title.orDash()
            contentPreview.text = post.content.orDash()
            username.text = post.username.orDash()
            breed.text = post.animal_type.orDash()
            likeCountText.text = (post.likeCount ?: 0).toString()
            commentCountText.text = (post.commentCount ?: 0).toString()

            // 프로필 이미지 (없으면 placeholder)
            Glide.with(itemView.context)
                .load(post.profileImageURL.nonBlankOrNull())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(profile)

            // 썸네일: thumbnailURL → images[0] → 없으면 GONE
            val thumbUrl = post.thumbnailURL.nonBlankOrNull()
                ?: post.images?.firstOrNull().nonBlankOrNull()

            if (thumbUrl != null) {
                thumbnail.isVisible = true
                Glide.with(itemView.context)
                    .load(thumbUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(thumbnail)
            } else {
                thumbnail.setImageDrawable(null)
                thumbnail.isGone = true
            }
        }
    }
}