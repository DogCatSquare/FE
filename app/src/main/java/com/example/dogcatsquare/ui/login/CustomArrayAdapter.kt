package com.example.dogcatsquare.ui.login

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.dogcatsquare.R

class CustomArrayAdapter(
    context: Context,
    private var items: List<String>,
    private var selectedPosition: Int
) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.setTextColor(
            if (position == selectedPosition) ContextCompat.getColor(context, R.color.main_color1)
            else ContextCompat.getColor(context, R.color.gray3)
        )
        return view
    }

    fun updateData(newItems: List<String>, newSelectedPosition: Int) {
        this.items = newItems
        this.selectedPosition = newSelectedPosition
        clear()
        addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateSelectedPosition(newSelectedPosition: Int) {
        this.selectedPosition = newSelectedPosition
        notifyDataSetChanged()
    }
}
