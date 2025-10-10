package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.Tip

class TipsAdapter(
    private val tipsList: List<com.example.dogcatsquare.data.model.community.Tip>,
    private val isCompactView: Boolean = false,  // 홈탭과 꿀팁탭 모두 compact UI 유지
    private val isExpanded: Boolean = false,  // 크기 조절을 위한 변수 추가
    private val onItemClick: ((com.example.dogcatsquare.data.model.community.Tip) -> Unit)? = null
) : RecyclerView.Adapter<TipsAdapter.TipsViewHolder>() {

    inner class TipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.ivTipThumbnail)
        val nickname: TextView? = itemView.findViewById(R.id.tvTipNickname)
        val time: TextView? = itemView.findViewById(R.id.tvTipTime)
        val likeCount: TextView? = itemView.findViewById(R.id.tvTipLikeCount)
        val commentCount: TextView? = itemView.findViewById(R.id.tvTipCommentCount)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(tipsList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tips_compact, parent, false)

        // 꿀팁 탭에서만 크기 확대
        if (isExpanded) {
            val layoutParams = view.layoutParams
            layoutParams.width = (parent.measuredWidth * 0.95).toInt()
            layoutParams.height = (parent.measuredHeight * 0.45).toInt()
            view.layoutParams = layoutParams

            // 이미지 크기도 같이 키우기
            val imageView: ImageView = view.findViewById(R.id.ivTipThumbnail)
            val imageParams = imageView.layoutParams
            imageParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            imageParams.height = (parent.measuredHeight * 0.35).toInt()    // ← 이미 여기엔 measuredHeight 사용 중
            imageView.layoutParams = imageParams
        }

        return TipsViewHolder(view)
    }


    override fun onBindViewHolder(holder: TipsViewHolder, position: Int) {
        val tip = tipsList[position]
        holder.thumbnail.setImageResource(tip.thumbnailResId)
        holder.nickname?.text = tip.nickname
        holder.time?.text = tip.time
        holder.likeCount?.text = tip.likeCount.toString()
        holder.commentCount?.text = tip.commentCount.toString()
    }

    override fun getItemCount(): Int = tipsList.size
}
