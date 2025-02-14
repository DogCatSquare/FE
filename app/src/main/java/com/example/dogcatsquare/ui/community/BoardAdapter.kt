package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.BoardData

class BoardAdapter(emptyList: List<Any>) :
    ListAdapter<BoardData, BoardAdapter.BoardViewHolder>(BoardDiffCallback()) {

    class BoardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val boardName: TextView = view.findViewById(R.id.tvBoardName)
        val boardContent: TextView = view.findViewById(R.id.tvBoardDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_board, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        val board = getItem(position)
        holder.boardName.text = board.boardName
        holder.boardContent.text = board.content
    }

    class BoardDiffCallback : DiffUtil.ItemCallback<BoardData>() {
        override fun areItemsTheSame(oldItem: BoardData, newItem: BoardData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BoardData, newItem: BoardData): Boolean {
            return oldItem == newItem
        }
    }
}
