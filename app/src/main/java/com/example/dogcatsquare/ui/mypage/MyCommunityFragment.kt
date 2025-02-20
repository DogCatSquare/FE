package com.example.dogcatsquare.ui.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.api.MyPageRetrofitItf
import com.example.dogcatsquare.data.model.mypage.GetMyPostResponse
import com.example.dogcatsquare.data.community.Post
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentMyCommunityBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyCommunityFragment : Fragment() {
    lateinit var binding: FragmentMyCommunityBinding

    private var communityDatas = ArrayList<com.example.dogcatsquare.data.model.post.Post>()

    private fun getToken(): String?{
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    private fun getUserId(): Int {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return  sharedPref.getInt("userId", -1) // 기본값으로 -1 설정
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyCommunityBinding.inflate(inflater, container, false)

        setupMyCommunityRecyclerView()

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun setupMyCommunityRecyclerView() {
        communityDatas.clear()

        val myCommunityRVAdpater = MyCommunityRVAdpater(communityDatas)
        binding.myCommunityRv.adapter = myCommunityRVAdpater
        binding.myCommunityRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true).apply {
            stackFromEnd = true
        }

        getMyPost(myCommunityRVAdpater)
    }

    private fun getMyPost(adapter: MyCommunityRVAdpater) {
        val token = getToken()
        val userId = getUserId()

        val getMyPostService = RetrofitObj.getRetrofit().create(MyPageRetrofitItf::class.java)
        getMyPostService.getMyPost("Bearer $token", userId).enqueue(object : Callback<GetMyPostResponse> {
            override fun onResponse(call: Call<GetMyPostResponse>, response: Response<GetMyPostResponse>) {
                Log.d("GetMyPost/SUCCESS", response.toString())
                val resp: GetMyPostResponse = response.body()!!

                if (resp != null) {
                    if (resp.isSuccess) {
                        Log.d("GetMyPost", "내 게시물 전체 조회 성공")

                        val myPost = resp.result.map { post ->
                            com.example.dogcatsquare.data.model.post.Post (
                                id = post.id,
                                board = post.board,
                                title = post.title,
                                username = post.username,
                                content = post.content,
                                like_count = post.likeCount,
                                comment_count = post.commentCount,
                                video_URL = post.videoUrl,
                                thumbnail_URL = post.thumbnailUrl,
                                images = post.images,
                                createdAt = post.createdAt,
                                profileImage_URL = post.profileImageUrl
                            )
                        }.toList()

                        communityDatas.addAll(myPost)
                        Log.d("GetMyPost", communityDatas.toString())
                        adapter.notifyDataSetChanged()
                    }

                } else {
                    Log.e("GetMyPost/ERROR", "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<GetMyPostResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }
}