package com.example.dogcatsquare.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.pet.PetList
import com.example.dogcatsquare.databinding.ItemAddPetBinding

class AddPetRVAdapter(private val petList: ArrayList<PetList>) : RecyclerView.Adapter<AddPetRVAdapter.AddPetAdapterViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(pet: PetList)
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
        fun bind(pet: PetList) {
            binding.petNameTv.text = pet.petName
            binding.petBreedTv.text = pet.breed
            binding.petBirthTv.text = pet.birth
            Glide.with(this.itemView)
                .load(pet.petImageUrl)
                .signature(ObjectKey(System.currentTimeMillis().toString())) // 캐시 무효화
                .placeholder(R.drawable.ic_profile_default)
                .into(binding.petIv)
        }
    }
}