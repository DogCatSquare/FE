package com.example.dogcatsquare.ui.community

import PostApiService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.community.GetAllPostResult
import com.example.dogcatsquare.data.model.community.PostListItem
import com.example.dogcatsquare.data.model.community.toResult
import com.example.dogcatsquare.data.model.post.PopularPostResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentCommunityHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityHomeFragment : Fragment(R.layout.fragment_community_home) {

    private var _binding: FragmentCommunityHomeBinding? = null
    private val binding get() = _binding!!

    private val hotPostDatas: ArrayList<GetAllPostResult> = arrayListOf()

    private var popularCall: Call<PopularPostResponse>? = null
    private var tipCall: Call<com.example.dogcatsquare.data.model.community.GetAllPostResponse>? = null

    private fun getToken(): String? {
        val ctx = context ?: return null
        val sp = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sp.getString("token", null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCommunityHomeBinding.bind(view)

        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }

        setupHotPostRecyclerView()
        setupTipPostRecyclerView()
    }

    private fun refreshData() {
        (binding.rvPopularPosts.adapter as? PostAdapter)?.let { adapter ->
            getPopularPost(adapter)
        }
        (binding.rvTips.adapter as? GetAllPostAdapter)?.let { adapter ->
            getTipPost(adapter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 진행 중 네트워크 콜 취소 (메모리 누수/콜백 중 UI접근 방지)
        popularCall?.cancel(); popularCall = null
        tipCall?.cancel(); tipCall = null
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

            addOnItemTouchListener(object : androidx.recyclerview.widget.RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: androidx.recyclerview.widget.RecyclerView, e: android.view.MotionEvent): Boolean {
                    when (e.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            rv.parent.requestDisallowInterceptTouchEvent(true)
                        }
                        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                            rv.parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    return false
                }

                override fun onTouchEvent(rv: androidx.recyclerview.widget.RecyclerView, e: android.view.MotionEvent) {}
                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            })
        }

        hotPostRVAdapter.setMyItemClickListener(object : PostAdapter.OnItemClickListener {
            override fun onItemClick(post: GetAllPostResult) {
                val ctx = context ?: return
                startActivity(
                    Intent(ctx, PostDetailActivity::class.java)
                        .putExtra("postId", post.id)
                )
            }
        })

        getPopularPost(hotPostRVAdapter)
    }

    private fun getPopularPost(adapter: PostAdapter) {
        val ctx = context ?: return
        val token = getToken() ?: run {
            Log.w("PopularPost", "no token")
            return
        }
        val svc = RetrofitObj.getRetrofit(ctx).create(PostApiService::class.java)

        popularCall = svc.getPopularPost("Bearer $token")
        popularCall?.enqueue(object : Callback<PopularPostResponse> {
            override fun onResponse(call: Call<PopularPostResponse>, response: Response<PopularPostResponse>) {
                _binding?.swipeRefresh?.isRefreshing = false
                // 화면 분리/파괴되었으면 종료
                if (!isAdded || _binding == null) return

                val resp = response.body()
                if (response.isSuccessful && resp?.isSuccess == true) {
                    val posts = resp.result.mapNotNull { post ->
                        val id = post.id ?: return@mapNotNull null
                        val title = post.title ?: "(제목 없음)"

                        GetAllPostResult(
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
                _binding?.swipeRefresh?.isRefreshing = false
                if (!isAdded) return
                Log.e("PopularPost", "error", t)
            }
        })
    }

    // ==== 꿀팁 게시물(Vertical) ====
    private fun setupTipPostRecyclerView() {

        val allPostRVAdapter = GetAllPostAdapter()
        binding.rvTips.apply {
            adapter = allPostRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(false)
        }

        allPostRVAdapter.setMyItemClickListener(object : GetAllPostAdapter.OnItemClickListener {
            override fun onItemClick(post: PostListItem) {
                val ctx = context ?: return
                startActivity(
                    Intent(ctx, PostDetailActivity::class.java)
                        .putExtra("postId", post.id)
                )
            }
        })

        getTipPost(allPostRVAdapter)
    }

    private fun getTipPost(adapter: GetAllPostAdapter) {
        val ctx = context ?: return
        val token = getToken() ?: run {
            Log.w("AllPost", "no token")
            return
        }
        val svc = RetrofitObj.getRetrofit(ctx).create(PostApiService::class.java)

        tipCall = svc.getAllPosts("Bearer $token")
        tipCall?.enqueue(object : Callback<com.example.dogcatsquare.data.model.community.GetAllPostResponse> {
            override fun onResponse(
                call: Call<com.example.dogcatsquare.data.model.community.GetAllPostResponse>,
                response: Response<com.example.dogcatsquare.data.model.community.GetAllPostResponse>
            ) {
                _binding?.swipeRefresh?.isRefreshing = false
                if (!isAdded || _binding == null) return

                val resp = response.body()
                if (response.isSuccessful && resp?.isSuccess == true) {
                    val sortedList = resp.result.sortedByDescending { it.id }
                    adapter.submitList(sortedList)
                } else {
                    Log.w("AllPost", "fail code=${response.code()} body=$resp")
                }
            }

            override fun onFailure(
                call: Call<com.example.dogcatsquare.data.model.community.GetAllPostResponse>,
                t: Throwable
            ) {
                _binding?.swipeRefresh?.isRefreshing = false
                if (!isAdded) return
                Log.e("AllPost", "error", t)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (!isAdded || _binding == null) return

        (binding.rvPopularPosts.adapter as? PostAdapter)?.let { adapter ->
            hotPostDatas.clear()
            adapter.notifyDataSetChanged()
            getPopularPost(adapter)
        }
        (binding.rvTips.adapter as? GetAllPostAdapter)?.let { adapter ->
            adapter.submitList(emptyList())
            getTipPost(adapter)
        }
    }
}