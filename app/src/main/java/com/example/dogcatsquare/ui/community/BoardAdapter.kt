package com.example.dogcatsquare.ui.community

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
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.model.community.BoardData
import com.example.dogcatsquare.data.model.community.MyBoardResponse
import com.example.dogcatsquare.data.model.community.MyBoardResult
import com.example.dogcatsquare.data.model.home.DDay
import com.example.dogcatsquare.data.model.home.GetAllDDayResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ItemBoardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoardAdapter(
    private val context: Context,
    private val onAddClick: (com.example.dogcatsquare.data.model.community.MyBoardResult) -> Unit // 🔹 삭제 클릭 리스너 추가
) :
    ListAdapter<com.example.dogcatsquare.data.model.community.BoardData, BoardAdapter.BoardViewHolder>(BoardDiffCallback()) {

    inner class BoardViewHolder(private val binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(board: com.example.dogcatsquare.data.model.community.BoardData) {
            binding.tvBoardName.text = board.boardName
            binding.tvBoardDescription.text = board.content
            binding.tvBoardHashtags.text = board.keywords?.joinToString(" ") { "#$it" } ?: ""

            // 🔹 추가 버튼 클릭 이벤트
            binding.ivAddIcon.setOnClickListener {
                val myBoard = com.example.dogcatsquare.data.model.community.MyBoardResult(
                    id = board.id,
                    boardId = board.id,
                    username = "",  // 필요하면 추가
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

    class BoardDiffCallback : DiffUtil.ItemCallback<com.example.dogcatsquare.data.model.community.BoardData>() {
        override fun areItemsTheSame(oldItem: com.example.dogcatsquare.data.model.community.BoardData, newItem: com.example.dogcatsquare.data.model.community.BoardData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: com.example.dogcatsquare.data.model.community.BoardData, newItem: com.example.dogcatsquare.data.model.community.BoardData): Boolean {
            return oldItem == newItem
        }
    }
}
