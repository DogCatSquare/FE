package com.example.dogcatsquare.ui.map.walking

import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.walk.Walk
import com.example.dogcatsquare.databinding.ItemMapwalkingBinding
import java.text.SimpleDateFormat
import java.util.Locale

class WalkRVAdapter(private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<WalkRVAdapter.ViewHolder>() {
    private var walks: List<Walk> = emptyList()

    inner class ViewHolder(private val binding: ItemMapwalkingBinding) : RecyclerView.ViewHolder(binding.root) {

        private fun dpToPx(dp: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                itemView.resources.displayMetrics
            ).toInt()
        }

        fun bind(walk: Walk) {
            binding.apply {
                // 1. 유저 정보 설정
                userName.text = walk.createdBy.nickname
                petType.text = walk.createdBy.breed
                Glide.with(userImg)
                    .load(walk.createdBy.profileImageUrl)
                    .fallback(R.drawable.ic_profile_img_default)
                    .into(userImg)

                // 2. 산책 정보 설정
                walkTime.text = "소요시간 ${walk.time}h"
                walkKm.text = "| ${String.format("%.1f", walk.distance)}km"
                walkText.text = walk.description

                // 3. [중요] 기존에 추가된 이미지 뷰들 삭제 (재사용 시 이미지 겹침 방지)
                imageContainer.removeAllViews()

                // 4. 이미지 리스트 처리 (여러 장을 가로로 추가)
                if (walk.walkImageUrl.isNotEmpty()) {
                    walk.walkImageUrl.forEach { url ->
                        // 각 이미지를 감쌀 CardView 생성 (모서리 12dp 라운딩)
                        val cardView = CardView(root.context).apply {
                            radius = dpToPx(12f).toFloat() // 여기서 dpToPx 사용
                            cardElevation = 0f
                            val params = LinearLayout.LayoutParams(dpToPx(120f), dpToPx(120f))
                            params.marginEnd = dpToPx(8f) // 이미지 간 간격 8dp
                            layoutParams = params
                        }

                        // 실제 사진이 들어갈 ImageView 생성
                        val imageView = ImageView(root.context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }

                        // Glide로 이미지 로드
                        Glide.with(imageView)
                            .load(url)
                            .placeholder(R.drawable.ic_place_img_default)
                            .into(imageView)

                        cardView.addView(imageView)
                        imageContainer.addView(cardView)
                    }
                } else {
                    // 이미지가 없을 경우 회색 박스 하나 노출
                    addDefaultPlaceholder()
                }

                // 5. 구글 지도 초기화 (가벼운 설정)
                mapView.onCreate(null)
                mapView.getMapAsync { googleMap ->
                    googleMap.uiSettings.isMapToolbarEnabled = false
                    // 필요 시 여기에 walk.coordinates를 이용한 Polyline 그리기 로직 추가
                }

                // 6. 날짜 포맷팅
                try {
                    val dateStr = walk.createdAt.split(".")[0]
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
                    val date = inputFormat.parse(dateStr)
                    walkDate.text = date?.let { outputFormat.format(it) } ?: ""
                } catch (e: Exception) {
                    walkDate.text = ""
                }

                // 7. 난이도 아이콘 설정
                val profileDrawable = when (walk.difficulty.lowercase()) {
                    "low" -> R.drawable.ic_easy
                    "high" -> R.drawable.ic_difficulty
                    else -> R.drawable.ic_normal
                }
                profileIv.setImageResource(profileDrawable)

                // 8. 아이템 클릭 리스너
                root.setOnClickListener {
                    onItemClick(walk.walkId)
                }
            }
        }

        // 이미지가 없을 때 보여줄 회색 박스 생성 함수
        private fun addDefaultPlaceholder() {
            val cardView = CardView(itemView.context).apply {
                radius = dpToPx(12f).toFloat()
                cardElevation = 0f
                val params = LinearLayout.LayoutParams(dpToPx(120f), dpToPx(120f))
                params.marginEnd = dpToPx(8f)
                layoutParams = params
            }
            val iv = ImageView(itemView.context).apply {
                setImageResource(R.drawable.ic_place_img_default)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(Color.parseColor("#F2F2F2"))
            }
            cardView.addView(iv)
            binding.imageContainer.addView(cardView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMapwalkingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(walks[position])
    }

    override fun getItemCount(): Int = walks.size

    fun updateData(newWalks: List<Walk>) {
        walks = newWalks
        notifyDataSetChanged()
    }
}