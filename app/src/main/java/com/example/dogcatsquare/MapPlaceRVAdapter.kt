package com.example.dogcatsquare

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.ItemMapButtonBinding
import com.example.dogcatsquare.databinding.ItemMapPlaceBinding

class MapPlaceRVAdapter(private val placeList: ArrayList<MapPlace>, private val listener: OnItemClickListener? = null): RecyclerView.Adapter<MapPlaceRVAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(place: MapPlace)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MapPlaceRVAdapter.ViewHolder {
        val binding: ItemMapPlaceBinding = ItemMapPlaceBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MapPlaceRVAdapter.ViewHolder, position: Int) {
        holder.bind(placeList[position])
    }

    override fun getItemCount(): Int = placeList.size

    inner class ViewHolder(val binding: ItemMapPlaceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(place: MapPlace) {
            binding.placeName.text = place.placeName
            binding.placeType.text = place.placeType
            binding.placeDistance.text = place.placeDistance
            binding.placeLocation.text = place.placeLocation
            binding.placeCall.text = place.placeCall
            binding.placeChar1.text = place.placeChar1
            binding.placeImg.setImageResource(place.placeImg!!)

            // 아이템 클릭 리스너 설정
            itemView.setOnClickListener {
                listener?.onItemClick(place)
            }
        }
    }

}