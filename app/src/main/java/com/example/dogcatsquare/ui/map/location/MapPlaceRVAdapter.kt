package com.example.dogcatsquare.ui.map.location

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.dogcatsquare.R
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
        Log.d("MapPlaceRVAdapter", "리스트 업데이트: ${placeList.size}개 항목")
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemMapPlaceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(place: MapPlace) {
            binding.placeName.text = place.placeName
            binding.placeType.text = place.placeType
            binding.placeDistance.text = place.placeDistance
            binding.placeLocation.text = place.placeLocation
            binding.placeCall.text = place.placeCall

            // 이미지 처리 수정
            if (place.placeImgUrl != null) {
                // 이미지 URL이 있는 경우 Glide 등을 사용해 이미지 로드
                 Glide.with(binding.placeImg.context)
                     .load(place.placeImgUrl)
                     .override(300, 300)
                     .transform(
                         MultiTransformation(
                             CenterCrop(),
                             RoundedCorners((8 * binding.root.resources.displayMetrics.density).toInt())
                         )
                     )
                     .placeholder(R.drawable.ic_place_img_default)
                     .into(binding.placeImg)
            } else {
                binding.placeImg.setImageResource(R.drawable.ic_place_img_default)
            }

            if (place.placeCall == "0" || place.placeCall == null) {
                // call 아이콘과 placeCall 텍스트뷰 숨기기
                binding.call.visibility = View.GONE
                binding.placeCall.visibility = View.GONE

                // reviewCount가 0일 때의 로직
                if (place.reviewCount == 0 || place.placeCall == null) {
                    binding.ImageView.visibility = View.GONE
                    binding.placeReview.visibility = View.GONE

                    //char1 위치 조정
                    (binding.char1.layoutParams as ConstraintLayout.LayoutParams).apply {
                        startToStart = binding.guideline1.id
                        topToBottom = binding.placeLocation.id  // call이 없으므로 placeLocation 기준으로 변경
                        bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                        marginStart = 0
                        topMargin = (8 * binding.root.context.resources.displayMetrics.density).toInt()
                    }
                    binding.char1.requestLayout()
                } else {
                    binding.ImageView.visibility = View.VISIBLE
                    binding.placeReview.visibility = View.VISIBLE
                    binding.placeReview.text = "리뷰(${place.reviewCount})"

                    // char1을 원래 위치로 복원 (단, topToBottom은 placeLocation 기준)
                    (binding.char1.layoutParams as ConstraintLayout.LayoutParams).apply {
                        startToStart = ConstraintLayout.LayoutParams.UNSET
                        startToEnd = binding.placeReview.id
                        topToBottom = binding.placeLocation.id  // call이 없으므로 placeLocation 기준으로 변경
                        bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                        marginStart = (8 * binding.root.context.resources.displayMetrics.density).toInt()
                        topMargin = (5 * binding.root.context.resources.displayMetrics.density).toInt()
                    }
                    binding.char1.requestLayout()
                }
            } else {
                // call 아이콘과 placeCall 텍스트뷰 보이기
                binding.call.visibility = View.VISIBLE
                binding.placeCall.visibility = View.VISIBLE
                binding.placeCall.text = place.placeCall

                // reviewCount에 따른 기존 로직
                if (place.reviewCount == 0) {
                    binding.ImageView.visibility = View.GONE
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
                    binding.ImageView.visibility = View.VISIBLE
                    binding.placeReview.visibility = View.VISIBLE
                    binding.placeReview.text = "리뷰(${place.reviewCount})"

                    // char1을 원래 위치로 복원
                    (binding.char1.layoutParams as ConstraintLayout.LayoutParams).apply {
                        startToStart = ConstraintLayout.LayoutParams.UNSET
                        startToEnd = binding.placeReview.id
                        topToBottom = binding.call.id
                        bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                        marginStart = (8 * binding.root.context.resources.displayMetrics.density).toInt()
                        topMargin = (5 * binding.root.context.resources.displayMetrics.density).toInt()
                    }
                    binding.char1.requestLayout()
                }
            }

            // placeReview가 null인 경우 관련 뷰들을 숨김
            if (place.reviewCount == 0 || place.reviewCount == null) {
                binding.ImageView.visibility = View.GONE
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
                binding.ImageView.visibility = View.VISIBLE
                binding.placeReview.visibility = View.VISIBLE
                binding.placeReview.text = "리뷰(${place.reviewCount})"

                // char1을 원래 위치로 복원
                (binding.char1.layoutParams as ConstraintLayout.LayoutParams).apply {
                    startToStart = ConstraintLayout.LayoutParams.UNSET  // 기존 제약 제거
                    startToEnd = binding.placeReview.id  // placeReview 우측에 위치
                    topToBottom = binding.placeCall.id
                    bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                    marginStart = (8 * binding.root.context.resources.displayMetrics.density).toInt()
                    topMargin = (5 * binding.root.context.resources.displayMetrics.density).toInt()
                }
                binding.char1.requestLayout()
            }

            // isOpen 표시 설정
            if (place.isOpen == null) {
                binding.char1.visibility = View.GONE
            } else {
                binding.char1.visibility = View.VISIBLE
                binding.char1Text.text = place.isOpen
            }

            // placeType에 따라 보이는 영업상태의 배경색과 텍스트 색상 설정
            when (place.placeType) {
                "동물병원" -> {
                    if (place.isOpen != null) {
                        binding.char1.setCardBackgroundColor(Color.parseColor("#EAF2FE"))
                        binding.char1Text.setTextColor(Color.parseColor("#276CCB"))
                    }
                }
                "호텔" -> {
                    if (place.isOpen != null) {
                        binding.char1.setCardBackgroundColor(Color.parseColor("#FEEEEA"))
                        binding.char1Text.setTextColor(Color.parseColor("#F36037"))
                    }
                }
                "산책로" -> {
                    if (place.isOpen != null) {
                        binding.char1.setCardBackgroundColor(Color.parseColor("#F4FCF5"))
                        binding.char1Text.setTextColor(Color.parseColor("#3E7C43"))
                    }
                }
                "식당", "카페" -> {
                    if (place.isOpen != null) {
                        binding.char1.setCardBackgroundColor(Color.parseColor("#FFFBF1"))
                        binding.char1Text.setTextColor(Color.parseColor("#FF8D41"))
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