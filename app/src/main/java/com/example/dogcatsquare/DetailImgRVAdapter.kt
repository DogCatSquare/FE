package com.example.dogcatsquare

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.ItemDetailImgBinding
import com.example.dogcatsquare.databinding.ItemMapButtonBinding

class DetailImgRVAdapter(private val imgList: ArrayList<DetailImg>): RecyclerView.Adapter<DetailImgRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DetailImgRVAdapter.ViewHolder {
        val binding: ItemDetailImgBinding = ItemDetailImgBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailImgRVAdapter.ViewHolder, position: Int) {
        holder.bind(imgList[position])
    }

    override fun getItemCount(): Int = imgList.size

    inner class ViewHolder(val binding: ItemDetailImgBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(img: DetailImg) {
            binding.detailImg.setImageResource(img.detailImg!!)
        }
    }

}