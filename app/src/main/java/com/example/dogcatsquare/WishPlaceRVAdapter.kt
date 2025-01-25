package com.example.dogcatsquare

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.ItemMapButtonBinding
import com.example.dogcatsquare.databinding.ItemMapPlaceBinding
import com.example.dogcatsquare.databinding.ItemWishPlaceBinding

class WishPlaceRVAdapter(private val placeList: ArrayList<WishPlace>): RecyclerView.Adapter<WishPlaceRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): WishPlaceRVAdapter.ViewHolder {
        val binding: ItemWishPlaceBinding = ItemWishPlaceBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishPlaceRVAdapter.ViewHolder, position: Int) {
        holder.bind(placeList[position])
    }

    override fun getItemCount(): Int = placeList.size

    inner class ViewHolder(val binding: ItemWishPlaceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(place: WishPlace) {
            binding.placeName.text = place.placeName
            binding.placeType.text = place.placeType
            binding.placeDistance.text = place.placeDistance
            binding.placeLocation.text = place.placeLocation
            binding.placeCall.text = place.placeCall
            binding.placeChar122.text = place.placeChar1
            binding.placeImg.setImageResource(place.placeImg!!)
            binding.placeReview.text = place.placeReview
        }
    }

}