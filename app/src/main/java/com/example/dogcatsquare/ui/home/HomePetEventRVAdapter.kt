package com.example.dogcatsquare.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.home.Event
import com.example.dogcatsquare.databinding.ItemPetEventBinding

class HomePetEventRVAdapter (private val eventList: ArrayList<Event>) : RecyclerView.Adapter<HomePetEventRVAdapter.HomePetEventAdapterViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(event: Event)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePetEventRVAdapter.HomePetEventAdapterViewHolder {
        val binding = ItemPetEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomePetEventAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomePetEventRVAdapter.HomePetEventAdapterViewHolder, position: Int) {
        val event = eventList[position]
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(eventList[position])
        }
        holder.bind(event)
    }

    override fun getItemCount(): Int = eventList.size

    // 클릭 인터페이스 정의

    inner class HomePetEventAdapterViewHolder(val binding: ItemPetEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            // 이벤트 제목
            binding.eventTitleTv.text = event.title

            // 이벤트 날짜
            binding.eventDateTv.text = event.period

            // 이벤트 이미지
            Glide.with(this.itemView)
                .load(event.bannerImageUrl)
                .placeholder(R.drawable.img_event_default)
                .into(binding.eventIv)
        }
    }
}