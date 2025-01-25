package com.example.dogcatsquare.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.data.post.Post
import com.example.dogcatsquare.databinding.ItemHomeHotPostBinding

class HomeHotPostRVAdapter(private val hotPostList: ArrayList<Post>) : RecyclerView.Adapter<HomeHotPostRVAdapter.HomeHotPostAdapterViewHolder>() {
//    private var d_days: List<DDay> = listOf()
    interface OnItemClickListener {
        fun onItemClick(post: Post)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHotPostRVAdapter.HomeHotPostAdapterViewHolder {
        val binding = ItemHomeHotPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeHotPostAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeHotPostRVAdapter.HomeHotPostAdapterViewHolder, position: Int) {
        val hotPost = hotPostList[position]
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(hotPostList[position])
        }
        holder.bind(hotPost)
    }

    override fun getItemCount(): Int = hotPostList.size

    inner class HomeHotPostAdapterViewHolder(val binding: ItemHomeHotPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hotPost: Post) {
            // 타이틀
            binding.postTitleTv.text = hotPost.title

            // 내용
            binding.postContentTv.text = hotPost.content

            // 닉네임
            binding.postNicknameTv.text = hotPost.nickname

            // 프로필
            binding.postProfileIv

            // 내용 사진(썸네일)

            // 좋아요 수

            // 답글 수
        }
    }
}