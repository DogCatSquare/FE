package com.example.dogcatsquare.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.data.map.MapPlace
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.map.Place
import com.example.dogcatsquare.databinding.ItemHomeHotPlaceBinding
import com.example.dogcatsquare.ui.map.location.MapDetailFragment

class HomeHotPlaceRVAdapter(private val placeList: ArrayList<Place>) : RecyclerView.Adapter<HomeHotPlaceRVAdapter.HomeHotPlaceAdapterViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(place: Place)
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
        fun bind(hot_place: Place) {
            binding.hotPlaceTv.text = hot_place.name
            binding.hotPlaceLocTv.text = "${hot_place.distance.toInt().toString().take(2)}km"
            binding.hotPlaceFilterTv.text = hot_place.category
            Glide.with(binding.root.context)
                .load(hot_place.imgUrl)
                .placeholder(R.drawable.ic_profile_default)
                .into(binding.hotPlaceIv)
        }
    }
}