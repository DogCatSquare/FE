package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.PostListItem
import com.example.dogcatsquare.util.DateFmt

class GetAllPostAdapter :
    ListAdapter<PostListItem, GetAllPostAdapter.PostViewHolder>(DiffCallback) {

    interface OnItemClickListener {
        fun onItemClick(post: PostListItem)
    }

    private var mItemClickListener: OnItemClickListener? = null
    fun setMyItemClickListener(listener: OnItemClickListener) {
        mItemClickListener = listener
    }

    private var lastClickTime = 0L
    private val debounceInterval = 500L

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
        private val titleText: TextView = itemView.findViewById(R.id.tvTipTitle)
        private val contentPreview: TextView = itemView.findViewById(R.id.tvTipContent)
        private val thumbnail: ImageView = itemView.findViewById(R.id.ivTipThumbnail)
        private val likeCountText: TextView = itemView.findViewById(R.id.tvLikeCount)
        private val commentCountText: TextView = itemView.findViewById(R.id.tvCommentCount)
        private val username: TextView = itemView.findViewById(R.id.tvUserNickname)
        private val breed: TextView = itemView.findViewById(R.id.tvDogBreed)
        private val profile: ImageView = itemView.findViewById(R.id.ivUserProfile)
        private val dateText: TextView = itemView.findViewById(R.id.tvPostDate)

        fun bind(post: PostListItem) {
            titleText.text = post.title
            contentPreview.text = post.content
            username.text = post.username
            breed.text = post.animalType ?: ""
            likeCountText.text = post.likeCount.toString()
            commentCountText.text = post.commentCount.toString()
            dateText.text = DateFmt.format(post.createdAt)

            Glide.with(itemView.context).clear(profile)
            if (!post.profileImageUrl.isNullOrBlank()) {
                profile.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(post.profileImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profile)
            } else {
                profile.visibility = View.VISIBLE
                profile.setImageResource(R.drawable.ic_profile_img_default)
            }

            Glide.with(itemView.context).clear(thumbnail)

            val videoUrl = post.videoUrl?.takeIf { it.isNotBlank() }

            android.util.Log.d(
                "YT_DEBUG",
                "postId=${post.id}, videoUrl=${post.videoUrl}, extractedThumb=${youtubeThumbnail(videoUrl)}"
            )

            val finalThumb =
                youtubeThumbnail(videoUrl)
                    ?: post.images?.firstOrNull()?.takeIf { it.isNotBlank() }
                    ?: post.thumbnailUrl?.takeIf { it.isNotBlank() && !it.endsWith(".mp4", true) }

            android.util.Log.d(
                "THUMB_FINAL",
                "postId=${post.id}, finalThumb=$finalThumb"
            )

            when {
                finalThumb != null -> {
                    thumbnail.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(finalThumb)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .centerCrop()
                        .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                            override fun onLoadFailed(
                                e: com.bumptech.glide.load.engine.GlideException?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                android.util.Log.e("GLIDE_FAIL", "postId=${post.id}, model=$model", e)
                                return false
                            }

                            override fun onResourceReady(
                                resource: android.graphics.drawable.Drawable?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                                dataSource: com.bumptech.glide.load.DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                android.util.Log.d("GLIDE_OK", "postId=${post.id}, model=$model")
                                return false
                            }
                        })
                        .into(thumbnail)
                }

                videoUrl != null &&
                        (videoUrl.endsWith(".mp4", true) || videoUrl.contains(".mp4", true)) -> {
                    thumbnail.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .asBitmap()
                        .load(videoUrl)
                        .apply(
                            RequestOptions()
                                .frame(1_000_000)
                                .centerCrop()
                        )
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(thumbnail)
                }

                else -> {
                    thumbnail.visibility = View.GONE
                    thumbnail.setImageDrawable(null)
                }
            }

            itemView.setOnClickListener {
                val now = System.currentTimeMillis()
                if (now - lastClickTime < debounceInterval) return@setOnClickListener
                lastClickTime = now
                mItemClickListener?.onItemClick(post)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tips, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<PostListItem>() {
            override fun areItemsTheSame(
                oldItem: PostListItem,
                newItem: PostListItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PostListItem,
                newItem: PostListItem
            ): Boolean = oldItem == newItem
        }
    }
}