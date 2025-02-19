package com.example.dogcatsquare.ui.map.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.map.MapReview
import com.example.dogcatsquare.databinding.ItemMapReviewBinding
import com.example.dogcatsquare.databinding.ItemMapReviewMultipleBinding

class MapReviewRVAdapter(private val reviewList: ArrayList<MapReview>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

            setupEtcButton(binding.etcButton, review.id)
        }
    }

    inner class MultipleImagesViewHolder(private val binding: ItemMapReviewMultipleBinding) :
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

            setupEtcButton(binding.etcButton, review.id)
        }
    }

    private fun setupEtcButton(etcButton: View, reviewId: Int) {
        etcButton.setOnClickListener { view ->
            val popupView = LayoutInflater.from(view.context)
                .inflate(R.layout.popup_menu_custom, null)

            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                setBackgroundDrawable(view.context.getDrawable(R.drawable.custom_popup_background))
                elevation = 10f
            }

            popupWindow.showAsDropDown(view, 0, 0)

            popupView.setOnClickListener {
                val activity = view.context as FragmentActivity
                val mapReportFragment = MapReportFragment.newInstance(reviewId)

                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, mapReportFragment)
                    .addToBackStack(null)
                    .commit()

                popupWindow.dismiss()
            }
        }
    }

    fun updateReviews(newReviews: List<MapReview>) {
        reviewList.clear()
        reviewList.addAll(newReviews)
        notifyDataSetChanged()
    }
}