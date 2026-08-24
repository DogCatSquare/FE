package com.dogcatsquare.dogcatsquare.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dogcatsquare.dogcatsquare.R
import com.dogcatsquare.dogcatsquare.data.model.home.DDay
import com.dogcatsquare.dogcatsquare.databinding.ItemHomeCardBinding
import com.dogcatsquare.dogcatsquare.utils.AlarmHelper

class HomeDDayRVAdapter(private val dDayList: ArrayList<DDay>, private val context: Context) : RecyclerView.Adapter<HomeDDayRVAdapter.HomeDDayAdapterViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(d_day: DDay)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeDDayRVAdapter.HomeDDayAdapterViewHolder {
        val binding = ItemHomeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeDDayAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeDDayRVAdapter.HomeDDayAdapterViewHolder, position: Int) {
        val d_day = dDayList[position]
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(dDayList[position])
        }
        holder.bind(d_day)

        if (d_day.isAlarm) {
            AlarmHelper.setDdayAlarm(context, d_day)
        }
    }

    override fun getItemCount(): Int = dDayList.size

    inner class HomeDDayAdapterViewHolder(val binding: ItemHomeCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(d_day: DDay) {
            binding.itemHomeCardTv1.text = d_day.title
            binding.itemHomeCardTv2.text = d_day.ddayText
            Glide.with(this.itemView)
                .load(d_day.ddayImageUrl)
                .placeholder(R.drawable.ic_set_d_day)
                .into(binding.itemHomeCardIv)
        }
    }
}