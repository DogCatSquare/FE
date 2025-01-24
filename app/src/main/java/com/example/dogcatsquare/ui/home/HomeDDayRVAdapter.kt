package com.example.dogcatsquare.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.home.DDay
import com.example.dogcatsquare.databinding.ItemHomeCardBinding

class HomeDDayRVAdapter(private val dDayList: List<DDay>) : RecyclerView.Adapter<HomeDDayRVAdapter.HomeDDayAdapterViewHolder>() {
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
    }

    override fun getItemCount(): Int = dDayList.size

    // 클릭 인터페이스 정의

    inner class HomeDDayAdapterViewHolder(val binding: ItemHomeCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(d_day: DDay) {
//            // d_day 제목
//            binding.itemHomeCardTv1.text = d_day.d_title
//
//            // d_day 날짜
//            binding.itemHomeCardTv2.text = d_day.d_when
//
//            // d_day 이미지
//            d_day.d_image?.let {
//                Glide.with(binding.root.context)
//                    .load(it)
//                    .placeholder(R.drawable.ic_hospital)
//                    .into(binding.itemHomeCardIv)
//            }

            if (d_day.isAddButton) {
                // "디데이 추가하기" 버튼 UI
                d_day.d_image?.let {
                    Glide.with(binding.root.context)
                        .load(it)
                        .placeholder(R.drawable.ic_set_d_day)
                        .into(binding.itemHomeCardIv)
                }
                binding.itemHomeCardTv1.text = "디데이 추가하기"
                binding.itemHomeCardTv2.text = "D-n"
            } else {
                // 일반 디데이 UI
                binding.itemHomeCardTv1.text = d_day.d_title
                binding.itemHomeCardTv2.text = d_day.d_when
                if (binding.itemHomeCardTv1.text.equals("병원 방문까지")) {
                    d_day.d_image?.let {
                        Glide.with(binding.root.context)
                            .load(it)
                            .placeholder(R.drawable.ic_hospital)
                            .into(binding.itemHomeCardIv)
                    }
                }
                else if (binding.itemHomeCardTv1.text.equals("사료 주문까지")) {
                    d_day.d_image?.let {
                        Glide.with(binding.root.context)
                            .load(it)
                            .placeholder(R.drawable.ic_food)
                            .into(binding.itemHomeCardIv)
                    }
                } else if (binding.itemHomeCardTv1.text.equals("패드 주문까지")) {
                    d_day.d_image?.let {
                        Glide.with(binding.root.context)
                            .load(it)
                            .placeholder(R.drawable.ic_pad)
                            .into(binding.itemHomeCardIv)
                    }
                } else if (binding.itemHomeCardTv1.text.equals("모래 주문까지")) {
                    d_day.d_image?.let {
                        Glide.with(binding.root.context)
                            .load(it)
                            .placeholder(R.drawable.ic_sand)
                            .into(binding.itemHomeCardIv)
                    }
                }
            }
        }
    }
}