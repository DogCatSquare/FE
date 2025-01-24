package com.example.dogcatsquare.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.home.Event
import com.example.dogcatsquare.databinding.ItemPetEventBinding

class EventRVAdapter(private val eventList: ArrayList<Event>) : RecyclerView.Adapter<EventRVAdapter.EventRVAdapterViewHolder>() {
    //    private var d_days: List<DDay> = listOf()
    interface OnItemClickListener {
        fun onItemClick(event: Event)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventRVAdapter.EventRVAdapterViewHolder {
        val binding = ItemPetEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventRVAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventRVAdapter.EventRVAdapterViewHolder, position: Int) {
        val event = eventList[position]
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(eventList[position])
        }
        holder.bind(event)
    }

    override fun getItemCount(): Int = eventList.size

    inner class EventRVAdapterViewHolder(val binding: ItemPetEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.eventTitleTv.text = event.e_title
            binding.eventDateTv.text = event.e_when
            event.e_image?.let {
                Glide.with(binding.root.context)
                    .load(it)
                    .placeholder(R.drawable.img_event_default)
                    .into(binding.eventIv)
            }
        }
    }
}