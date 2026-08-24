package com.dogcatsquare.dogcatsquare.ui.community

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dogcatsquare.dogcatsquare.R
import com.dogcatsquare.dogcatsquare.data.api.BoardApiService
import com.dogcatsquare.dogcatsquare.data.model.community.BoardData
import com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResponse
import com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult
import com.dogcatsquare.dogcatsquare.data.model.home.DDay
import com.dogcatsquare.dogcatsquare.data.model.home.GetAllDDayResponse
import com.dogcatsquare.dogcatsquare.data.network.RetrofitObj
import com.dogcatsquare.dogcatsquare.databinding.ItemBoardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoardAdapter(
    private val context: Context,
    private val onAddClick: (com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult) -> Unit // 🔹 삭제 클릭 리스너 추가
) :
    ListAdapter<com.dogcatsquare.dogcatsquare.data.model.community.BoardData, BoardAdapter.BoardViewHolder>(BoardDiffCallback()) {

    inner class BoardViewHolder(private val binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(board: com.dogcatsquare.dogcatsquare.data.model.community.BoardData) {
            binding.tvBoardName.text = board.boardName
            binding.tvBoardDescription.text = board.content
            binding.tvBoardHashtags.text = board.keywords?.joinToString(" ") { "#$it" } ?: ""

            binding.ivAddIcon.setOnClickListener {
                val myBoard = com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult(
                    id = board.id,
                    boardId = board.id,
                    username = "",
                    boardName = board.boardName
                )
                onAddClick(myBoard)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val binding = ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BoardDiffCallback : DiffUtil.ItemCallback<com.dogcatsquare.dogcatsquare.data.model.community.BoardData>() {
        override fun areItemsTheSame(oldItem: com.dogcatsquare.dogcatsquare.data.model.community.BoardData, newItem: com.dogcatsquare.dogcatsquare.data.model.community.BoardData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: com.dogcatsquare.dogcatsquare.data.model.community.BoardData, newItem: com.dogcatsquare.dogcatsquare.data.model.community.BoardData): Boolean {
            return oldItem == newItem
        }
    }
}
