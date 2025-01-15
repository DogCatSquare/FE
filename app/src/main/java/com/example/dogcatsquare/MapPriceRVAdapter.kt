package com.example.dogcatsquare

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.ItemDetailImgBinding
import com.example.dogcatsquare.databinding.ItemMapPriceBinding

class MapPriceRVAdapter(private val priceList: ArrayList<MapPrice>): RecyclerView.Adapter<MapPriceRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MapPriceRVAdapter.ViewHolder {
        val binding: ItemMapPriceBinding = ItemMapPriceBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MapPriceRVAdapter.ViewHolder, position: Int) {
        holder.bind(priceList[position])
    }

    override fun getItemCount(): Int = priceList.size

    inner class ViewHolder(val binding: ItemMapPriceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(price: MapPrice) {
            binding.priceName.text = price.priceName
            binding.price.text = price.price
        }
    }

}