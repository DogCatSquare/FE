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
                    "전체" -> ContextCompat.getColor(holder.itemView.context, R.color.black)
                    "병원" ->  ContextCompat.getColor(holder.itemView.context, R.color.blue)
                    "산책로" ->  ContextCompat.getColor(holder.itemView.context, R.color.green)
                    "음식/카페" ->  ContextCompat.getColor(holder.itemView.context, R.color.main_color1)
                    "호텔" ->  ContextCompat.getColor(holder.itemView.context, R.color.red)
                    else -> ContextCompat.getColor(holder.itemView.context, R.color.map_stroke_gray)
                }
            }
            else -> ContextCompat.getColor(holder.itemView.context, R.color.map_stroke_gray)
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
                    startToEnd = ConstraintLayout.LayoutParams.UNSET
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    marginStart = 24
                }
                binding.buttonName.requestLayout() // 레이아웃 갱신
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