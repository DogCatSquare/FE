package com.example.dogcatsquare.ui.mypage

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.announcement.AnnouncementResponse
import com.example.dogcatsquare.databinding.ItemAnnouncementBinding
import com.example.dogcatsquare.databinding.ItemMyCommunityBinding
import com.example.dogcatsquare.ui.community.PostDetailActivity

class AnnouncementRVAdapter(private val announcementList: ArrayList<AnnouncementResponse>) : RecyclerView.Adapter<AnnouncementRVAdapter.AnnouncementAdapterHolder>() {
    interface OnItemClickListener {
        fun onItemClick(announcement: AnnouncementResponse)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnnouncementRVAdapter.AnnouncementAdapterHolder {
        val binding = ItemAnnouncementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnouncementAdapterHolder(binding)
    }

    override fun onBindViewHolder(holder: AnnouncementRVAdapter.AnnouncementAdapterHolder, position: Int) {
        val myPost = announcementList[position]
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(announcementList[position])
        }
        holder.bind(myPost)
    }

    override fun getItemCount(): Int = announcementList.size

    inner class AnnouncementAdapterHolder(val binding: ItemAnnouncementBinding) :RecyclerView.ViewHolder(binding.root) {
        fun bind(announcementResponse: AnnouncementResponse) {

            binding.announcmentCategoryTv.text = announcementResponse.category
            binding.announcmentTimeTv.text = announcementResponse.time
            binding.announcementTitleTv.text = announcementResponse.title
        }
    }
}