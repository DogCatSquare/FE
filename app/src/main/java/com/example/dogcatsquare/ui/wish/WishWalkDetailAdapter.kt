package com.example.dogcatsquare.ui.wish

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.ItemWishWalkDetailBinding

data class WalkDetailItem(
    val userName: String,
    val petType: String,
    val walkDistance: String,
    val walkTime: String,
    val walkText: String,
    val walkDate: String
)

class WishWalkDetailAdapter : RecyclerView.Adapter<WishWalkDetailAdapter.DetailViewHolder>() {
    private var details = mutableListOf<WalkDetailItem>()

    inner class DetailViewHolder(private val binding: ItemWishWalkDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WalkDetailItem) {
            binding.apply {
                userName.text = item.userName
                petType.text = item.petType
                walkDistance.text = item.walkDistance
                walkTime.text = item.walkTime
                walkText.text = item.walkText
                walkDate.text = item.walkDate
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemWishWalkDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(details[position])
    }

    override fun getItemCount() = details.size

    fun submitList(newDetails: List<WalkDetailItem>) {
        details.clear()
        details.addAll(newDetails)
        notifyDataSetChanged()
    }
}