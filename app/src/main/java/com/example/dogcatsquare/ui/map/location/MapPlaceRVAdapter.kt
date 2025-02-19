package com.example.dogcatsquare.ui.map.location

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.map.MapPlace
import com.example.dogcatsquare.databinding.ItemMapPlaceBinding

// PlaceType enum class 추가
enum class PlaceType(val value: String, @DrawableRes val defaultImage: Int) {
    HOSPITAL("동물병원", R.drawable.img_hospital_dafault),
    HOTEL("호텔", R.drawable.img_hotel_dafault),
    PARK("산책로", R.drawable.img_walk_default),
    RESTAURANT("식당", R.drawable.img_cafe_default),
    CAFE("카페", R.drawable.img_cafe_default),
    UNKNOWN("", R.drawable.ic_place_img_default);

    companion object {
        fun fromString(value: String?): PlaceType {
            return values().find { it.value == value } ?: UNKNOWN
        }
    }
}

class MapPlaceRVAdapter(
    private val placeList: ArrayList<MapPlace>,
    private val listener: OnItemClickListener? = null
): RecyclerView.Adapter<MapPlaceRVAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(place: MapPlace)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMapPlaceBinding = ItemMapPlaceBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
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

            // 카테고리별 기본 이미지 설정
            val defaultImageRes = PlaceType.fromString(place.placeType).defaultImage

            // 이미지 처리
            if (place.placeImgUrl != null) {
                Glide.with(binding.placeImg.context)
                    .load(place.placeImgUrl)
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

            // 전화번호 관련 처리
            if (place.placeCall == "0" || place.placeCall == null) {
                binding.call.visibility = View.GONE
                binding.placeCall.visibility = View.GONE
            } else {
                binding.call.visibility = View.VISIBLE
                binding.placeCall.visibility = View.VISIBLE
                binding.placeCall.text = place.placeCall
            }

            // 리뷰 관련 처리
            if (place.reviewCount == 0 || place.reviewCount == null) {
                binding.review2.visibility = View.GONE
                binding.placeReview.visibility = View.GONE
            } else {
                binding.review2.visibility = View.VISIBLE
                binding.placeReview.visibility = View.VISIBLE
                binding.placeReview.text = "리뷰(${place.reviewCount})"
            }

            // keywords 처리
            place.keywords?.let { keywords ->
                // char1 처리
                if (keywords.isNotEmpty()) {
                    binding.char1.visibility = View.VISIBLE
                    binding.char1Text.text = keywords[0]
                } else {
                    binding.char1.visibility = View.GONE
                }

                // char2 처리
                if (keywords.size > 1) {
                    binding.char2.visibility = View.VISIBLE
                    binding.char2Text.text = keywords[1]
                } else {
                    binding.char2.visibility = View.GONE
                }

                // char3 처리
                if (keywords.size > 2) {
                    binding.char3.visibility = View.VISIBLE
                    binding.char3Text.text = keywords[2]
                } else {
                    binding.char3.visibility = View.GONE
                }
            } ?: run {
                // keywords가 null인 경우 모든 char 숨기기
                binding.char1.visibility = View.GONE
                binding.char2.visibility = View.GONE
                binding.char3.visibility = View.GONE
            }

            // FlexboxLayout의 마진 설정
            val flexLP = binding.characteristicsContainer.layoutParams as ViewGroup.MarginLayoutParams
            flexLP.topMargin = if (place.placeCall == "0" || place.placeCall == null) {
                (8 * binding.root.resources.displayMetrics.density).toInt()
            } else {
                (5 * binding.root.resources.displayMetrics.density).toInt()
            }
            binding.characteristicsContainer.layoutParams = flexLP

            val hasReview = place.reviewCount != null && place.reviewCount > 0
            val hasKeywords = !place.keywords.isNullOrEmpty()

            binding.characteristicsContainer.visibility = if (hasReview || hasKeywords) View.VISIBLE else View.GONE

            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.root as ConstraintLayout)

            if (hasReview || hasKeywords) {
                constraintSet.connect(
                    R.id.contour2,
                    ConstraintSet.TOP,
                    R.id.characteristicsContainer,
                    ConstraintSet.BOTTOM,
                    (15 * binding.root.resources.displayMetrics.density).toInt()
                )
            } else {
                constraintSet.connect(
                    R.id.contour2,
                    ConstraintSet.TOP,
                    R.id.placeImg,
                    ConstraintSet.BOTTOM,
                    (15 * binding.root.resources.displayMetrics.density).toInt()
                )
            }

            constraintSet.applyTo(binding.root as ConstraintLayout)

            // placeType에 따른 특성 카드 스타일 설정
            when (place.placeType) {
                "동물병원" -> setCardsStyle("#EAF2FE", "#276CCB")
                "호텔" -> setCardsStyle("#FEEEEA", "#F36037")
                "산책로" -> setCardsStyle("#F4FCF5", "#3E7C43")
                "식당", "카페" -> setCardsStyle("#FFFBF1", "#FF8D41")
            }

            // 아이템 클릭 리스너 설정
            itemView.setOnClickListener {
                listener?.onItemClick(place)
            }
        }

        private fun setCardsStyle(backgroundColor: String, textColor: String) {
            binding.apply {
                char1.setCardBackgroundColor(Color.parseColor(backgroundColor))
                char1Text.setTextColor(Color.parseColor(textColor))
                char2.setCardBackgroundColor(Color.parseColor(backgroundColor))
                char2Text.setTextColor(Color.parseColor(textColor))
                char3.setCardBackgroundColor(Color.parseColor(backgroundColor))
                char3Text.setTextColor(Color.parseColor(textColor))
            }
        }
    }
}