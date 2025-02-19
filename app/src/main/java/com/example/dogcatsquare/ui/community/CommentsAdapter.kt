package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.Comment
import com.example.dogcatsquare.data.community.Reply

class CommentsAdapter(
    private val comments: ArrayList<Comment>,
    private val actionListener: CommentActionListener // 액션 처리를 위한 콜백 인터페이스
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvCommentContent: TextView = itemView.findViewById(R.id.tvCommentContent)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val rvReplies: RecyclerView = itemView.findViewById(R.id.rvReplies)
        val ivReplyMenu: ImageView = itemView.findViewById(R.id.ivReplyMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvUsername.text = comment.name
        holder.tvCommentContent.text = comment.content
        holder.tvTimestamp.text = comment.timestamp

        Glide.with(holder.itemView.context)
            .load(comment.profileImageUrl)
            .into(holder.ivProfile)

    // ✅ 대댓글을 `List<Comment>`에서 `List<Reply>`로 변환하여 사용
        val repliesList = comments.filter { it.parentId == comment.id.toString() }.map { comment ->
            Reply(
                id = comment.id,
                content = comment.content,
                name = comment.name,
                dogBreed = "",  // 필요하면 여기에 데이터를 추가
                profileImageUrl = comment.profileImageUrl,
                timestamp = comment.timestamp
            )
        }.toMutableList()  // ✅ `MutableList<Reply>` 변환

        if (repliesList.isNotEmpty()) {
            holder.rvReplies.visibility = View.VISIBLE
            holder.rvReplies.layoutManager = LinearLayoutManager(holder.itemView.context)

            val repliesAdapter = holder.rvReplies.adapter as? RepliesAdapter
            if (repliesAdapter == null) {
                holder.rvReplies.adapter = RepliesAdapter(repliesList)
            } else {
                repliesAdapter.updateReplies(repliesList)
            }
        } else {
            holder.rvReplies.visibility = View.GONE
        }

        // 메뉴 버튼 클릭 이벤트 처리
        holder.ivReplyMenu.setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.comment_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_reply -> {
                        actionListener.onReplyClicked(comment)
                        true
                    }
                    R.id.action_delete -> {
                        actionListener.onDeleteClicked(comment)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = comments.size
}
