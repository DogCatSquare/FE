package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.map.MapReview
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.databinding.ItemMapReviewBinding
import com.example.dogcatsquare.databinding.ItemMapReviewMultipleBinding
import com.example.dogcatsquare.ui.map.location.MapReportFragment
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkingReviewListAdapter(
    private val reviewList: ArrayList<WalkReview>,
    private var currentUserNickname: String = "",
    private val onReviewDeleted: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SINGLE_IMAGE_TYPE = 0
    private val MULTIPLE_IMAGES_TYPE = 1

    fun updateReviews(newReviews: List<WalkReview>) {
        reviewList.clear()
        reviewList.addAll(newReviews)
        notifyDataSetChanged()
    }

    fun updateNickname(nickname: String) {
        currentUserNickname = nickname
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (reviewList[position].walkReviewImageUrl?.size ?: 0 <= 1) {
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

        fun bind(review: WalkReview) {
            val context = binding.reviewProfileImg.context
            val imageSize = (32 * context.resources.displayMetrics.density).toInt()

            // 프로필 이미지 설정
            if (!review.createdBy.profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(review.createdBy.profileImageUrl)
                    .placeholder(R.drawable.ic_profile_img_default)
                    .override(imageSize, imageSize)
                    .fallback(R.drawable.ic_profile_img_default)
                    .error(R.drawable.ic_profile_img_default)
                    .circleCrop()
                    .into(binding.reviewProfileImg)
            } else {
                binding.reviewProfileImg.setImageResource(R.drawable.ic_profile_img_default)
            }

            // 텍스트 정보 설정
            binding.reviewName.text = review.createdBy.nickname ?: "알 수 없음"
            binding.petType.text = review.createdBy.breed ?: ""
            binding.reviewText.text = review.content ?: ""
            binding.reviewDate.text = review.createdAt?.split("T")?.get(0)?.replace("-", ".") ?: ""

            // 단일 이미지 설정
            if (!review.walkReviewImageUrl.isNullOrEmpty()) {
                val context = binding.reviewImg.context
                val imageSize = (70 * context.resources.displayMetrics.density).toInt()

                Glide.with(itemView.context)
                    .load(review.walkReviewImageUrl.first())
                    .placeholder(R.drawable.ic_place_img_default)
                    .override(imageSize, imageSize)
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

        fun bind(review: WalkReview) {
            Log.d(
                "MapReviewRVAdapter", """
                바인딩 정보:
                - 리뷰 ID: ${review.reviewId}
                - 닉네임: ${review.createdBy.nickname}
            """.trimIndent()
            )

            // 프로필 이미지 설정
            if (!review.createdBy.profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(review.createdBy.profileImageUrl)
                    .fallback(R.drawable.ic_profile_img_default)
                    .error(R.drawable.ic_profile_img_default)
                    .circleCrop()
                    .into(binding.reviewProfileImg)
            } else {
                binding.reviewProfileImg.setImageResource(R.drawable.ic_profile_img_default)
            }

            // 텍스트 정보 설정
            binding.reviewName.text = review.createdBy.nickname ?: "알 수 없음"
            binding.petType.text = review.createdBy.breed ?: ""
            binding.reviewText.text = review.content ?: ""
            binding.reviewDate.text = review.createdAt?.split("T")?.get(0)?.replace("-", ".") ?: ""

            // 이미지 컨테이너 초기화
            binding.reviewImagesContainer.removeAllViews()

            // 여러 이미지 추가
            review.walkReviewImageUrl?.forEach { imageUrl ->
                val imageView = ImageView(itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        210,
                        210
                    ).apply {
                        marginEnd = context.resources.getDimensionPixelSize(R.dimen.spacing_8)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background =
                        ContextCompat.getDrawable(context, R.drawable.rounded_image_background)
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

    private fun setupEtcButton(etcButton: View, review: WalkReview) {
        val isCurrentUserReview = review.createdBy.nickname == currentUserNickname && currentUserNickname.isNotEmpty()

        etcButton.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_review_all, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete -> {
                        if (isCurrentUserReview) {
                            deleteReview(view.context, review.walkId.toInt(), review.reviewId.toInt())
                        } else {
                            Toast.makeText(view.context, "권한이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    R.id.action_report -> {
                        if (isCurrentUserReview) {
                            Toast.makeText(view.context, "자신의 후기는 신고할 수 없습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            val activity = view.context as FragmentActivity
                            val walkReviewReportFragment = WalkReviewReportFragment.newInstance(review.walkId.toInt(), review.reviewId.toInt())
            
                            activity.supportFragmentManager.beginTransaction()
                                .replace(R.id.main_frm, walkReviewReportFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun deleteReview(context: Context, walkId: Int, reviewId: Int) {
        val token = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("token", null)

        if (token == null) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.walkingApiService.deleteWalkReview(
                    token = "Bearer $token",
                    walkId = walkId,
                    reviewId = reviewId
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccess) {
                        Toast.makeText(context, "리뷰가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        val position = reviewList.indexOfFirst { it.reviewId.toInt() == reviewId }
                        if (position != -1) {
                            reviewList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        onReviewDeleted()
                    } else {
                        if (response.message?.contains("정지") == true) {
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context,
                                response.message ?: "리뷰 삭제에 실패했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = if (e is retrofit2.HttpException && e.code() == 403) {
                        "권한이 없습니다."
                    } else {
                        "오류가 발생했습니다: ${e.message}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}