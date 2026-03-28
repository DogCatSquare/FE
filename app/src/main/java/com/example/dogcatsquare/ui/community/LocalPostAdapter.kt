package com.example.dogcatsquare.ui.community

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.LocalPost

class LocalPostAdapter(
    private val context: Context,
    private val localPosts: MutableList<LocalPost>,
    private val onEditPost: (LocalPost) -> Unit,
    private val onDeletePost: (Int) -> Unit,
    private val isCompactView: Boolean,
    private val onItemClick: ((LocalPost) -> Unit)? = null
) : RecyclerView.Adapter<LocalPostAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val dogBreed: TextView = itemView.findViewById(R.id.tvDogBreed)
        val content: TextView = itemView.findViewById(R.id.tvContent)

        val image1: ImageView? = itemView.findViewById(R.id.ivPostImage1)
        val image2: ImageView? = itemView.findViewById(R.id.ivPostImage2)
        val image3: ImageView? = itemView.findViewById(R.id.ivPostImage3)
        val image4: ImageView? = itemView.findViewById(R.id.ivPostImage4)
        val image5: ImageView? = itemView.findViewById(R.id.ivPostImage5)

        val postMenu: ImageView? = itemView.findViewById(R.id.ivPostMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (isCompactView) {
            R.layout.item_local_compact
        } else {
            R.layout.item_local_post
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = localPosts[position]

        holder.itemView.setOnClickListener { onItemClick?.invoke(post) }

        holder.username.text = post.username
        holder.dogBreed.text = post.dogbreed
        holder.content.text = post.content
        holder.content.apply {
            maxLines = if (isCompactView) 2 else 3
            ellipsize = TextUtils.TruncateAt.END
        }

        val imageViews = listOfNotNull(
            holder.image1,
            holder.image2,
            holder.image3,
            holder.image4,
            holder.image5
        )

        // 재활용 이슈 방지: 매번 초기화
        imageViews.forEach { imageView ->
            Glide.with(holder.itemView).clear(imageView)
            imageView.setImageDrawable(null)
            imageView.visibility = View.GONE
        }

        val images: List<String> = try {
            @Suppress("UNCHECKED_CAST")
            (post.images as? List<String>)?.filter { it.isNotBlank() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        if (images.isNotEmpty()) {
            imageViews.forEachIndexed { index, imageView ->
                if (index < images.size) {
                    imageView.visibility = View.VISIBLE
                    Glide.with(holder.itemView)
                        .load(images[index])
                        .into(imageView)
                }
            }
        } else {
            val ytThumb = getYouTubeThumb(post.videoUrl)
            if (!ytThumb.isNullOrBlank() && holder.image1 != null) {
                holder.image1.visibility = View.VISIBLE
                Glide.with(holder.itemView)
                    .load(ytThumb)
                    .into(holder.image1)
            }
        }

        holder.postMenu?.setOnClickListener {
            showPopupMenu(it, post, position)
        }
    }

    override fun getItemCount(): Int = localPosts.size

    private fun showPopupMenu(
        view: View,
        post: LocalPost,
        position: Int
    ) {
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.post_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    onEditPost(post)
                    true
                }
                R.id.menu_delete -> {
                    onDeletePost(position)
                    true
                }
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

    private fun getYouTubeThumb(videoUrl: String?): String? {
        if (videoUrl.isNullOrBlank()) return null

        val id = when {
            "youtu.be/" in videoUrl ->
                videoUrl.substringAfter("youtu.be/")
                    .substringBefore('?')
                    .substringBefore('&')

            "watch?v=" in videoUrl ->
                videoUrl.substringAfter("watch?v=")
                    .substringBefore('&')

            "/shorts/" in videoUrl ->
                videoUrl.substringAfter("/shorts/")
                    .substringBefore('?')
                    .substringBefore('&')

            "/embed/" in videoUrl ->
                videoUrl.substringAfter("/embed/")
                    .substringBefore('?')
                    .substringBefore('&')

            else -> null
        }

        return id?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }
    }
}