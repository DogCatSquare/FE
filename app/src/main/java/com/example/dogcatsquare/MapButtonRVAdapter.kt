package com.example.dogcatsquare

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
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
        val strokeColor = when {
            position == selectedPosition -> {
                when(buttonList[position].buttonName) {
                    "전체" -> Color.parseColor("#000000")
                    "병원" -> Color.parseColor("#4690F5")
                    "산책로" -> Color.parseColor("#6BAB70")
                    "음식/카페" -> Color.parseColor("#FFB200")
                    "호텔" -> Color.parseColor("#F36037")
                    else -> Color.parseColor("#E8E8E8")
                }
            }
            else -> Color.parseColor("#E8E8E8")
        }
        (holder.itemView as MaterialCardView).strokeColor = strokeColor
    }

    override fun getItemCount(): Int = buttonList.size

    inner class ViewHolder(val binding: ItemMapButtonBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(selectedPosition)
                    selectedPosition = position
                    notifyItemChanged(selectedPosition)
                    buttonList[position].buttonName?.let { name ->
                        listener.onItemClick(position, name)
                    }
                }
            }
        }

        fun bind(button: MapButton) {
            binding.buttonName.text = button.buttonName

            if (button.buttonImg == null) {
                binding.buttonImg.visibility = View.GONE
                (binding.buttonName.layoutParams as ConstraintLayout.LayoutParams).apply {
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    startToEnd = ConstraintLayout.LayoutParams.UNSET
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    marginStart = 16
                }
            } else {
                binding.buttonImg.visibility = View.VISIBLE
                binding.buttonImg.setImageResource(button.buttonImg!!)
                (binding.buttonName.layoutParams as ConstraintLayout.LayoutParams).apply {
                    startToStart = ConstraintLayout.LayoutParams.UNSET
                    startToEnd = binding.buttonImg.id
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    marginStart = 8
                }
            }
        }
    }
}