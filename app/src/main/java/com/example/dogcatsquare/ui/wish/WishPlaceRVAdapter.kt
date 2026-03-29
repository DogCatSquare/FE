package com.example.dogcatsquare.ui.wish

import android.content.Context
import android.graphics.Color
import android.icu.text.DecimalFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.WishRetrofitObj
import com.example.dogcatsquare.data.model.wish.FetchMyWishPlaceResponse
import com.example.dogcatsquare.data.model.wish.WishPlace
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ItemWishPlaceBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// PlaceType enum class 추가
enum class PlaceType(val value: String, @DrawableRes val defaultImage: Int) {
    HOSPITAL("동물병원", R.drawable.img_hospital_dafault),
    HOTEL("호텔", R.drawable.img_hotel_dafault),
    PARK("산책로", R.drawable.img_walk_default),
    RESTAURANT("식당", R.drawable.img_cafe_default),
    CAFE("카페", R.drawable.img_cafe_default),
    ETC("기타", R.drawable.img_etc_default),
    UNKNOWN("", R.drawable.ic_profile_default);

    companion object {
        fun fromString(value: String?): PlaceType {
            return values().find { it.value == value } ?: UNKNOWN
        }
    }
}

class WishPlaceRVAdapter(private val context: Context, private val placeList: ArrayList<WishPlace>, private val bearer_token: String?): RecyclerView.Adapter<WishPlaceRVAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(place: WishPlace)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemWishPlaceBinding = ItemWishPlaceBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(placeList[position])
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(placeList[position])
        }
    }

    override fun getItemCount(): Int = placeList.size

    inner class ViewHolder(val binding: ItemWishPlaceBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(place: WishPlace) {
            binding.placeName.text = place.name
            binding.placeType.text = place.category

            val decimalFormat = DecimalFormat("#.##")
            val formattedDistance = decimalFormat.format(place.distance)
            binding.placeDistance.text = "${formattedDistance}km"
            binding.placeLocation.text = place.address
            binding.placeCall.text = place.phoneNumber

            // 카테고리별 기본 이미지 설정
            val defaultImageRes = com.example.dogcatsquare.ui.map.location.PlaceType.fromString(place.category).defaultImage

            // 이미지 처리
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

            binding.placeReview.text = "리뷰(${place.reviewCount})"

            // 🔹 키워드 리스트에서 최대 3개까지 설정
            val keywords = place.keywords ?: emptyList()
            val charViews = listOf(binding.char1, binding.char2, binding.char3)
            val charTextViews = listOf(binding.char1Text, binding.char2Text, binding.char3Text)

            // 모든 키워드 View를 초기화 (숨김)
            charViews.forEach { it.visibility = View.GONE }

            // 키워드 개수만큼 보여주기 (최대 3개)
            for (i in keywords.indices.take(3)) {
                charViews[i].visibility = View.VISIBLE
                charTextViews[i].text = keywords[i]
            }

            // 🔹 placeType에 따른 배경색 & 텍스트 색상 설정
            val (bgColor, textColor) = when (place.category) {
                "동물병원" -> Pair("#EAF2FE", "#276CCB")
                "호텔" -> Pair("#FEEEEA", "#F36037")
                "산책로" -> Pair("#F4FCF5", "#3E7C43")
                "식당", "카페" -> Pair("#FFFBF1", "#FF8D41")
                else -> Pair("#FFFFFF", "#000000") // 기본값
            }

            // 적용
            for (i in keywords.indices.take(3)) {
                charViews[i].setCardBackgroundColor(Color.parseColor(bgColor))
                charTextViews[i].setTextColor(Color.parseColor(textColor))
            }

            if (place.phoneNumber == "") {
                binding.call.visibility = View.GONE
                binding.placeCall.visibility = View.GONE
            } else {
                binding.call.visibility = View.VISIBLE
                binding.placeCall.visibility = View.VISIBLE
            }

            // 🔹 placeReview가 null이면 숨김
            if (place.reviewCount == 0) {
                binding.review2.visibility = View.GONE
                binding.placeReview.visibility = View.GONE
            } else {
                binding.review2.visibility = View.VISIBLE
                binding.placeReview.visibility = View.VISIBLE
            }

            binding.wishIv.setOnClickListener {
                 toggleWishStatus(place, adapterPosition)
            }
        }
    }

    private fun toggleWishStatus(place: WishPlace, position: Int) {
        val token = "Bearer $bearer_token"

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            try {
                val response = com.example.dogcatsquare.data.network.RetrofitClient.placesApiService.toggleWish(token, place.googlePlaceId)
                if (response.isSuccess) {
                    // 위시리스트 アイ템 삭제
                    val actualPos = placeList.indexOf(place)
                    if (actualPos != -1) {
                        placeList.removeAt(actualPos)
                        notifyItemRemoved(actualPos)
                        android.widget.Toast.makeText(context, "위시리스트에서 제외되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                        Log.d("WishPlaceRVAdapter", "위시 삭제 성공: ${place.name}")
                    }
                } else {
                    android.widget.Toast.makeText(context, response.message ?: "위시 해제 실패", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("WishPlaceRVAdapter", "위시 변경 에러", e)
                android.widget.Toast.makeText(context, "네트워크 오류", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}