package com.example.dogcatsquare.ui.map.walking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.naver.maps.map.MapFragment

class WalkingMapReviewAllFragment : Fragment() {

    private lateinit var reviewAdapter: WalkingReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mapwalking_reviewall, container, false)

        (activity as? AppCompatActivity)?.apply {
            val toolbar: Toolbar = view.findViewById(R.id.walking_review_toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.title = "이웃들의 추천 코스"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        // RecyclerView 설정
        val recyclerView: RecyclerView = view.findViewById(R.id.review_rv)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val reviews: MutableList<WalkMapReview> = mutableListOf()

        reviews.add(
            WalkMapReview(
                userName = "홍길동",
                petType = "강아지",
                walkTime = "30분",
                walkKm = "2.5km",
                walkText = "산책로가 너무 좋아요!",
                walkDate = "2024-02-19",
                userImgUrl = "https://example.com/user1.jpg",
                walkImgUrl = "https://example.com/walk1.jpg",
                profileImgUrl = "https://example.com/profile1.jpg"
            )
        )

        reviews.add(
            WalkMapReview(
                userName = "김철수",
                petType = "고양이",
                walkTime = "40분",
                walkKm = "3.0km",
                walkText = "길이 넓고 한적해서 좋아요!",
                walkDate = "2024-02-18",
                userImgUrl = "https://example.com/user2.jpg",
                walkImgUrl = "https://example.com/walk2.jpg",
                profileImgUrl = "https://example.com/profile2.jpg"
            )
        )

        // RecyclerView 설정
        reviewAdapter = WalkingReviewAdapter(reviews, maxItemCount = 2) { item ->
            // 아이템 클릭 시 다른 프래그먼트로 이동
            val bundle = Bundle().apply {
                putString("itemName", item.userName) // 아이템의 데이터를 전달
            }

            val detailFragment = WalkingStartViewFragment().apply {
                arguments = bundle
            }

            // 프래그먼트 트랜잭션을 사용하여 이동
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, WalkingStartViewFragment())
                .addToBackStack(null)
                .commit()
        }

        recyclerView.adapter = reviewAdapter

        return view
    }

}