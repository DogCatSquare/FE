package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.BoardPost
import com.example.dogcatsquare.data.model.post.Post
import com.example.dogcatsquare.databinding.ItemHomeHotPostBinding
import com.example.dogcatsquare.databinding.ItemLocalPostBinding
import com.example.dogcatsquare.ui.home.HomeHotPostRVAdapter

class MyBoardPostRVAdapter(private val boardPost: ArrayList<Post>)  : RecyclerView.Adapter<MyBoardPostRVAdapter.MyBoardPostRVAdapterViewHolder>() {
    //    private var d_days: List<DDay> = listOf()
    interface OnItemClickListener {
        fun onItemClick(post: Post)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBoardPostRVAdapter.MyBoardPostRVAdapterViewHolder {
        val binding = ItemLocalPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyBoardPostRVAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyBoardPostRVAdapter.MyBoardPostRVAdapterViewHolder, position: Int) {
        val board = boardPost[position]
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(boardPost[position])
        }
        holder.bind(board)
    }

    override fun getItemCount(): Int = boardPost.size

    inner class MyBoardPostRVAdapterViewHolder(val binding: ItemLocalPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(boardPost: Post) {
            binding.tvUsername.text = boardPost.username
            binding.tvTitle.text = boardPost.title ?: "제목 없음"
            binding.tvContent.text = boardPost.content ?: "내용 없음"
            binding.tvLikeCount.text = boardPost.like_count.toString()
            binding.tvCommentCount.text = boardPost.comment_count.toString()
            binding.tvDate.text = boardPost.createdAt
            Glide.with(itemView.context)
                .load(boardPost.profileImage_URL)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(binding.ivProfile)

            // 이미지가 존재하는 경우 표시, 없으면 GONE 처리
            if (!boardPost.images.isNullOrEmpty()) {
                val imageViews = listOf(
                    binding.ivPostImage1,
                    binding.ivPostImage2,
                    binding.ivPostImage3,
                    binding.ivPostImage4,
                    binding.ivPostImage5
                )

                // 이미지 최대 5개까지 표시
                for (i in imageViews.indices) {
                    if (i < boardPost.images.size) {
                        imageViews[i].visibility = View.VISIBLE
                        Glide.with(itemView.context)
                            .load(boardPost.images[i])
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

        }
    }
}