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
import com.example.dogcatsquare.data.model.community.GetAllPostResult
import com.example.dogcatsquare.util.DateFmt

class GetAllPostAdapter :
    ListAdapter<GetAllPostResult, GetAllPostAdapter.PostViewHolder>(DiffCallback) {

    interface OnItemClickListener {
        fun onItemClick(post: GetAllPostResult)
    }

    private var mItemClickListener: OnItemClickListener? = null
    fun setMyItemClickListener(listener: OnItemClickListener) {
        mItemClickListener = listener
    }

    private var lastClickTime = 0L
    private val debounceInterval = 500L

    // ---- YouTube helpers ----
    private fun extractYouTubeVideoId(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val patterns = listOf(
            "(?:v=)([a-zA-Z0-9_-]{11})",                 // youtube.com/watch?v=VIDEOID
            "youtu.be/([a-zA-Z0-9_-]{11})",              // youtu.be/VIDEOID
            "youtube.com/shorts/([a-zA-Z0-9_-]{11})"     // youtube.com/shorts/VIDEOID
        )
        for (p in patterns) {
            val regex = Regex(p)
            val m = Regex(p, RegexOption.IGNORE_CASE).find(url)
            if (m != null && m.groupValues.size > 1) return m.groupValues[1]
        }
        return null
    }

    private fun youtubeThumbnail(url: String?): String? =
        extractYouTubeVideoId(url)?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

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
            // ===== 텍스트 =====
            titleText.text = post.title ?: "제목 없음"
            contentPreview.text = post.content ?: "내용 없음"
            username.text = post.username ?: ""
            breed.text = post.animal_type ?: ""
            likeCountText.text = (post.likeCount ?: 0).toString()
            commentCountText.text = (post.commentCount ?: 0).toString()
            dateText.text = DateFmt.format(post.createdAt)

            // ===== 프로필 =====
            Glide.with(itemView.context).clear(profile)
            if (!post.profileImageURL.isNullOrBlank()) {
                profile.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(post.profileImageURL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profile)
            } else {
                // 기본 프로필 이미지 유지
                profile.visibility = View.VISIBLE
                profile.setImageResource(R.drawable.ic_profile_img_default)
            }

            // ===== 썸네일 =====
            Glide.with(itemView.context).clear(thumbnail)

            // 1) 서버가 준 이미지(썸네일 or 첫 이미지) 우선
            val imageThumb: String? =
                post.thumbnailURL?.takeIf { it.isNotBlank() && !it.endsWith(".mp4", true) }
                    ?: post.images.firstOrNull()?.takeIf { it.isNotBlank() }

            // 2) 비디오만 있을 때(썸네일/이미지 없음) -> 비디오 프레임 추출
            val videoUrl: String? = post.videoURL?.takeIf { it.isNotBlank() }
            val youTubeThumb: String? = youtubeThumbnail(videoUrl)

            when {
                imageThumb != null -> {
                    thumbnail.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(imageThumb)
                        .centerCrop()
                        .into(thumbnail)
                }
                youTubeThumb != null -> {
                    // YouTube 링크일 때는 프레임 추출이 불가하므로 공식 썸네일 URL 사용
                    thumbnail.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(youTubeThumb)
                        .centerCrop()
                        .into(thumbnail)
                }
                videoUrl != null && (videoUrl.endsWith(".mp4", true) || videoUrl.contains(".mp4", true)) -> {
                    // mp4 등 직접 스트리밍되는 동영상만 프레임 추출
                    thumbnail.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .asBitmap()
                        .load(videoUrl)
                        .apply(
                            RequestOptions()
                                .frame(1_000_000) // 1s frame
                                .centerCrop()
                        )
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
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<GetAllPostResult>() {
            override fun areItemsTheSame(
                oldItem: GetAllPostResult,
                newItem: GetAllPostResult
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: GetAllPostResult,
                newItem: GetAllPostResult
            ): Boolean = oldItem == newItem
        }
    }
}