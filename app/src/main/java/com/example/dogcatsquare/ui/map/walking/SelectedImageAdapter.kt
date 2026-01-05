package com.example.dogcatsquare.ui.map.walking

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R

class SelectedImageAdapter(
    private val bitmaps: MutableList<Bitmap>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<SelectedImageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.iv_selected_img)
        val deleteBtn: ImageView = view.findViewById(R.id.iv_delete_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageBitmap(bitmaps[position])
        holder.deleteBtn.setOnClickListener { onDeleteClick(position) }
    }

    override fun getItemCount(): Int = bitmaps.size
}