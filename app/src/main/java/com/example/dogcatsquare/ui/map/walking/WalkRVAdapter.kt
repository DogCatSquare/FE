package com.example.dogcatsquare.ui.map.walking

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.walk.Walk
import com.example.dogcatsquare.databinding.ItemMapwalkingBinding
import java.text.SimpleDateFormat
import java.util.Locale

class WalkRVAdapter : RecyclerView.Adapter<WalkRVAdapter.ViewHolder>() {
    private var walks: List<Walk> = emptyList()

    inner class ViewHolder(private val binding: ItemMapwalkingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(walk: Walk) {
            binding.apply {
                // 유저 정보
                userName.text = walk.createdBy.nickname
                petType.text = walk.createdBy.breed

                // 프로필 이미지
                Glide.with(userImg)
                    .load(walk.createdBy.profileImageUrl)
                    .fallback(R.drawable.ic_profile_img_default)
                    .into(userImg)

                // 산책 정보
                walkTime.text = "소요시간 ${walk.time}h"
                walkKm.text = "| ${String.format("%.1f", walk.distance)}km"
                walkText.text = walk.description

                // 산책 이미지
                if (walk.walkImageUrl.isNotEmpty()) {
                    Glide.with(walkImg)
                        .load(walk.walkImageUrl[0])
                        .fallback(R.drawable.dog_sample)
                        .into(walkImg)
                }

                // 날짜 포맷팅 (2025-02-20T11:29:10Z -> 25.02.20)
                try {
                    val dateStr = walk.createdAt.split(".")[0] // 마이크로초 부분 제거
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
                    val date = inputFormat.parse(dateStr)
                    walkDate.text = date?.let { outputFormat.format(it) } ?: ""
                } catch (e: Exception) {
                    Log.e("WalkRVAdapter", "Date parsing error", e)
                    walkDate.text = "" // 파싱 실패시 빈 문자열 표시
                }


                // 난이도에 따른 프로필 이미지 설정
                val profileDrawable = when (walk.difficulty.lowercase()) {
                    "easy" -> R.drawable.ic_easy
                    "normal" -> R.drawable.ic_normal
                    "hard" -> R.drawable.ic__difficulty
                    else -> R.drawable.ic_normal
                }
                profileIv.setImageResource(profileDrawable)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMapwalkingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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