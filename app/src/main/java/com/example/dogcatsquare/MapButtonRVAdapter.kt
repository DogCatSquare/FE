package com.example.dogcatsquare

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.ItemMapButtonBinding

class MapButtonRVAdapter(private val buttonList: ArrayList<MapButton>): RecyclerView.Adapter<MapButtonRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MapButtonRVAdapter.ViewHolder {
        val binding: ItemMapButtonBinding = ItemMapButtonBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MapButtonRVAdapter.ViewHolder, position: Int) {
        holder.bind(buttonList[position])
    }

    override fun getItemCount(): Int = buttonList.size

    inner class ViewHolder(val binding: ItemMapButtonBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(button: MapButton) {
            binding.buttonName.text = button.buttonName
        }
    }

}