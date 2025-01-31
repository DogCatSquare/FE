package com.example.dogcatsquare.ui.map.location

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.map.MapReview
import com.example.dogcatsquare.databinding.ItemMapReviewBinding

class MapReviewRVAdapter(private val reviewList: ArrayList<MapReview>): RecyclerView.Adapter<MapReviewRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMapReviewBinding = ItemMapReviewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reviewList[position])
    }

    override fun getItemCount(): Int = reviewList.size

    inner class ViewHolder(val binding: ItemMapReviewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(review: MapReview) {
            binding.reviewProfileImg.setImageResource(review.reviewProfileImg!!)
            binding.reviewName.text = review.reviewName
            binding.petType.text = review.petType
            binding.reviewText.text = review.reviewText
            binding.reviewDate.text = review.reviewDate
            binding.reviewImg.setImageResource(review.reviewImg!!)

            // etcButton에 클릭 리스너 추가
            binding.etcButton.setOnClickListener { view ->
                // 커스텀 팝업 레이아웃 inflate
                val popupView = LayoutInflater.from(view.context).inflate(R.layout.popup_menu_custom, null)

                // PopupWindow 생성
                val popupWindow = PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
                )

                // 팝업 창의 배경 설정
                popupWindow.setBackgroundDrawable(view.context.getDrawable(R.drawable.custom_popup_background))
                popupWindow.elevation = 10f

                // 팝업 창 위치 설정
                popupWindow.showAsDropDown(view, 0, 0)

                // 팝업 메뉴 전체에 클릭 리스너 추가
                popupView.setOnClickListener {
                    val activity = view.context as FragmentActivity
                    val mapReportFragment = MapReportFragment()

                    activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, mapReportFragment)
                        .addToBackStack(null)
                        .commit()

                    popupWindow.dismiss()
                }
            }


        }
    }
}