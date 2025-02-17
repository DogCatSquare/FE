package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.ItemMyBoardBinding

class AddMyBoardRVAdapter() : RecyclerView.Adapter<AddMyBoardRVAdapter.AddMyBoardRVAdapterViewHolder>() {
    inner class AddMyBoardRVAdapterViewHolder(val binding: ItemMyBoardBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddMyBoardRVAdapterViewHolder {
        val binding = ItemMyBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddMyBoardRVAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddMyBoardRVAdapterViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 3
    }
}