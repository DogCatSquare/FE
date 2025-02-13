package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.Tip

class TipsAdapter(
    private val tipsList: List<Tip>, // 데이터 리스트
    private val isCompactView: Boolean = false, // 간략한 뷰인지 여부
    private val onItemClick: ((Tip) -> Unit)? = null // 클릭 이벤트 리스너
) : RecyclerView.Adapter<TipsAdapter.TipsViewHolder>() {

    // ViewHolder 정의
    inner class TipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.ivTipThumbnail)
        val title: TextView? = itemView.findViewById(R.id.tvTipTitle) // 홈 탭에서는 null 가능
        val content: TextView? = itemView.findViewById(R.id.tvTipContent) // 홈 탭에서는 null 가능
        val nickname: TextView? = itemView.findViewById(R.id.tvTipNickname) // 닉네임
        val time: TextView? = itemView.findViewById(R.id.tvTipTime) // 올린 시간
        val likeCount: TextView? = itemView.findViewById(R.id.tvTipLikeCount) // 좋아요 수
        val commentCount: TextView? = itemView.findViewById(R.id.tvTipCommentCount) // 댓글 수

        init {
            // 아이템 클릭 이벤트 연결
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(tipsList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipsViewHolder {
        // 간략한 뷰와 전체 뷰를 조건에 따라 선택
        val layout = if (isCompactView) R.layout.item_tips_compact else R.layout.item_tips
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return TipsViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipsViewHolder, position: Int) {
        val tip = tipsList[position]

        // 공통 데이터 설정
        holder.thumbnail.setImageResource(tip.thumbnailResId)

        if (isCompactView) {
            // 간략한 뷰에서는 닉네임, 시간, 좋아요, 댓글만 표시
            holder.nickname?.text = "닉네임" // 예시
            holder.time?.text = "1시간 전" // 예시
            holder.likeCount?.text = "6" // 예시
            holder.commentCount?.text = "1" // 예시
        } else {
            // 전체 뷰에서는 제목, 내용까지 표시
            holder.title?.text = tip.title
            holder.content?.text = tip.content
        }
    }

    override fun getItemCount(): Int = tipsList.size
}
