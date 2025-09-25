package com.example.dogcatsquare.ui.community

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.Comment
import com.example.dogcatsquare.data.model.community.Reply

class CommentsAdapter(
    private val comments: ArrayList<com.example.dogcatsquare.data.model.community.Comment>,
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

        val sp = holder.itemView.context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val myUserId = sp.getInt("userId", -1)
        val isMine = (comment.userId == myUserId)

        holder.ivReplyMenu.visibility = if (isMine) View.VISIBLE else View.GONE

        val repliesList = comments.filter { it.parentId == comment.id.toString() }.map { reply ->
            Reply(
                id = reply.id,
                content = reply.content,
                name = reply.name,
                dogBreed = "",
                profileImageUrl = reply.profileImageUrl,
                timestamp = reply.timestamp
            )
        }.toMutableList()

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

        holder.ivReplyMenu.visibility = View.VISIBLE

        holder.ivReplyMenu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.comment_menu, popup.menu)

            val sp = view.context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            val myUserId = sp.getInt("userId", -1)
            val isMine = (comment.userId == myUserId)   // ← Comment.userId 필드가 있어야 합니다

            // reply는 항상, delete는 내 댓글일 때만
            popup.menu.findItem(R.id.action_reply)?.isVisible = true
            popup.menu.findItem(R.id.action_delete)?.isVisible = isMine

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
