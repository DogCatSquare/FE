package com.example.dogcatsquare.ui.wish

import android.graphics.Color
import android.icu.text.DecimalFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.WishRetrofitObj
import com.example.dogcatsquare.data.model.wish.FetchMyWishPlaceResponse
import com.example.dogcatsquare.data.model.wish.WishPlace
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ItemWishPlaceBinding
import com.example.dogcatsquare.databinding.ItemWishWalkBinding
import com.example.dogcatsquare.ui.wish.WishPlaceRVAdapter.ViewHolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class WalkItem(
    val placeName: String,
    val placeDistance: String,
    val placeLocation: String,
    val details: List<WalkDetailItem>
)

class WishWalkAdapter(private val walkList: ArrayList<WishPlace>, private val bearer_token: String?) : RecyclerView.Adapter<WishWalkAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(place: WishPlace)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemWishWalkBinding =
            ItemWishWalkBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(walkList[position])
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(walkList[position])
        }
    }

    override fun getItemCount(): Int = walkList.size

    inner class ViewHolder(val binding: ItemWishWalkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(place: WishPlace) {
            binding.placeName.text = place.name

            val decimalFormat = DecimalFormat("#.##")
            val formattedDistance = decimalFormat.format(place.distance)
            binding.placeDistance.text = "${formattedDistance}km"
            binding.placeLocation.text = place.address

            Glide.with(itemView.context)
                .load(place.imgUrl)
                .placeholder(R.drawable.ic_profile_default)
                .into(binding.placeImg)

            binding.ivWish.setOnClickListener {
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
                        walkList.removeAt(position)
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