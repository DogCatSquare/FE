package com.example.dogcatsquare.ui.community

import PostApiService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.GetAllPostResult
import com.example.dogcatsquare.data.model.post.PopularPostResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentCommunityHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityHomeFragment : Fragment(R.layout.fragment_community_home) {

    private var _binding: FragmentCommunityHomeBinding? = null
    private val binding get() = _binding!!

    // 어댑터가 ArrayList를 요구한다면 ArrayList로 유지
    private val hotPostDatas: ArrayList<GetAllPostResult> = arrayListOf()
    private val allPostDatas: ArrayList<GetAllPostResult> = arrayListOf()

    private fun getToken(): String? {
        val ctx = context ?: return null
        val sp = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sp.getString("token", null)
    }

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCommunityHomeBinding.bind(view)

        setupHotPostRecyclerView()
        setupTipPostRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ==== 인기 게시물(Horizontal) ====
    private fun setupHotPostRecyclerView() {
        hotPostDatas.clear()

        val hotPostRVAdapter = PostAdapter(hotPostDatas)
        binding.rvPopularPosts.apply {
            adapter = hotPostRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }

        hotPostRVAdapter.setMyItemClickListener(object : PostAdapter.OnItemClickListener {
            override fun onItemClick(post: GetAllPostResult) {
                val ctx = context ?: return
                startActivity(Intent(ctx, PostDetailActivity::class.java).putExtra("postId", post.id))
            }
        })

        getPopularPost(hotPostRVAdapter)
    }

    private fun getPopularPost(adapter: PostAdapter) {
        val ctx = context ?: return
        val token = getToken() ?: return
        val svc = RetrofitObj.getRetrofit(ctx).create(PostApiService::class.java)

        svc.getPopularPost("Bearer $token").enqueue(object : Callback<PopularPostResponse> {
            override fun onResponse(call: Call<PopularPostResponse>, response: Response<PopularPostResponse>) {
                if (!isAdded || _binding == null) return
                val resp = response.body()
                if (response.isSuccessful && resp?.isSuccess == true) {

                    val posts = resp.result.mapNotNull { post ->
                        // 필수값 체크 (id, title 정도는 필수로 가정)
                        val id = post.id ?: return@mapNotNull null
                        val title = post.title ?: "(제목 없음)"

                        com.example.dogcatsquare.data.model.community.GetAllPostResult(
                            id = id,
                            board = post.board ?: "(게시판)",
                            title = title,
                            username = post.username ?: "(익명)",
                            animal_type = post.animal_type ?: "",
                            content = post.content ?: "",
                            likeCount = post.like_count ?: 0,
                            commentCount = post.comment_count ?: 0,
                            videoURL = post.video_URL ?: "",
                            thumbnailURL = post.thumbnail_URL ?: "",
                            images = post.images ?: emptyList(),
                            createdAt = post.createdAt ?: "",
                            profileImageURL = post.profileImage_URL ?: ""
                        )
                    }.take(3)

                    hotPostDatas.clear()
                    hotPostDatas.addAll(posts)
                    adapter.notifyDataSetChanged()

                } else {
                    Log.w("PopularPost", "fail code=${response.code()} body=$resp")
                }
            }

            override fun onFailure(call: Call<PopularPostResponse>, t: Throwable) {
                if (!isAdded) return
                Log.e("PopularPost", "error", t)
            }
        })
    }

    // ==== 꿀팁 게시물(Vertical) ====
    private fun setupTipPostRecyclerView() {
        allPostDatas.clear()

        val allPostRVAdapter = GetAllPostAdapter(allPostDatas)
        binding.rvTips.apply {
            adapter = allPostRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }

        allPostRVAdapter.setMyItemClickListener(object : GetAllPostAdapter.OnItemClickListener {
            override fun onItemClick(post: GetAllPostResult) {
                val ctx = context ?: return
                startActivity(Intent(ctx, PostDetailActivity::class.java).putExtra("postId", post.id))
            }
        })

        getTipPost(allPostRVAdapter)
    }

    private fun getTipPost(adapter: GetAllPostAdapter) {
        val ctx = context ?: return
        val token = getToken() ?: return
        val svc = RetrofitObj.getRetrofit(ctx).create(PostApiService::class.java)

        svc.getAllPosts("Bearer $token").enqueue(
            object : Callback<com.example.dogcatsquare.data.model.community.GetAllPostResponse> {
                override fun onResponse(
                    call: Call<com.example.dogcatsquare.data.model.community.GetAllPostResponse>,
                    response: Response<com.example.dogcatsquare.data.model.community.GetAllPostResponse>
                ) {
                    if (!isAdded || _binding == null) return
                    val resp = response.body()
                    if (response.isSuccessful && resp?.isSuccess == true) {

                        val posts = resp.result.mapNotNull { post ->
                            val id = post.id ?: return@mapNotNull null
                            val title = post.title ?: "(제목 없음)"

                            com.example.dogcatsquare.data.model.community.GetAllPostResult(
                                id = id,
                                board = post.board ?: "(게시판)",
                                title = title,
                                username = post.username ?: "(익명)",
                                animal_type = post.animal_type ?: "",
                                content = post.content ?: "",
                                likeCount = post.likeCount ?: 0,
                                commentCount = post.commentCount ?: 0,
                                videoURL = post.videoURL ?: "",
                                thumbnailURL = post.thumbnailURL ?: "",
                                images = post.images ?: emptyList(),
                                createdAt = post.createdAt ?: "",
                                profileImageURL = post.profileImageURL ?: ""
                            )
                        }.take(10)

                        allPostDatas.clear()
                        allPostDatas.addAll(posts)
                        adapter.notifyDataSetChanged()

                    } else {
                        Log.w("AllPost", "fail code=${response.code()} body=$resp")
                    }
                }

                override fun onFailure(
                    call: Call<com.example.dogcatsquare.data.model.community.GetAllPostResponse>,
                    t: Throwable
                ) {
                    if (!isAdded) return
                    Log.e("AllPost", "error", t)
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (!isAdded || _binding == null) return

        // 어댑터가 아직 붙지 않았어도 안전
        (binding.rvPopularPosts.adapter as? PostAdapter)?.let { adapter ->
            hotPostDatas.clear()
            adapter.notifyDataSetChanged()
            getPopularPost(adapter)
        }
        (binding.rvTips.adapter as? GetAllPostAdapter)?.let { adapter ->
            allPostDatas.clear()
            adapter.notifyDataSetChanged()
            getTipPost(adapter)
        }
    }
}