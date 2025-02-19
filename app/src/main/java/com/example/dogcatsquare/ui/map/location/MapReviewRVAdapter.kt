package com.example.dogcatsquare.ui.map.location

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.map.MapReview
import com.example.dogcatsquare.databinding.ItemMapReviewBinding

class MapReviewRVAdapter(private val reviewList: ArrayList<MapReview>): RecyclerView.Adapter<MapReviewRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMapReviewBinding = ItemMapReviewBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reviewList[position])
    }

    override fun getItemCount(): Int = reviewList.size

    inner class ViewHolder(val binding: ItemMapReviewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(review: MapReview) {
            // 프로필 이미지 설정
            if (!review.userImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(review.userImageUrl)
                    .fallback(R.drawable.ic_profile_img_default)
                    .error(R.drawable.ic_profile_img_default)
                    .circleCrop() // 프로필 이미지를 원형으로 표시
                    .into(binding.reviewProfileImg)
            } else {
                binding.reviewProfileImg.setImageResource(R.drawable.ic_profile_img_default)
            }

            // 텍스트 정보 설정
            binding.reviewName.text = review.nickname ?: "알 수 없음"
            binding.petType.text = review.breed ?: ""
            binding.reviewText.text = review.content ?: ""

            // 날짜 포맷 변환 (YYYY-MM-DD -> YYYY.MM.DD)
            binding.reviewDate.text = review.createdAt?.split("T")?.get(0)?.replace("-", ".") ?: ""

            // 리뷰 이미지 설정
            if (!review.placeReviewImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(review.placeReviewImageUrl.first())
                    .fallback(R.drawable.ic_place_img_default)
                    .error(R.drawable.ic_place_img_default)
                    .centerCrop() // 이미지를 화면에 맞게 크롭
                    .override(210, 210) // 이미지 크기를 70dp x 70dp (3배수)로 설정
                    .into(binding.reviewImg)
            } else {
                binding.reviewImg.setImageResource(R.drawable.ic_place_img_default)
            }

            // 더보기 버튼(etcButton) 클릭 리스너
            binding.etcButton.setOnClickListener { view ->
                // 커스텀 팝업 레이아웃 inflate
                val popupView = LayoutInflater.from(view.context)
                    .inflate(R.layout.popup_menu_custom, null)

                // PopupWindow 생성
                val popupWindow = PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
                ).apply {
                    setBackgroundDrawable(view.context.getDrawable(R.drawable.custom_popup_background))
                    elevation = 10f
                }

                // 팝업 창 위치 설정 및 표시
                popupWindow.showAsDropDown(view, 0, 0)

                // 팝업 메뉴 클릭 리스너
                popupView.setOnClickListener {
                    val activity = view.context as FragmentActivity
                    val mapReportFragment = MapReportFragment.newInstance(review.id)
                    activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, mapReportFragment)
                        .addToBackStack(null)
                        .commit()

                    activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, mapReportFragment)
                        .addToBackStack(null)
                        .commit()

                    popupWindow.dismiss()
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