package com.example.dogcatsquare.ui.wish

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.data.wish.WishPlace
import com.example.dogcatsquare.databinding.ItemWishPlaceBinding

class WishPlaceRVAdapter(private val placeList: ArrayList<WishPlace>): RecyclerView.Adapter<WishPlaceRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemWishPlaceBinding = ItemWishPlaceBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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
            binding.placeImg.setImageResource(place.placeImg!!)
            binding.placeReview.text = place.placeReview

            // char1, char2, char3의 visibility 설정
            if (place.char1Text == null) {
                binding.char1.visibility = View.GONE
            } else {
                binding.char1.visibility = View.VISIBLE
                binding.char1Text.text = place.char1Text
            }

            if (place.char2Text == null) {
                binding.char2.visibility = View.GONE
            } else {
                binding.char2.visibility = View.VISIBLE
                binding.char2Text.text = place.char2Text
            }

            if (place.char3Text == null) {
                binding.char3.visibility = View.GONE
            } else {
                binding.char3.visibility = View.VISIBLE
                binding.char3Text.text = place.char3Text
            }

            // placeType에 따라 char들의 배경색과 텍스트 색상 설정
            when (place.placeType) {
                "동물병원" -> {
                    if (place.char1Text != null) {
                        binding.char1.setCardBackgroundColor(Color.parseColor("#EAF2FE"))
                        binding.char1Text.setTextColor(Color.parseColor("#276CCB"))
                    }
                    if (place.char2Text != null) {
                        binding.char2.setCardBackgroundColor(Color.parseColor("#EAF2FE"))
                        binding.char2Text.setTextColor(Color.parseColor("#276CCB"))
                    }
                    if (place.char3Text != null) {
                        binding.char3.setCardBackgroundColor(Color.parseColor("#EAF2FE"))
                        binding.char3Text.setTextColor(Color.parseColor("#276CCB"))
                    }
                }
                "호텔" -> {
                    if (place.char1Text != null) {
                        binding.char1.setCardBackgroundColor(Color.parseColor("#FEEEEA"))
                        binding.char1Text.setTextColor(Color.parseColor("#F36037"))
                    }
                    if (place.char2Text != null) {
                        binding.char2.setCardBackgroundColor(Color.parseColor("#FEEEEA"))
                        binding.char2Text.setTextColor(Color.parseColor("#F36037"))
                    }
                    if (place.char3Text != null) {
                        binding.char3.setCardBackgroundColor(Color.parseColor("#FEEEEA"))
                        binding.char3Text.setTextColor(Color.parseColor("#F36037"))
                    }
                }
                "산책로" -> {
                    if (place.char1Text != null) {
                        binding.char1.setCardBackgroundColor(Color.parseColor("#F4FCF5"))
                        binding.char1Text.setTextColor(Color.parseColor("#3E7C43"))
                    }
                    if (place.char2Text != null) {
                        binding.char2.setCardBackgroundColor(Color.parseColor("#F4FCF5"))
                        binding.char2Text.setTextColor(Color.parseColor("#3E7C43"))
                    }
                    if (place.char3Text != null) {
                        binding.char3.setCardBackgroundColor(Color.parseColor("#F4FCF5"))
                        binding.char3Text.setTextColor(Color.parseColor("#3E7C43"))
                    }
                }
                "식당", "카페" -> {
                    if (place.char1Text != null) {
                        binding.char1.setCardBackgroundColor(Color.parseColor("#FFFBF1"))
                        binding.char1Text.setTextColor(Color.parseColor("#FF8D41"))
                    }
                    if (place.char2Text != null) {
                        binding.char2.setCardBackgroundColor(Color.parseColor("#FFFBF1"))
                        binding.char2Text.setTextColor(Color.parseColor("#FF8D41"))
                    }
                    if (place.char3Text != null) {
                        binding.char3.setCardBackgroundColor(Color.parseColor("#FFFBF1"))
                        binding.char3Text.setTextColor(Color.parseColor("#FF8D41"))
                    }
                }
            }

            // placeReview가 null인 경우 관련 뷰들을 숨김
            if (place.placeReview == null) {
                binding.review2.visibility = View.GONE
                binding.placeReview.visibility = View.GONE
            } else {
                binding.review2.visibility = View.VISIBLE
                binding.placeReview.visibility = View.VISIBLE
                binding.placeReview.text = place.placeReview
            }
        }
    }
}