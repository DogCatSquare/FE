package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.login.DogCat
import com.example.dogcatsquare.data.login.Pet
import com.example.dogcatsquare.databinding.FragmentEditInfoBinding

class EditInfoFragment : Fragment() {
    lateinit var binding : FragmentEditInfoBinding

    companion object {
        const val ADD_PET_REQUEST_CODE = 1001
    }

    private var petDatas = ArrayList<Pet>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditInfoBinding.inflate(inflater, container, false)

        setupAddPetRecyclerView()

        // 반려동물 추가
        binding.addPetBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, AddPetFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        binding.myEmailEt.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, EditEmailFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        binding.editDoneBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MypageFragment())
                .commitAllowingStateLoss()
        }

        return binding.root
    }

    private fun setupAddPetRecyclerView() {
        petDatas.clear()

        val addPetRVAdapter = AddPetRVAdapter(petDatas)
        binding.petInfoRv.adapter = addPetRVAdapter
        binding.petInfoRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        parentFragmentManager.setFragmentResultListener("addPetInfoResult", this) { _, _ ->
            petDatas.clear()
            // 기본 아이템 추가
            petDatas.apply {
                add(Pet("이름", DogCat.DOG.toString(), "포메라니안", "2025-01-23"))
            }
            addPetRVAdapter.notifyDataSetChanged() // RecyclerView 업데이트
        }

        addPetRVAdapter.setMyItemClickListener(object : AddPetRVAdapter.OnItemClickListener {
            override fun onItemClick(pet: Pet) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, EditPetFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        })
    }
}