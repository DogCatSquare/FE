package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.Reply

class RepliesAdapter(private var replies: MutableList<com.example.dogcatsquare.data.model.community.Reply>) :
    RecyclerView.Adapter<RepliesAdapter.ReplyViewHolder>() {

    inner class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivReplyProfile: ImageView = itemView.findViewById(R.id.ivReplyProfile)
        val tvReplyUsername: TextView = itemView.findViewById(R.id.tvReplyUsername)
        val tvReplyDogBreed: TextView = itemView.findViewById(R.id.tvReplyDogBreed)
        val tvReplyContent: TextView = itemView.findViewById(R.id.tvReplyContent)
        val tvReplyTimestamp: TextView = itemView.findViewById(R.id.tvReplyTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reply, parent, false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val reply = replies[position]
        holder.tvReplyUsername.text = reply.name
        holder.tvReplyDogBreed.text = reply.dogBreed
        holder.tvReplyContent.text = reply.content
        holder.tvReplyTimestamp.text = reply.timestamp

        Glide.with(holder.itemView.context)
            .load(reply.profileImageUrl)
            .into(holder.ivReplyProfile)
    }

    override fun getItemCount(): Int = replies.size

    // ✅ 기존 리스트를 변경하는 방식으로 수정
    fun updateReplies(newReplies: List<com.example.dogcatsquare.data.model.community.Reply>) {
        replies.clear()
        replies.addAll(newReplies)
        notifyDataSetChanged()
    }
}
