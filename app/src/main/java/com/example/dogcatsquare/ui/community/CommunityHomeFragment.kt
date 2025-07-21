package com.example.dogcatsquare.ui.community

import PostApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.GetAllPostResponse
import com.example.dogcatsquare.data.model.community.GetAllPostResult
import com.example.dogcatsquare.data.model.community.LocalPost
import com.example.dogcatsquare.data.model.post.PopularPostResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentCommunityHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityHomeFragment : Fragment(R.layout.fragment_community_home) {

    private lateinit var binding: FragmentCommunityHomeBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var tipsAdapter: TipsAdapter
    private lateinit var localPostAdapter: LocalPostAdapter

    private var hotPostDatas = ArrayList<com.example.dogcatsquare.data.model.community.GetAllPostResult>()
    private var allPostDatas = ArrayList<com.example.dogcatsquare.data.model.community.GetAllPostResult>()

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
        setupTipPostRecyclerView()

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
            override fun onItemClick(post: com.example.dogcatsquare.data.model.community.GetAllPostResult) {
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

        val getPopularPostService = RetrofitObj.getRetrofit(requireContext()).create(PostApiService::class.java)
        getPopularPostService.getPopularPost("Bearer $token").enqueue(object :
            Callback<PopularPostResponse> {
            override fun onResponse(call: Call<PopularPostResponse>, response: Response<PopularPostResponse>) {
                Log.d("PopularPost/SUCCESS", response.toString())
                val resp: PopularPostResponse = response.body()!!

                if (resp != null) {
                    if (resp.isSuccess) {
                        Log.d("PopularPost", "인기게시물 전체 조회 성공")

                        val posts = resp.result.map { post ->
                            com.example.dogcatsquare.data.model.community.GetAllPostResult(
                                id = post.id,
                                board = post.board,
                                title = post.title,
                                username = post.username,
                                animal_type = post.animal_type,
                                content = post.content,
                                likeCount = post.like_count,
                                commentCount = post.comment_count,
                                videoURL = post.video_URL,
                                thumbnailURL = post.thumbnail_URL,
                                images = post.images,
                                createdAt = post.createdAt,
                                profileImageURL = post.profileImage_URL
                            )
                        }.take(3)

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
    private fun setupTipPostRecyclerView() {
        allPostDatas.clear()

        // 꿀팁 게시물 recycler view
        val allPostRVAdapter = GetAllPostAdapter(allPostDatas)
        binding.rvTips.adapter = allPostRVAdapter
        binding.rvTips.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // 클릭 인터페이스
        allPostRVAdapter.setMyItemClickListener(object : GetAllPostAdapter.OnItemClickListener {
            override fun onItemClick(post: com.example.dogcatsquare.data.model.community.GetAllPostResult) {
                val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
                    putExtra("postId", post.id)
                }
                startActivity(intent)
            }
        })

        getTipPost(allPostRVAdapter)
    }

    private fun getTipPost(adapter: GetAllPostAdapter) {
        val token = getToken()

        val getAllPostSercive = RetrofitObj.getRetrofit(requireContext()).create(PostApiService::class.java)
        getAllPostSercive.getAllPosts("Bearer $token").enqueue(object : Callback<com.example.dogcatsquare.data.model.community.GetAllPostResponse> {
            override fun onResponse(
                call: Call<com.example.dogcatsquare.data.model.community.GetAllPostResponse>,
                response: Response<com.example.dogcatsquare.data.model.community.GetAllPostResponse>
            ) {
                Log.d("AllPost/SUCCESS", response.toString())
                val resp: com.example.dogcatsquare.data.model.community.GetAllPostResponse = response.body()!!

                if (resp != null) {
                    if (resp.isSuccess) {
                        Log.d("AllPost", "전체 게시물 전체 조회 성공")

                        val posts = resp.result.map { post ->
                            com.example.dogcatsquare.data.model.community.GetAllPostResult(
                                id = post.id,
                                board = post.board,
                                title = post.title,
                                username = post.username,
                                animal_type = post.animal_type,
                                content = post.content,
                                likeCount = post.likeCount,
                                commentCount = post.commentCount,
                                videoURL = post.videoURL,
                                thumbnailURL = post.thumbnailURL,
                                images = post.images,
                                createdAt = post.createdAt,
                                profileImageURL = post.profileImageURL
                            )
                        }.take(10)

                        allPostDatas.addAll(posts)
                        Log.d("AllPostList", allPostDatas.toString())
                        adapter.notifyDataSetChanged()
                    }

                } else {
                    Log.e("AllPost/ERROR", "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<com.example.dogcatsquare.data.model.community.GetAllPostResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }

    override fun onResume() {
        super.onResume()
        val token = getToken()
        if (token != null) {
            val hotAdapter = binding.rvPopularPosts.adapter as? PostAdapter
            hotAdapter?.let {
                hotPostDatas.clear()
                getPopularPost(it)
            }
            // 꿀팁 게시물(Tip Post) 데이터 업데이트
            val tipAdapter = binding.rvTips.adapter as? GetAllPostAdapter
            tipAdapter?.let {
                allPostDatas.clear()
                getTipPost(it)
            }
        }
    }
}