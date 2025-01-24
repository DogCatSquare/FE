package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.post.Pet
import com.example.dogcatsquare.data.post.Post
import com.example.dogcatsquare.databinding.FragmentMyCommunityBinding

class MyCommunityFragment : Fragment() {
    lateinit var binding: FragmentMyCommunityBinding

    private var communityDatas = ArrayList<Post>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyCommunityBinding.inflate(inflater, container, false)

        setupMyCommunityRecyclerView()

        return binding.root
    }

    private fun setupMyCommunityRecyclerView() {
        communityDatas.clear()
        communityDatas.apply {
            add(Post(
                1,
                "반려동물과의 첫 만남",
                "우리 집에 새로운 가족이 생겼어요! 포메라니안인 호두가 우리 집에 온 첫날이에요. 너무 귀엽고 사랑스러워요!",
                "HappyPetLover",
                listOf(
                    Pet("호두", "포메라니안")
                ),
                120,
                15
            ))

            add(Post(
                1,
                "반려동물과의 첫 만남",
                "우리 집에 새로운 가족이 생겼어요! 포메라니안인 호두가 우리 집에 온 첫날이에요. 너무 귀엽고 사랑스러워요!",
                "HappyPetLover",
                listOf(
                    Pet("호두", "포메라니안")
                ),
                120,
                15
            ))

            add(Post(
                1,
                "반려동물과의 첫 만남",
                "우리 집에 새로운 가족이 생겼어요! 포메라니안인 호두가 우리 집에 온 첫날이에요. 너무 귀엽고 사랑스러워요!",
                "HappyPetLover",
                listOf(
                    Pet("호두", "포메라니안")
                ),
                120,
                15
            ))
        }

        val myCommunityRVAdpater = MyCommunityRVAdpater(communityDatas)
        binding.myCommunityRv.adapter = myCommunityRVAdpater
        binding.myCommunityRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        myCommunityRVAdpater.setMyItemClickListener(object : MyCommunityRVAdpater.OnItemClickListener {
            override fun onItemClick(myPost: Post) {
//                requireActivity().supportFragmentManager.beginTransaction()
//                    .replace(R.id.main_frm, fragment)
//                    .addToBackStack(null)
//                    .commitAllowingStateLoss()
            }
        })
    }
}