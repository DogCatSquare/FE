package com.example.dogcatsquare.ui.mypage

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.ReviewRetrofitItf
import com.example.dogcatsquare.data.map.DeleteReviewResponse
import com.example.dogcatsquare.data.model.mypage.ReviewContent
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ItemMyReviewBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyReviewRVAdapter(private val myReviewList: ArrayList<ReviewContent>, private val onDeleteReview: (DeleteReviewParams) -> Unit) : RecyclerView.Adapter<MyReviewRVAdapter.MyReviewAdapterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewAdapterViewHolder {
        val binding = ItemMyReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyReviewAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyReviewAdapterViewHolder, position: Int) {
        val myReview = myReviewList[position]
        holder.bind(myReview)
    }

    override fun getItemCount(): Int = myReviewList.size

    inner class MyReviewAdapterViewHolder(val binding: ItemMyReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(myReview: ReviewContent) {
            binding.placeName.text = myReview.title
            binding.reviewText.text = myReview.content
            binding.reviewDate.text = myReview.createdAt
            Glide.with(itemView.context)
                .load(myReview.imageUrls)
                .placeholder(R.drawable.ic_profile_default)
                .into(binding.reviewImg)

            // 설정 클릭(수정, 삭제)
            binding.settingIv.setOnClickListener {
                showPopupMenu(it, itemView.context, myReview.id, myReview.walkId, myReview.placeId)
            }
        }
    }

    private fun showPopupMenu(view: View, myReview: Context, id: Int, walkId: Int?, placeId: Int?) {
        val popup = PopupMenu(myReview, view)
        popup.menuInflater.inflate(R.menu.review_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.review_delete -> {
                    onDeleteReview(DeleteReviewParams(id, placeId, walkId))
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}

data class DeleteReviewParams(
    val reviewId: Int,
    val placeId: Int?,
    val walkId: Int?
)