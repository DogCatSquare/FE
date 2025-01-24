package com.example.dogcatsquare.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.data.login.Pet
import com.example.dogcatsquare.databinding.ItemAddPetBinding

class AddPetRVAdapter(private val petList: ArrayList<Pet>) : RecyclerView.Adapter<AddPetRVAdapter.AddPetAdapterViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(pet: Pet)
    }

    private lateinit var mItemClickListener: OnItemClickListener

    fun setMyItemClickListener(itemClickListener: OnItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddPetRVAdapter.AddPetAdapterViewHolder {
        val binding = ItemAddPetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddPetAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddPetRVAdapter.AddPetAdapterViewHolder, position: Int) {
        val pet = petList[position]
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(petList[position])
        }
        holder.bind(pet)
    }

    override fun getItemCount(): Int = petList.size

    inner class AddPetAdapterViewHolder(val binding: ItemAddPetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pet: Pet) {

        }
    }
}