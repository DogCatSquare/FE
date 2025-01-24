package com.example.dogcatsquare.ui.map.walking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.ui.map.walking.data.Address

class WalkingMapViewAdapter(
    private val addressList: MutableList<Address>,
    private val onDeleteClickListener: (Address) -> Unit
) : RecyclerView.Adapter<WalkingMapViewAdapter.WalkingAddressViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkingAddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mapwalking_address, parent, false)
        return WalkingAddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalkingAddressViewHolder, position: Int) {
        val address = addressList[position]
        holder.bind(address)
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    inner class WalkingAddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressEditText: EditText = itemView.findViewById(R.id.address_et)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.imageButton2)
        private val addressImageView: ImageView = itemView.findViewById(R.id.imageView17)

        fun bind(address: Address) {
            // 주소를 EditText에 설정
            addressEditText.setText(address.address)

            // 삭제 버튼 클릭 시 처리
            deleteButton.setOnClickListener {
                onDeleteClickListener(address)
            }
        }
    }
}
