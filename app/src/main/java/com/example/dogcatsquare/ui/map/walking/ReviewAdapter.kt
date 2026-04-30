package com.example.dogcatsquare.ui.map.walking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ItemMapwalingReviewBinding
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReview
import com.example.dogcatsquare.ui.map.walking.data.Review
import kotlinx.coroutines.launch

class ReviewAdapter(
    private var reviews: List<WalkReview>,
    private var currentUserNickname: String = "",
    private val onReviewDeleted: () -> Unit = {}
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    // 클릭 리스너 변수 선언
    private var onItemClickListener: ((WalkReview) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mapwaling_review, parent, false)
        return ReviewViewHolder(view)
    }

    // 클릭 리스너 설정 메소드
    fun setOnItemClickListener(listener: (WalkReview) -> Unit) {
        onItemClickListener = listener
    }

    fun updateData(newReviews: List<WalkReview>) {
        reviews = newReviews
        notifyDataSetChanged()  // 변경 사항을 RecyclerView에 알림
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // 텍스트 뷰에 데이터 바인딩
        holder.nicknameTextView.text = review.createdBy.nickname
        holder.breedTextView.text = review.createdBy.breed
        holder.contentTextView.text = review.content

        // 프로필 이미지 로드
        Glide.with(holder.itemView.context)
            .load(review.createdBy.profileImageUrl)
            .into(holder.profileImageView)

        // 리뷰 이미지가 있으면 로드
        if (review.walkReviewImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(review.walkReviewImageUrl[0]) // 첫 번째 이미지만 예시로 사용
                .into(holder.reviewImageView)
        }

        // 아이템 클릭 시 리스너 호출
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(review)
        }

        val isCurrentUserReview = review.createdBy.nickname == currentUserNickname && currentUserNickname.isNotEmpty()
        holder.reviewMenuButton.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_review_all, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete -> {
                        if (isCurrentUserReview) {
                            deleteReview(view.context, review.walkId.toInt(), review.reviewId.toInt())
                        } else {
                            android.widget.Toast.makeText(view.context, "권한이 없습니다.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    R.id.action_report -> {
                        if (isCurrentUserReview) {
                            android.widget.Toast.makeText(view.context, "자신의 후기는 신고할 수 없습니다.", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            val activity = view.context as androidx.fragment.app.FragmentActivity
                            val walkReviewReportFragment = com.example.dogcatsquare.ui.map.walking.WalkReviewReportFragment.newInstance(review.walkId.toInt(), review.reviewId.toInt())
            
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

    private fun deleteReview(context: android.content.Context, walkId: Int, reviewId: Int) {
        val token = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            .getString("token", null)

        if (token == null) {
            android.widget.Toast.makeText(context, "로그인이 필요합니다.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val response = com.example.dogcatsquare.data.network.RetrofitClient.walkingApiService.deleteWalkReview(
                    token = "Bearer $token",
                    walkId = walkId,
                    reviewId = reviewId
                )

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (response.isSuccess) {
                        android.widget.Toast.makeText(context, "리뷰가 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                        // 1. Immutable List를 처리하기 어려우니 다시 fetch 되도록 콜백 호출
                        onReviewDeleted()
                    } else {
                        if (response.message?.contains("정지") == true) {
                            android.widget.Toast.makeText(context, response.message, android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                response.message ?: "리뷰 삭제에 실패했습니다.",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    val errorMessage = if (e is retrofit2.HttpException && e.code() == 403) {
                        "권한이 없습니다."
                    } else {
                        "오류가 발생했습니다: ${e.message}"
                    }
                    android.widget.Toast.makeText(context, errorMessage, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.review_image)
        val nicknameTextView: TextView = itemView.findViewById(R.id.name_title)
        val breedTextView: TextView = itemView.findViewById(R.id.name_tv)
        val contentTextView: TextView = itemView.findViewById(R.id.review_tv)
        val reviewImageView: ImageView = itemView.findViewById(R.id.review_iv)
        val reviewMenuButton: android.widget.ImageButton = itemView.findViewById(R.id.review_button)
    }
}
