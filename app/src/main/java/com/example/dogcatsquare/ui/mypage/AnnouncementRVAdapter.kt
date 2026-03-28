package com.example.dogcatsquare.ui.mypage

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.announcement.Notice
import com.example.dogcatsquare.databinding.ItemAnnouncementBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AnnouncementRVAdapter(private val announcementList: ArrayList<Notice>) : RecyclerView.Adapter<AnnouncementRVAdapter.AnnouncementAdapterHolder>() {
    interface OnItemClickListener {
        fun onItemClick(announcement: Notice)
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
        fun bind(notice: Notice) {
            binding.announcmentCategoryTv.text = "공지"
            binding.announcementTitleTv.text = notice.title

            // 날짜 포맷팅 (YYYY-MM-DDTHH:MM:SS -> yy.MM.dd)
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
                val date = inputFormat.parse(notice.createdAt)
                binding.announcmentTimeTv.text = date?.let { outputFormat.format(it) } ?: notice.createdAt
            } catch (e: Exception) {
                binding.announcmentTimeTv.text = notice.createdAt
            }
        }
    }
}