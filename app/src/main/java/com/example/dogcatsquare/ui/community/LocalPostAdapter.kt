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
import com.example.dogcatsquare.data.community.LocalPost

class LocalPostAdapter(
    private val context: Context,
    private val localPosts: MutableList<LocalPost>,
    private val onEditPost: (LocalPost) -> Unit, // 수정 기능 연결
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

        // 게시글 작성자, 견종, 내용을 설정
        holder.username.text = post.username
        holder.dogBreed.text = post.dogbreed
        holder.content.text = post.content

        // 홈 탭에서는 2줄, 동네 이야기 탭에서는 3줄 표시
        holder.content.apply {
            maxLines = if (isCompactView) 2 else 3
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        // 이미지가 있다면, dummy 데이터의 경우 리소스 ID를 사용한다고 가정하여 캐스팅 처리
        if (post.images.isNotEmpty()) {
            val imageList = post.images as? List<Int>
            if (imageList != null && imageList.isNotEmpty()) {
                holder.image1?.setImageResource(imageList[0])
                holder.image1?.visibility = View.VISIBLE

                if (imageList.size > 1) {
                    holder.image2?.setImageResource(imageList[1])
                    holder.image2?.visibility = View.VISIBLE
                } else {
                    holder.image2?.visibility = View.GONE
                }
            } else {
                holder.image1?.visibility = View.GONE
                holder.image2?.visibility = View.GONE
            }
        } else {
            holder.image1?.visibility = View.GONE
            holder.image2?.visibility = View.GONE
        }

        // 메뉴 버튼 클릭 이벤트: 수정/삭제 기능 연결
        holder.postMenu?.setOnClickListener { showPopupMenu(it, post, position) }
    }

    override fun getItemCount(): Int = localPosts.size

    private fun showPopupMenu(view: View, post: LocalPost, position: Int) {
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
}
