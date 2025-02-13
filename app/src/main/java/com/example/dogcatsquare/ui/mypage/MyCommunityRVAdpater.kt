package com.example.dogcatsquare.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.data.post.Post
import com.example.dogcatsquare.databinding.ItemMyCommunityBinding
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
        }
    }
}