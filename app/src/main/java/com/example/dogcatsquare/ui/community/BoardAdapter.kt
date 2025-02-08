package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.BoardItem

class BoardAdapter(private var boardList: List<BoardItem>) :
    RecyclerView.Adapter<BoardAdapter.BoardViewHolder>() {

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
        val board = boardList[position]
        holder.boardName.text = board.boardName
        holder.boardContent.text = board.content
    }

    override fun getItemCount(): Int = boardList.size

    fun updateData(newList: List<BoardItem>) {
        boardList = newList
        notifyDataSetChanged()
    }
}
