package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.ItemKeywordBinding

class KeywordRVAdapter(private val keywordList: MutableList<String>) :
    RecyclerView.Adapter<KeywordRVAdapter.KeywordViewHolder>() {

    inner class KeywordViewHolder(val binding: ItemKeywordBinding) : RecyclerView.ViewHolder(binding.root) {
        val keywordTv = binding.keywordTv
        val removeIv = binding.deleteKeywordIv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordRVAdapter.KeywordViewHolder {
        val binding = ItemKeywordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KeywordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeywordRVAdapter.KeywordViewHolder, position: Int) {
        val keyword = keywordList[position]
        holder.keywordTv.text = keyword
        holder.removeIv.setOnClickListener {
            keywordList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = keywordList.size.coerceAtMost(3)
}