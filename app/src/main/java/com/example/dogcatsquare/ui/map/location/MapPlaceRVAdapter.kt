package com.example.dogcatsquare.ui.map.location

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.data.map.MapPlace
import com.example.dogcatsquare.databinding.ItemMapPlaceBinding

class MapPlaceRVAdapter(private val placeList: ArrayList<MapPlace>, private val listener: OnItemClickListener? = null): RecyclerView.Adapter<MapPlaceRVAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(place: MapPlace)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMapPlaceBinding = ItemMapPlaceBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(placeList[position])
    }

    override fun getItemCount(): Int = placeList.size

    fun updateList(newList: List<MapPlace>) {
        placeList.clear()
        placeList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMapPlaceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(place: MapPlace) {
            binding.placeName.text = place.placeName
            binding.placeType.text = place.placeType
            binding.placeDistance.text = place.placeDistance
            binding.placeLocation.text = place.placeLocation
            binding.placeCall.text = place.placeCall
            binding.placeImg.setImageResource(place.placeImg!!)

            // placeReview가 null인 경우 관련 뷰들을 숨김
            if (place.placeReview == null) {
                binding.review.visibility = View.GONE
                binding.placeReview.visibility = View.GONE

                //char1 위치 조정
                (binding.char1.layoutParams as ConstraintLayout.LayoutParams).apply {
                    startToStart = binding.guideline1.id
                    topToBottom = binding.call.id
                    bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                    marginStart = 0
                    topMargin = (8 * binding.root.context.resources.displayMetrics.density).toInt()
                }
                binding.char1.requestLayout()
            } else {
                binding.review.visibility = View.VISIBLE
                binding.placeReview.visibility = View.VISIBLE
                binding.placeReview.text = place.placeReview
            }

            // char1, char2, char3 표시 여부 설정
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

            // placeType에 따라 보이는 char들의 배경색과 텍스트 색상 설정
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
                "식당" -> {
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
                "카페" -> {
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

            // 아이템 클릭 리스너 설정
            itemView.setOnClickListener {
                listener?.onItemClick(place)
            }
        }
    }

}