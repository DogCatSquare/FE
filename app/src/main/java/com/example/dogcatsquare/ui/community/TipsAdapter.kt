package com.example.dogcatsquare.ui.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R

class TipsAdapter(private val tipsList: List<Tip>) :
    RecyclerView.Adapter<TipsAdapter.TipsViewHolder>() {

    inner class TipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTipTitle)
        val content: TextView = itemView.findViewById(R.id.tvTipContent)
        val thumbnail: ImageView = itemView.findViewById(R.id.ivTipThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tips, parent, false)
        return TipsViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipsViewHolder, position: Int) {
        val tip = tipsList[position]
        holder.title.text = tip.title
        holder.content.text = tip.content
        holder.thumbnail.setImageResource(R.drawable.ic_placeholder) // 썸네일
    }

    override fun getItemCount(): Int = tipsList.size
}
