package com.example.dogcatsquare.ui.map.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.map.MapButton
import com.example.dogcatsquare.databinding.ItemMapButtonBinding
import com.google.android.material.card.MaterialCardView

class MapButtonRVAdapter(
    private val buttonList: ArrayList<MapButton>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MapButtonRVAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int, buttonName: String)
    }

    private var selectedPosition = 0

    companion object {
        private val CATEGORY_COLORS = mapOf(
            "전체" to R.color.black,
            "병원" to R.color.blue,
            "산책로" to R.color.green,
            "음식/카페" to R.color.main_color1,
            "호텔" to R.color.red
        )
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMapButtonBinding = ItemMapButtonBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(buttonList[position])
        holder.itemView.isSelected = position == selectedPosition

        // 선택된 아이템의 테두리 색상 변경
        val buttonName = buttonList[position].buttonName
        val strokeColor = if (position == selectedPosition) {
            ContextCompat.getColor(
                holder.itemView.context,
                CATEGORY_COLORS[buttonName] ?: R.color.map_stroke_gray
            )
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.map_stroke_gray)
        }
        (holder.itemView as MaterialCardView).strokeColor = strokeColor
    }

    override fun getItemCount(): Int = buttonList.size

    inner class ViewHolder(val binding: ItemMapButtonBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && position != selectedPosition) {
                    val oldPosition = selectedPosition
                    selectedPosition = position

                    // 이전 선택과 현재 선택 아이템만 업데이트
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)

                    // 리스너 호출
                    buttonList[position].buttonName?.let { name ->
                        listener.onItemClick(position, name)
                    }
                }
            }
        }

        fun bind(button: MapButton) {
            binding.buttonName.text = button.buttonName

            // 이미지 유무에 따른 레이아웃 처리
            if (button.buttonImg == null) {
                setupLayoutWithoutImage()
            } else {
                setupLayoutWithImage(button.buttonImg!!)
            }
        }

        private fun setupLayoutWithoutImage() {
            binding.buttonImg.visibility = View.GONE
            (binding.buttonName.layoutParams as ConstraintLayout.LayoutParams).apply {
                startToEnd = ConstraintLayout.LayoutParams.UNSET
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                marginStart = 24
            }
            binding.buttonName.requestLayout()
        }

        private fun setupLayoutWithImage(imageResource: Int) {
            binding.buttonImg.apply {
                visibility = View.VISIBLE
                setImageResource(imageResource)
            }
            (binding.buttonName.layoutParams as ConstraintLayout.LayoutParams).apply {
                startToStart = ConstraintLayout.LayoutParams.UNSET
                startToEnd = binding.buttonImg.id
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                marginStart = 8
            }
        }
    }

    // 선택 초기화 함수
    fun resetSelection() {
        val oldPosition = selectedPosition
        selectedPosition = 0
        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)
    }

    // 선택된 버튼 업데이트 함수
    fun updateSelectedButton(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    // 현재 선택된 카테고리 이름을 반환하는 함수
    fun getSelectedButtonName(): String {
        return buttonList.getOrNull(selectedPosition)?.buttonName ?: "전체"
    }
}