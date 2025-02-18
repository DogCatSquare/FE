package com.example.dogcatsquare.ui.map.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.databinding.ItemRecentSearchBinding

data class SearchItem(
    val query: String
)

class SearchWordAdapter(
    private var searches: MutableList<SearchItem>,
    private val listener: OnSearchTermClickListener
) : RecyclerView.Adapter<SearchWordAdapter.ViewHolder>() {

    interface OnSearchTermClickListener {
        fun onSearchTermClicked(query: String)
        fun onSearchTermRemoved(query: String)
    }

    inner class ViewHolder(private val binding: ItemRecentSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchItem) {
            binding.apply {
                // 검색어 설정
                searchKeyword.text = item.query

                // 전체 카드뷰 클릭 이벤트
                root.setOnClickListener {
                    listener.onSearchTermClicked(item.query)
                }

                // 삭제 버튼 클릭 이벤트
                deleteButton.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onSearchTermRemoved(item.query)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(searches[position])
    }

    override fun getItemCount(): Int = searches.size

    fun updateSearches(newSearches: List<SearchItem>) {
        searches.clear()
        searches.addAll(newSearches)
        notifyDataSetChanged()
    }

}