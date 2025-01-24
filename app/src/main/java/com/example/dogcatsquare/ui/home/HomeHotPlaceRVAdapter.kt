package com.example.dogcatsquare.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.MapPlace
import com.example.dogcatsquare.MapPlaceRVAdapter.OnItemClickListener
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ItemHomeHotPlaceBinding
import com.example.dogcatsquare.ui.map.MapDetailFragment

class HomeHotPlaceRVAdapter(private val placeList: ArrayList<MapPlace>) : RecyclerView.Adapter<HomeHotPlaceRVAdapter.HomeHotPlaceAdapterViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(place: MapPlace)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeHotPlaceRVAdapter.HomeHotPlaceAdapterViewHolder {
        val binding = ItemHomeHotPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeHotPlaceAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: HomeHotPlaceAdapterViewHolder,
        position: Int
    ) {
        val hot_place = placeList[position]
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(placeList[position])
        }
        holder.bind(hot_place)
    }

    override fun getItemCount(): Int = placeList.size

    inner class HomeHotPlaceAdapterViewHolder(val binding: ItemHomeHotPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hot_place: MapPlace) {
            // 핫플 이름
            binding.hotPlaceTv.text = hot_place.placeName

            // 핫플 거리
            binding.hotPlaceLocTv.text = hot_place.placeDistance

            // 핫플 카테고리
            binding.hotPlaceFilterTv.text = hot_place.placeType

            // 핫플 이미지
            hot_place.placeImg?.let {
                Glide.with(binding.root.context)
                    .load(it)
                    .placeholder(R.drawable.img_home_hot_place_pet)
                    .into(binding.hotPlaceIv)
            }
        }
    }
}