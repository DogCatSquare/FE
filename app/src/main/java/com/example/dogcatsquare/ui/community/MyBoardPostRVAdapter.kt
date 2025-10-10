package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ItemLocalPostBinding
import com.example.dogcatsquare.ui.viewmodel.PostViewModel
import com.example.dogcatsquare.util.DateFmt

class MyBoardPostRVAdapter(
    private val boardPost: ArrayList<com.example.dogcatsquare.data.model.post.Post>,
    private val postViewModel: PostViewModel,
    private val userId: Int?,
    private val token: String?,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<MyBoardPostRVAdapter.MyBoardPostRVAdapterViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(post: com.example.dogcatsquare.data.model.post.Post)
    }

    private lateinit var mItemClickListener: OnItemClickListener
    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBoardPostRVAdapterViewHolder {
        val binding = ItemLocalPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyBoardPostRVAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyBoardPostRVAdapterViewHolder, position: Int) {
        val post = boardPost[position]
        holder.itemView.setOnClickListener { mItemClickListener.onItemClick(post) }
        holder.bind(post)
    }

    override fun getItemCount(): Int = boardPost.size

    inner class MyBoardPostRVAdapterViewHolder(
        private val binding: ItemLocalPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: com.example.dogcatsquare.data.model.post.Post) = with(binding) {
            tvUsername.text = post.username
            tvTitle.text = post.title ?: "제목 없음"
            tvContent.text = post.content ?: "내용 없음"
            tvLikeCount.text = post.like_count.toString()
            tvCommentCount.text = post.comment_count.toString()
            tvDate.text = DateFmt.format(post.createdAt)

            // ===== 프로필 =====
            Glide.with(itemView.context).clear(ivProfile)
            Glide.with(itemView.context)
                .load(post.profileImage_URL)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(ivProfile)

            // ===== 대표 썸네일 결정 =====
            val images = post.images ?: emptyList()
            val firstImage = images.firstOrNull()?.takeIf { !it.isNullOrBlank() }

            // 썸네일 URL이 있고 mp4가 아니면 우선
            val thumbUrl = post.thumbnail_URL?.takeIf { it.isNotBlank() && !it.endsWith(".mp4", true) }
            val videoUrl = post.video_URL?.takeIf { it.isNotBlank() }

            // 유튜브 영상이면 썸네일 생성
            val yt = youtubeThumb(videoUrl)

            // 우선순위: 썸네일 → 첫번째 이미지 → 유튜브 → (없으면 mp4 프레임 추출)
            val primaryThumb = thumbUrl ?: firstImage ?: yt

            // 먼저 전부 클리어
            val imageViews = listOf(ivPostImage1, ivPostImage2, ivPostImage3, ivPostImage4, ivPostImage5)
            imageViews.forEach { iv ->
                Glide.with(iv).clear(iv)
                iv.setImageDrawable(null)
                iv.visibility = View.GONE
            }

            // 1) 대표 썸네일 채우기 (ivPostImage1)
            if (primaryThumb != null) {
                ivPostImage1.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(primaryThumb)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(ivPostImage1)
            } else if (videoUrl != null && (videoUrl.endsWith(".mp4", true) || videoUrl.contains(".mp4", true))) {
                // mp4면 1초 프레임 추출
                ivPostImage1.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .asBitmap()
                    .load(videoUrl)
                    .apply(RequestOptions().frame(1_000_000).centerCrop())
                    .into(ivPostImage1)
            }

            // 2) 나머지 보조 이미지들(images[1..]) 채우기
            for (i in 1 until minOf(images.size, imageViews.size)) {
                val url = images[i] ?: continue
                val iv = imageViews.getOrNull(i) ?: continue
                if (url.isNotBlank()) {
                    iv.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(url)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .centerCrop()
                        .into(iv)
                }
            }
        }
    }

    companion object {
        // ====== YouTube ID/썸네일 헬퍼 ======
        private fun extractYouTubeVideoId(url: String?): String? {
            if (url.isNullOrBlank()) return null
            val patterns = listOf(
                "(?:v=)([a-zA-Z0-9_-]{11})",
                "youtu.be/([a-zA-Z0-9_-]{11})",
                "youtube.com/shorts/([a-zA-Z0-9_-]{11})"
            )
            for (p in patterns) {
                val m = Regex(p, RegexOption.IGNORE_CASE).find(url)
                if (m != null && m.groupValues.size > 1) return m.groupValues[1]
            }
            return null
        }

        private fun youtubeThumb(url: String?): String? =
            extractYouTubeVideoId(url)?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }
    }
}