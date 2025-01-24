package com.example.dogcatsquare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.example.dogcatsquare.databinding.ItemWishWalkBinding

data class WalkItem(
    val placeName: String,
    val placeDistance: String,
    val placeLocation: String,
    val details: List<WalkDetailItem>
)

class WishWalkAdapter : RecyclerView.Adapter<WishWalkAdapter.ViewHolder>() {
    private var walkList = mutableListOf<WalkItem>()

    inner class ViewHolder(private val binding: ItemWishWalkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isExpanded = false
        private val detailAdapter = WishWalkDetailAdapter()

        init {
            binding.detailsRecyclerView.apply {
                adapter = detailAdapter
                layoutManager = LinearLayoutManager(context)
            }

            binding.expandButton.setOnClickListener {
                isExpanded = !isExpanded
                toggleExpansion(binding, isExpanded)
            }
        }

        private fun toggleExpansion(binding: ItemWishWalkBinding, isExpanded: Boolean) {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
            binding.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.expandButton.rotation = if (isExpanded) 90f else -90f
        }

        fun bind(item: WalkItem) {
            binding.apply {
                placeName.text = item.placeName
                placeDistance.text = item.placeDistance
                placeLocation.text = item.placeLocation

                if (item.details.size >= 2) {
                    // 이미지와 카운트 표시
                    imageView20.visibility = View.VISIBLE
                    itemCount.visibility = View.VISIBLE
                    itemCount.text = "+ ${item.details.size - 1}"

                    // 이미지 크기 조정
                    val newSize = itemView.dpToPx(43)

                    // placeImg의 왼쪽 마진을 8dp로 변경
                    placeImg.layoutParams = (placeImg.layoutParams as ConstraintLayout.LayoutParams).apply {
                        width = newSize
                        height = newSize
                        marginStart = itemView.dpToPx(13)  // 8dp로 변경
                    }

                    imageView20.layoutParams = (imageView20.layoutParams as ConstraintLayout.LayoutParams).apply {
                        width = newSize
                        height = newSize
                    }
                } else {
                    // 이미지와 카운트 숨기기
                    imageView20.visibility = View.GONE
                    itemCount.visibility = View.GONE

                    // 원래 크기와 마진으로 복원
                    placeImg.layoutParams = (placeImg.layoutParams as ConstraintLayout.LayoutParams).apply {
                        width = itemView.dpToPx(48)
                        height = itemView.dpToPx(48)
                        marginStart = itemView.dpToPx(16)  // 16dp로 복원
                    }
                }
            }
            detailAdapter.submitList(item.details)
        }

        private fun View.dpToPx(dp: Int): Int {
            val scale = resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWishWalkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(walkList[position])
    }

    override fun getItemCount() = walkList.size

    fun submitList(list: List<WalkItem>) {
        walkList.clear()
        walkList.addAll(list)
        notifyDataSetChanged()
    }
}