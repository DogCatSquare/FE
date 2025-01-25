package com.example.dogcatsquare.ui.wish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.databinding.FragmentWishWalkBinding

class WishWalkFragment : Fragment() {
    private var _binding: FragmentWishWalkBinding? = null
    private val binding get() = _binding!!
    private lateinit var wishWalkAdapter: WishWalkAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishWalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        wishWalkAdapter = WishWalkAdapter()
        binding.walkRV.apply {
            adapter = wishWalkAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadData() {
        val sampleData = listOf(
            WalkItem(
                placeName = "서대문 안산자락길",
                placeDistance = "0.55km",
                placeLocation = "서울시 서대문구 봉원사길 75-66",
                details = listOf(
                    WalkDetailItem(
                        userName = "이름1",
                        petType = "포메라니안",
                        walkDistance = "5km",
                        walkTime = "1시간 소요",
                        walkText = "산책로 본문1",
                        walkDate = "24.12.27"
                    )
                )
            ),
            WalkItem(
                placeName = "서대문 안산자락길",
                placeDistance = "0.55km",
                placeLocation = "서울시 서대문구 봉원사길 75-66",
                details = listOf(
                    WalkDetailItem(
                        userName = "이름1",
                        petType = "포메라니안",
                        walkDistance = "5km",
                        walkTime = "1시간 소요",
                        walkText = "산책로 본문1",
                        walkDate = "24.12.27"
                    ),
                    WalkDetailItem(
                        userName = "이름2",
                        petType = "치와와",
                        walkDistance = "3km",
                        walkTime = "30분 소요",
                        walkText = "산책로 본문2",
                        walkDate = "24.12.28"
                    ),
                    WalkDetailItem(
                        userName = "이름3",
                        petType = "말티즈",
                        walkDistance = "4km",
                        walkTime = "45분 소요",
                        walkText = "산책로 본문3",
                        walkDate = "24.12.29"
                    )
                )
            ),
            WalkItem(
                placeName = "서대문 안산자락길",
                placeDistance = "0.55km",
                placeLocation = "서울시 서대문구 봉원사길 75-66",
                details = listOf(
                    WalkDetailItem(
                        userName = "이름1",
                        petType = "포메라니안",
                        walkDistance = "5km",
                        walkTime = "1시간 소요",
                        walkText = "산책로 본문1",
                        walkDate = "24.12.27"
                    ),
                    WalkDetailItem(
                        userName = "이름2",
                        petType = "치와와",
                        walkDistance = "3km",
                        walkTime = "30분 소요",
                        walkText = "산책로 본문2",
                        walkDate = "24.12.28"
                    )
                )
            )
        )

        wishWalkAdapter.submitList(sampleData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}