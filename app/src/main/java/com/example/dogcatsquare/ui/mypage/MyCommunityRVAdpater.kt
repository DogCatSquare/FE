package com.example.dogcatsquare.ui.mypage

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.post.Post
import com.example.dogcatsquare.databinding.ItemMyCommunityBinding
import com.example.dogcatsquare.ui.community.PostDetailActivity
import com.example.dogcatsquare.ui.home.HomeHotPostRVAdapter.OnItemClickListener

class MyCommunityRVAdpater(private val myPostList: ArrayList<Post>) : RecyclerView.Adapter<MyCommunityRVAdpater.MyCommunityAdapterViewHolder>(){
    interface OnItemClickListener {
        fun onItemClick(myPost: Post)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyCommunityRVAdpater.MyCommunityAdapterViewHolder {
        val binding = ItemMyCommunityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyCommunityAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyCommunityRVAdpater.MyCommunityAdapterViewHolder, position: Int) {
        val myPost = myPostList[position]
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(myPostList[position])
        }
        holder.bind(myPost)
    }

    override fun getItemCount(): Int = myPostList.size

    inner class MyCommunityAdapterViewHolder(val binding: ItemMyCommunityBinding) :RecyclerView.ViewHolder(binding.root) {
        fun bind(myPost: Post) {
            binding.tvTitle.text = myPost.title
            binding.tvContent.text = myPost.content
            binding.tvLikeCount.text = myPost.like_count.toString()
            binding.tvCommentCount.text = myPost.comment_count.toString()

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, PostDetailActivity::class.java).apply {
                    putExtra("postId", myPost.id) // ✅ postId 전달
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}