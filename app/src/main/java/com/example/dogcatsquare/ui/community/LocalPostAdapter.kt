package com.example.dogcatsquare.ui.community

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R

class LocalPostAdapter(
    private val context: Context,
    private val localPosts: MutableList<LocalPost>,
    private val onEditPost: (LocalPost) -> Unit, // 🔹 추가
    private val onDeletePost: (Int) -> Unit,
    private val isCompactView: Boolean
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

        holder.username.text = post.username
        holder.dogBreed.text = post.dogbreed
        holder.content.text = post.content

        // 🛠 홈 탭에서는 2줄, 동네이야기 탭에서는 3줄 표시
        holder.content.apply {
            maxLines = if (isCompactView) 2 else 3
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        if (post.images.isNotEmpty()) {
            holder.image1?.setImageResource(post.images[0])
            holder.image1?.visibility = View.VISIBLE

            if (post.images.size > 1) {
                holder.image2?.setImageResource(post.images[1])
                holder.image2?.visibility = View.VISIBLE
            } else {
                holder.image2?.visibility = View.GONE
            }
        } else {
            holder.image1?.visibility = View.GONE
            holder.image2?.visibility = View.GONE
        }

        // 메뉴 버튼 클릭 이벤트 설정
        holder.postMenu?.setOnClickListener { showPopupMenu(it, post, position) }
    }


    override fun getItemCount(): Int = localPosts.size

    private fun showPopupMenu(view: View, post: LocalPost, position: Int) {
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.post_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    onEditPost(post) // 🔹 수정 기능 연결
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
}
