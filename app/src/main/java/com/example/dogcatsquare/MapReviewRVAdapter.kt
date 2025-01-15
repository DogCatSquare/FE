package com.example.dogcatsquare

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.ItemMapReviewBinding
import com.example.dogcatsquare.ui.map.MapReportFragment

class MapReviewRVAdapter(private val reviewList: ArrayList<MapReview>): RecyclerView.Adapter<MapReviewRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MapReviewRVAdapter.ViewHolder {
        val binding: ItemMapReviewBinding = ItemMapReviewBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MapReviewRVAdapter.ViewHolder, position: Int) {
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
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.menu_review_option, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_report -> {
                            // FragmentActivity로 캐스팅하여 supportFragmentManager 접근
                            val activity = view.context as FragmentActivity
                            val mapReportFragment = MapReportFragment()

                            activity.supportFragmentManager.beginTransaction()
                                .replace(R.id.main_frm, mapReportFragment)
                                .addToBackStack(null)
                                .commit()
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }
}