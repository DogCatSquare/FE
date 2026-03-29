package com.example.dogcatsquare.ui.wish

import android.content.Context
import android.icu.text.DecimalFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.wish.WishPlace
import com.example.dogcatsquare.databinding.ItemWishWalkBinding
import com.example.dogcatsquare.ui.map.location.PlaceType

class WishWalkAdapter(
    private val walkList: ArrayList<WishPlace>,
    private val bearer_token: String?,
    private val context: Context
) : RecyclerView.Adapter<WishWalkAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(place: WishPlace)
    }

    private lateinit var mItemClickListener: OnItemClickListener
    private val expandedPositionSet = mutableSetOf<Int>()

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemWishWalkBinding =
            ItemWishWalkBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(walkList[position], position)
    }

    override fun getItemCount(): Int = walkList.size

    inner class ViewHolder(val binding: ItemWishWalkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(place: WishPlace, position: Int) {
            binding.placeName.text = place.name

            val decimalFormat = DecimalFormat("#.##")
            val formattedDistance = decimalFormat.format(place.distance)
            binding.placeDistance.text = "${formattedDistance}km"
            binding.placeLocation.text = place.address

            // 카테고리별 기본 이미지 설정
            val defaultImageRes = PlaceType.fromString(place.category).defaultImage

            // 이미지 처리 - 첫번째 유저 프로필이나 디폴트 세팅
            if (place.imgUrl != null) {
                Glide.with(binding.placeImg.context)
                    .load(place.imgUrl)
                    .override(300, 300)
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners((8 * binding.root.resources.displayMetrics.density).toInt())
                        )
                    )
                    .placeholder(defaultImageRes)
                    .error(defaultImageRes)
                    .into(binding.placeImg)
            } else {
                binding.placeImg.setImageResource(defaultImageRes)
            }

            // Expand / Collapse State
            val isExpanded = expandedPositionSet.contains(position)
            binding.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivExpand.setImageResource(if (isExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down)

            // Child Adapter
            if (!place.walks.isNullOrEmpty()) {
                val detailAdapter = WishWalkDetailAdapter(bearer_token, context) { removedWalk ->
                    // Remove the walk from the list and update UI
                    val updatedWalks = place.walks!!.filter { it.walkId != removedWalk.walkId }
                    place.walks = updatedWalks
                    notifyItemChanged(adapterPosition)
                }
                binding.detailsRecyclerView.layoutManager = LinearLayoutManager(context)
                binding.detailsRecyclerView.adapter = detailAdapter
                detailAdapter.submitList(place.walks!!)
            } else {
                binding.detailsRecyclerView.adapter = null
            }

            // Click Listeners
            binding.ivExpand.setOnClickListener {
                if (expandedPositionSet.contains(position)) {
                    expandedPositionSet.remove(position)
                } else {
                    expandedPositionSet.add(position)
                }
                notifyItemChanged(position)
            }
            
            // Allow clicking the item view to expand as well, or go to map. Using map nav for now.
            binding.root.setOnClickListener {
                if (::mItemClickListener.isInitialized) {
                    mItemClickListener.onItemClick(place)
                }
            }
        }
    }
}