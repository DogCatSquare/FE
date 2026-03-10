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

    fun submitList(items: List<GetAllPostResult>?) {
        hotPostList.clear()
        if (!items.isNullOrEmpty()) {
            hotPostList.addAll(items)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
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

    private fun extractYouTubeVideoId(url: String?): String? {
        if (url.isNullOrBlank()) return null

        val patterns = listOf(
            "[?&]v=([a-zA-Z0-9_-]{11})",
            "youtu\\.be/([a-zA-Z0-9_-]{11})",
            "youtube\\.com/shorts/([a-zA-Z0-9_-]{11})",
            "youtube\\.com/embed/([a-zA-Z0-9_-]{11})"
        )

        for (pattern in patterns) {
            val match = Regex(pattern, RegexOption.IGNORE_CASE).find(url)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1]
            }
        }
        return null
    }

    private fun youtubeThumbnail(url: String?): String? {
        val videoId = extractYouTubeVideoId(url) ?: return null
        return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
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

        private fun String?.orDash(): String = if (this.isNullOrBlank()) "—" else this
        private fun String?.nonBlankOrNull(): String? = this?.takeIf { it.isNotBlank() }

        fun bind(post: GetAllPostResult) {
            val safeTitle = post.title.orDash()
            val safeContent = post.content.orDash()
            val safeUsername = post.username.orDash()
            val safeBreed = post.animal_type.orDash()

            titleText.text = safeTitle
            username.text = safeUsername
            breed.text = safeBreed

            if (post.content.isNullOrBlank()) {
                contentPreview.isGone = true
                contentPreview.text = ""
            } else {
                contentPreview.isVisible = true
                contentPreview.text = safeContent
            }

            likeCountText.text = (post.likeCount ?: 0).toString()
            commentCountText.text = (post.commentCount ?: 0).toString()

            Glide.with(itemView.context)
                .load(post.profileImageURL.nonBlankOrNull())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(profile)

            Glide.with(itemView.context).clear(thumbnail)

            val videoUrl = post.videoURL.nonBlankOrNull()

            val thumbUrl =
                youtubeThumbnail(videoUrl)
                    ?: post.images?.firstOrNull().nonBlankOrNull()
                    ?: post.thumbnailURL.nonBlankOrNull()

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