package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ItemLocalPostBinding
import com.example.dogcatsquare.ui.viewmodel.PostViewModel
import com.example.dogcatsquare.util.DateFmt

class MyBoardPostRVAdapter(
    private val boardPost: ArrayList<com.example.dogcatsquare.data.model.post.Post>,
    private val postViewModel: PostViewModel,
    private val userId: Int?,
    private val token: String?,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<MyBoardPostRVAdapter.MyBoardPostRVAdapterViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(post: com.example.dogcatsquare.data.model.post.Post)
    }

    private lateinit var mItemClickListener: OnItemClickListener
    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBoardPostRVAdapterViewHolder {
        val binding = ItemLocalPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyBoardPostRVAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyBoardPostRVAdapterViewHolder, position: Int) {
        val post = boardPost[position]
        holder.itemView.setOnClickListener { mItemClickListener.onItemClick(post) }
        holder.bind(post)
    }

    override fun getItemCount(): Int = boardPost.size

    inner class MyBoardPostRVAdapterViewHolder(
        private val binding: ItemLocalPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: com.example.dogcatsquare.data.model.post.Post) = with(binding) {
            tvUsername.text = post.username
            tvTitle.text = post.title ?: "제목 없음"
            tvContent.text = post.content ?: "내용 없음"
            tvLikeCount.text = post.like_count.toString()
            tvCommentCount.text = post.comment_count.toString()

            tvDate.text = DateFmt.format(post.createdAt)

            Glide.with(itemView.context)
                .load(post.profileImage_URL)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(ivProfile)

            // 이미지 표시 (최대 5개)
            val imageViews = listOf(ivPostImage1, ivPostImage2, ivPostImage3, ivPostImage4, ivPostImage5)
            val images = post.images.orEmpty()

            imageViews.forEachIndexed { index, imageView ->
                if (index < images.size) {
                    imageView.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(images[index])
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(imageView)
                } else {
                    imageView.visibility = View.GONE
                }
            }
        }
    }
}