package com.example.dogcatsquare.ui.wish

import android.graphics.Color
import android.icu.text.DecimalFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.WishRetrofitObj
import com.example.dogcatsquare.data.model.wish.FetchMyWishPlaceResponse
import com.example.dogcatsquare.data.model.wish.WishPlace
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ItemWishPlaceBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WishPlaceRVAdapter(private val placeList: ArrayList<WishPlace>, private val bearer_token: String?): RecyclerView.Adapter<WishPlaceRVAdapter.ViewHolder>() {
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

            Glide.with(itemView.context)
                .load(place.imgUrl)
                .placeholder(R.drawable.ic_profile_default)
                .into(binding.placeImg)

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

        val wishService = RetrofitObj.getRetrofit().create(WishRetrofitObj::class.java)
        wishService.fetchMyWishPlaceList(token, place.id).enqueue(object :
            Callback<FetchMyWishPlaceResponse> {
            override fun onResponse(
                call: Call<FetchMyWishPlaceResponse>,
                response: Response<FetchMyWishPlaceResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()?.result ?: false
                    if (!result) { // ❌ result == false 이면 삭제
                        placeList.removeAt(position)
                        notifyItemRemoved(position)
                        Log.d("WishPlaceRVAdapter", "위시 삭제 성공: ${place.name}")
                    }
                } else {
                    Log.e("WishPlaceRVAdapter", "위시 변경 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(
                call: Call<FetchMyWishPlaceResponse>,
                t: Throwable
            ) {
                Log.e("WishPlaceRVAdapter", "위시 변경 실패", t)
            }
        })
    }
}