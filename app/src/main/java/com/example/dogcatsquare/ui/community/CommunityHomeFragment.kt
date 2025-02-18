package com.example.dogcatsquare.ui.community

import PostApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.LocalPost
import com.example.dogcatsquare.data.community.Post
import com.example.dogcatsquare.data.community.Tip
import com.example.dogcatsquare.data.model.post.PopularPostResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentCommunityHomeBinding
import com.example.dogcatsquare.ui.home.HomeHotPostRVAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityHomeFragment : Fragment(R.layout.fragment_community_home) {

    private lateinit var binding: FragmentCommunityHomeBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var tipsAdapter: TipsAdapter
    private lateinit var localPostAdapter: LocalPostAdapter

    private var hotPostDatas = ArrayList<com.example.dogcatsquare.data.model.post.Post>()
    private var tipPostDatas = ArrayList<com.example.dogcatsquare.data.model.post.Post>()

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityHomeBinding.inflate(inflater, container, false)

        setupHotPostRecyclerView()
//        setupTipPostRecyclerView()

        return binding.root
    }

    // hot post rv
    private fun setupHotPostRecyclerView() {
        hotPostDatas.clear()

        // 인기 게시물 recycler view
        val hotPostRVAdapter = PostAdapter(hotPostDatas)
        binding.rvPopularPosts.adapter = hotPostRVAdapter
        binding.rvPopularPosts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // 클릭 인터페이스
        hotPostRVAdapter.setMyItemClickListener(object : PostAdapter.OnItemClickListener {
            override fun onItemClick(post: com.example.dogcatsquare.data.model.post.Post) {
                val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
                    putExtra("postId", post.id)
                }
                startActivity(intent)
            }
        })

        getPopularPost(hotPostRVAdapter)
    }

    private fun getPopularPost(adapter: PostAdapter) {
        val token = getToken()

        val getPopularPostService = RetrofitObj.getRetrofit().create(PostApiService::class.java)
        getPopularPostService.getPopularPost("Bearer $token").enqueue(object :
            Callback<PopularPostResponse> {
            override fun onResponse(call: Call<PopularPostResponse>, response: Response<PopularPostResponse>) {
                Log.d("PopularPost/SUCCESS", response.toString())
                val resp: PopularPostResponse = response.body()!!

                if (resp != null) {
                    if (resp.isSuccess) {
                        Log.d("PopularPost", "인기게시물 전체 조회 성공")

                        val posts = resp.result.map { post ->
                            com.example.dogcatsquare.data.model.post.Post(
                                id = post.id,
                                board = post.board,
                                title = post.title,
                                username = post.username,
                                content = post.content,
                                like_count = post.like_count,
                                comment_count = post.comment_count,
                                video_URL = post.video_URL,
                                thumbnail_URL = post.thumbnail_URL,
                                images = post.images,
                                createdAt = post.createdAt,
                                profileImage_URL = post.profileImage_URL
                            )
                        }.take(2)

                        hotPostDatas.addAll(posts)
                        Log.d("HotPostList", hotPostDatas.toString())
                        adapter.notifyDataSetChanged()
                    }

                } else {
                    Log.e("GetEvent/ERROR", "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PopularPostResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }

    // hot post rv
//    private fun setupTipPostRecyclerView() {
//        tipPostDatas.clear()
//
//        // 꿀팁 게시물 recycler view
//        val tipPostRVAdapter = TipsAdapter(tipPostDatas)
//        binding.rvTips.adapter = tipPostRVAdapter
//        binding.rvTips.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//
//        // 클릭 인터페이스
//        tipPostRVAdapter.setMyItemClickListener(object : tipPostRVAdapter.OnItemClickListener {
//            override fun onItemClick(post: com.example.dogcatsquare.data.model.post.Post) {
//                val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
//                    putExtra("postId", post.id)
//                }
//                startActivity(intent)
//            }
//        })
//
//        getTipPost(tipPostRVAdapter)
//    }

//    private fun getTipPost(adapter: TipsAdapter) {
//        val token = getToken()
//    }

    private fun editPost(post: LocalPost) {
        val intent = Intent(requireContext(), EditPostActivity::class.java)
        intent.putExtra("POST_ID", post.id)
        startActivity(intent)
    }

    private fun deletePost(position: Int) {
        localPostAdapter.removePost(position)
    }
}
