package com.example.dogcatsquare.ui.community

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.LocalPost
import com.bumptech.glide.Glide  // ★ Glide 추가

class LocalPostAdapter(
    private val context: Context,
    private val localPosts: MutableList<com.example.dogcatsquare.data.model.community.LocalPost>,
    private val onEditPost: (com.example.dogcatsquare.data.model.community.LocalPost) -> Unit,
    private val onDeletePost: (Int) -> Unit,
    private val isCompactView: Boolean,
    private val onItemClick: ((com.example.dogcatsquare.data.model.community.LocalPost) -> Unit)? = null
) : RecyclerView.Adapter<LocalPostAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val dogBreed: TextView = itemView.findViewById(R.id.tvDogBreed)
        val content: TextView = itemView.findViewById(R.id.tvContent)
        val image1: ImageView? = itemView.findViewById(R.id.ivPostImage1)
        val image2: ImageView? = itemView.findViewById(R.id.ivPostImage2)
        val postMenu: ImageView? = itemView.findViewById(R.id.ivPostMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (isCompactView) R.layout.item_local_compact else R.layout.item_local_post
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = localPosts[position]

        // 전체 아이템 클릭 시 상세 화면으로 이동
        holder.itemView.setOnClickListener { onItemClick?.invoke(post) }

        // 텍스트 바인딩
        holder.username.text = post.username
        holder.dogBreed.text = post.dogbreed
        holder.content.text = post.content
        holder.content.apply {
            maxLines = if (isCompactView) 2 else 3
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        // ===== 이미지 처리 (요구 사항 반영) =====
        // 1) images(String URL) 시도 → Glide 로드
        // 2) 없으면 videoUrl에서 유튜브 썸네일 생성 → 로드
        // 3) 둘 다 없으면 GONE
        val images: List<String>? = post.images as? List<String>
        val firstUrl = images?.getOrNull(0)
        val secondUrl = images?.getOrNull(1)

        var loadedAny = false

        // 첫 번째 이미지
        if (!firstUrl.isNullOrBlank() && holder.image1 != null) {
            holder.image1.visibility = View.VISIBLE
            Glide.with(holder.itemView)
                .load(firstUrl)
                .into(holder.image1)
            loadedAny = true
        } else {
            // 유튜브 썸네일 대체 (첫 번째만)
            val ytThumb = getYouTubeThumb(post.videoUrl)
            if (ytThumb != null && holder.image1 != null) {
                holder.image1.visibility = View.VISIBLE
                Glide.with(holder.itemView)
                    .load(ytThumb)
                    .into(holder.image1)
                loadedAny = true
            } else {
                holder.image1?.visibility = View.GONE
            }
        }

        // 두 번째 이미지
        if (!secondUrl.isNullOrBlank() && holder.image2 != null) {
            holder.image2.visibility = View.VISIBLE
            Glide.with(holder.itemView)
                .load(secondUrl)
                .into(holder.image2)
            loadedAny = true
        } else {
            holder.image2?.visibility = View.GONE
        }

        // 어떤 것도 못 불러왔으면 둘 다 GONE 보장
        if (!loadedAny) {
            holder.image1?.visibility = View.GONE
            holder.image2?.visibility = View.GONE
        }
        // ===== 이미지 처리 끝 =====

        // 메뉴 버튼: 수정/삭제
        holder.postMenu?.setOnClickListener { showPopupMenu(it, post, position) }
    }

    override fun getItemCount(): Int = localPosts.size

    private fun showPopupMenu(
        view: View,
        post: com.example.dogcatsquare.data.model.community.LocalPost,
        position: Int
    ) {
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.post_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> { onEditPost(post); true }
                R.id.menu_delete -> { onDeletePost(position); true }
                else -> false
            }
        }
        popup.show()
    }

    fun removePost(position: Int) {
        if (position in localPosts.indices) {
            localPosts.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, localPosts.size)
        }
    }

    // ★ 유튜브 썸네일 URL 생성 헬퍼
    // 지원: youtu.be/XXXX, youtube.com/watch?v=XXXX, youtube.com/shorts/XXXX
    private fun getYouTubeThumb(videoUrl: String?): String? {
        if (videoUrl.isNullOrBlank()) return null
        val id = when {
            "youtu.be/" in videoUrl -> videoUrl.substringAfter("youtu.be/").substringBefore('?').substringBefore('&')
            "watch?v=" in videoUrl -> videoUrl.substringAfter("watch?v=").substringBefore('&')
            "/shorts/" in videoUrl -> videoUrl.substringAfter("/shorts/").substringBefore('?').substringBefore('&')
            else -> null
        }
        return id?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }
    }
}