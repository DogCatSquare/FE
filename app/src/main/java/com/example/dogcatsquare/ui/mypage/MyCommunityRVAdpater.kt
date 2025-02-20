package com.example.dogcatsquare.ui.mypage

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.Post
import com.example.dogcatsquare.databinding.ItemMyCommunityBinding
import com.example.dogcatsquare.ui.community.PostDetailActivity

class MyCommunityRVAdpater(private val myPostList: ArrayList<com.example.dogcatsquare.data.model.post.Post>) : RecyclerView.Adapter<MyCommunityRVAdpater.MyCommunityAdapterViewHolder>(){
    interface OnItemClickListener {
        fun onItemClick(myPost: com.example.dogcatsquare.data.model.post.Post)
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
        fun bind(myPost: com.example.dogcatsquare.data.model.post.Post) {
            binding.tvTitle.text = myPost.title
            binding.tvContent.text = myPost.content
            binding.tvLikeCount.text = myPost.like_count.toString()
            binding.tvCommentCount.text = myPost.comment_count.toString()
            binding.tvDate.text = myPost.createdAt

            // 이미지가 존재하는 경우 표시, 없으면 GONE 처리
            if (!myPost.images.isNullOrEmpty()) {
                val imageViews = listOf(
                    binding.ivPostImage1,
                    binding.ivPostImage2,
                    binding.ivPostImage3,
                    binding.ivPostImage4,
                    binding.ivPostImage5
                )

                // 이미지 최대 5개까지 표시
                for (i in imageViews.indices) {
                    if (i < myPost.images.size) {
                        imageViews[i].visibility = View.VISIBLE
                        Glide.with(itemView.context)
                            .load(myPost.images[i])
                            .placeholder(R.drawable.ic_placeholder)
                            .into(imageViews[i])
                    } else {
                        imageViews[i].visibility = View.GONE
                    }
                }
            } else {
                // 이미지가 없으면 모든 ImageView 숨기기
                binding.ivPostImage1.visibility = View.GONE
                binding.ivPostImage2.visibility = View.GONE
                binding.ivPostImage3.visibility = View.GONE
                binding.ivPostImage4.visibility = View.GONE
                binding.ivPostImage5.visibility = View.GONE
            }


            itemView.setOnClickListener {
                val intent = Intent(itemView.context, PostDetailActivity::class.java).apply {
                    putExtra("postId", myPost.id) // ✅ postId 전달
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}