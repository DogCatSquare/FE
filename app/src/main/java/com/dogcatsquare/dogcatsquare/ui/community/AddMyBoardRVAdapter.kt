package com.dogcatsquare.dogcatsquare.ui.community

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dogcatsquare.dogcatsquare.data.api.BoardApiService
import com.dogcatsquare.dogcatsquare.data.model.community.DeleteMyBoardResponse
import com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResponse
import com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult
import com.dogcatsquare.dogcatsquare.data.network.RetrofitObj
import com.dogcatsquare.dogcatsquare.databinding.ItemMyBoardBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddMyBoardRVAdapter(
    private val context: Context,
    private val onDeleteClick: (com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult) -> Unit // 🔹 삭제 클릭 리스너 추가
) : ListAdapter<com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult, AddMyBoardRVAdapter.MyBoardViewHolder>(MyBoardDiffCallback()) {

    inner class MyBoardViewHolder(private val binding: ItemMyBoardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(myBoard: com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult) {
            binding.myBoardTv.text = myBoard.boardName
            binding.deleteMyBoardIv.setOnClickListener {
                onDeleteClick(myBoard) // 🔹 삭제 버튼 클릭 시 콜백 호출
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBoardViewHolder {
        val binding = ItemMyBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyBoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyBoardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // 🔹 DiffUtil 추가 (RecyclerView 자동 갱신)
    class MyBoardDiffCallback : DiffUtil.ItemCallback<com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult>() {
        override fun areItemsTheSame(oldItem: com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult, newItem: com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult, newItem: com.dogcatsquare.dogcatsquare.data.model.community.MyBoardResult): Boolean {
            return oldItem == newItem
        }
    }
}