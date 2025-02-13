package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
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

        val myCommunityRVAdpater = MyCommunityRVAdpater(communityDatas)
        binding.myCommunityRv.adapter = myCommunityRVAdpater
        binding.myCommunityRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        myCommunityRVAdpater.setMyItemClickListener(object : MyCommunityRVAdpater.OnItemClickListener {
            override fun onItemClick(myPost: Post) {

            }
        })
    }
}