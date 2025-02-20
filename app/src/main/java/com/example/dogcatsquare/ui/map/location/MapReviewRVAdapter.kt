package com.example.dogcatsquare.ui.map.location

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.data.map.MapReview
import com.example.dogcatsquare.databinding.ItemMapReviewBinding
import com.example.dogcatsquare.databinding.ItemMapReviewMultipleBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapReviewRVAdapter(
    private val reviewList: ArrayList<MapReview>,
    private val currentUserNickname: String = "",
    private val onReviewDeleted: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SINGLE_IMAGE_TYPE = 0
    private val MULTIPLE_IMAGES_TYPE = 1

    override fun getItemViewType(position: Int): Int {
        return if (reviewList[position].placeReviewImageUrl?.size ?: 0 <= 1) {
            SINGLE_IMAGE_TYPE
        } else {
            MULTIPLE_IMAGES_TYPE
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SINGLE_IMAGE_TYPE -> {
                val binding = ItemMapReviewBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
                SingleImageViewHolder(binding)
            }
            else -> {
                val binding = ItemMapReviewMultipleBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
                MultipleImagesViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SingleImageViewHolder -> holder.bind(reviewList[position])
            is MultipleImagesViewHolder -> holder.bind(reviewList[position])
        }
    }

    override fun getItemCount(): Int = reviewList.size

    inner class SingleImageViewHolder(private val binding: ItemMapReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: MapReview) {
            // 프로필 이미지 설정
            if (!review.userImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(review.userImageUrl)
                    .fallback(R.drawable.ic_profile_img_default)
                    .error(R.drawable.ic_profile_img_default)
                    .circleCrop()
                    .into(binding.reviewProfileImg)
            } else {
                binding.reviewProfileImg.setImageResource(R.drawable.ic_profile_img_default)
            }

            // 텍스트 정보 설정
            binding.reviewName.text = review.nickname ?: "알 수 없음"
            binding.petType.text = review.breed ?: ""
            binding.reviewText.text = review.content ?: ""
            binding.reviewDate.text = review.createdAt?.split("T")?.get(0)?.replace("-", ".") ?: ""

            // 단일 이미지 설정
            if (!review.placeReviewImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(review.placeReviewImageUrl.first())
                    .fallback(R.drawable.ic_place_img_default)
                    .error(R.drawable.ic_place_img_default)
                    .centerCrop()
                    .override(210, 210)
                    .into(binding.reviewImg)
            } else {
                binding.reviewImg.setImageResource(R.drawable.ic_place_img_default)
            }

            setupEtcButton(binding.etcButton, review)
        }
    }

    inner class MultipleImagesViewHolder(private val binding: ItemMapReviewMultipleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: MapReview) {
            Log.d("MapReviewRVAdapter", """
                바인딩 정보:
                - 리뷰 ID: ${review.id}
                - 리뷰 작성자 ID: ${review.userId}
                - 닉네임: ${review.nickname}
            """.trimIndent())

            // 프로필 이미지 설정
            if (!review.userImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(review.userImageUrl)
                    .fallback(R.drawable.ic_profile_img_default)
                    .error(R.drawable.ic_profile_img_default)
                    .circleCrop()
                    .into(binding.reviewProfileImg)
            } else {
                binding.reviewProfileImg.setImageResource(R.drawable.ic_profile_img_default)
            }

            // 텍스트 정보 설정
            binding.reviewName.text = review.nickname ?: "알 수 없음"
            binding.petType.text = review.breed ?: ""
            binding.reviewText.text = review.content ?: ""
            binding.reviewDate.text = review.createdAt?.split("T")?.get(0)?.replace("-", ".") ?: ""

            // 이미지 컨테이너 초기화
            binding.reviewImagesContainer.removeAllViews()

            // 여러 이미지 추가
            review.placeReviewImageUrl?.forEach { imageUrl ->
                val imageView = ImageView(itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        210,
                        210
                    ).apply {
                        marginEnd = context.resources.getDimensionPixelSize(R.dimen.spacing_8)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = ContextCompat.getDrawable(context, R.drawable.rounded_image_background)
                    clipToOutline = true
                }

                Glide.with(itemView.context)
                    .load(imageUrl)
                    .fallback(R.drawable.ic_place_img_default)
                    .error(R.drawable.ic_place_img_default)
                    .centerCrop()
                    .override(210, 210)
                    .into(imageView)

                binding.reviewImagesContainer.addView(imageView)
            }

            setupEtcButton(binding.etcButton, review)
        }
    }

    private fun setupEtcButton(etcButton: View, review: MapReview) {
        val isCurrentUserReview = review.nickname == currentUserNickname && currentUserNickname.isNotEmpty()

        Log.d("MapReviewRVAdapter", """
            리뷰 정보:
            - 리뷰 닉네임: ${review.nickname}
            - 현재 사용자 닉네임: $currentUserNickname
            - isCurrentUserReview: $isCurrentUserReview
        """.trimIndent())

        etcButton.setOnClickListener { view ->
            val popupView = LayoutInflater.from(view.context)
                .inflate(
                    if (isCurrentUserReview) R.layout.popup_menu_my_review
                    else R.layout.popup_menu_custom,
                    null
                )

            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                setBackgroundDrawable(view.context.getDrawable(R.drawable.custom_popup_background))
                elevation = 10f
            }

            if (isCurrentUserReview) {
                // 내가 작성한 리뷰인 경우 - 바로 삭제
                popupView.setOnClickListener {
                    deleteReview(view.context, review.placeId, review.id)
                    popupWindow.dismiss()
                }
            } else {
                // 다른 사람의 리뷰인 경우 - 신고하기 기능
                popupView.setOnClickListener {
                    val activity = view.context as FragmentActivity
                    val mapReportFragment = MapReportFragment.newInstance(review.id)

                    activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, mapReportFragment)
                        .addToBackStack(null)
                        .commit()

                    popupWindow.dismiss()
                }
            }

            popupWindow.showAsDropDown(view, 0, 0)
        }
    }

    private fun deleteReview(context: Context, placeId: Int, reviewId: Int) {
        val token = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("token", null)

        if (token == null) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.placesApiService.deleteReview(
                    token = "Bearer $token",
                    placeId = placeId,
                    reviewId = reviewId
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccess) {
                        Toast.makeText(context, "리뷰가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        val position = reviewList.indexOfFirst { it.id == reviewId }
                        if (position != -1) {
                            reviewList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        onReviewDeleted()
                    } else {
                        Toast.makeText(
                            context,
                            response.message ?: "리뷰 삭제에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "오류가 발생했습니다: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun updateReviews(newReviews: List<MapReview>) {
        reviewList.clear()
        reviewList.addAll(newReviews)
        notifyDataSetChanged()
    }
}