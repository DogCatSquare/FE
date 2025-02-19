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
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.community.BoardPost
import com.example.dogcatsquare.data.model.post.PopularPostResponse
import com.example.dogcatsquare.data.model.post.Post
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentMyBoardBinding
import com.example.dogcatsquare.ui.home.HomeHotPostRVAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyBoardFragment : Fragment() {
    lateinit var binding: FragmentMyBoardBinding

    private var boardPostDatas = ArrayList<Post>()

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyBoardBinding.inflate(inflater, container, false)

        val boardName = arguments?.getString("board_name") ?: "마이게시판"
        val boardId = arguments?.getInt("board_id") ?: -1

        setupBoardPostRecyclerView(boardId)

        binding.ivCreatePost.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java).apply {
                putExtra("BOARD_ID", boardId)
            }
            startActivity(intent)
        }

        return binding.root
    }

    companion object {
        fun newInstance(boardName: String, boardId: Int): MyBoardFragment {
            val fragment = MyBoardFragment()
            val args = Bundle()
            args.putString("board_name", boardName)
            args.putInt("board_id", boardId)
            fragment.arguments = args
            return fragment
        }
    }

    // hot post rv
    private fun setupBoardPostRecyclerView(id: Int) {
        boardPostDatas.clear()

        // 인기 게시물 recycler view
        val boardPostRVAdapter = MyBoardPostRVAdapter(boardPostDatas)
        binding.rvPosts.adapter = boardPostRVAdapter
        binding.rvPosts.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true).apply {
            stackFromEnd = true
        }

        // 클릭 인터페이스
        boardPostRVAdapter.setMyItemClickListener(object : MyBoardPostRVAdapter.OnItemClickListener {
            override fun onItemClick(post: Post) {
                val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
                    putExtra("postId", post.id)
                }
                startActivity(intent)
            }
        })

        getBoardPostRV(id, boardPostRVAdapter)
    }

    private fun getBoardPostRV(id: Int, adapter: MyBoardPostRVAdapter) {
        val token = getToken()

        val getBoardPostService = RetrofitObj.getRetrofit().create(PostApiService::class.java)
        getBoardPostService.getBoardPost("Bearer $token", id).enqueue(object : Callback<BoardPost> {
            override fun onResponse(call: Call<BoardPost>, response: Response<BoardPost>) {
                if (response.isSuccessful) {
                    // 응답 본문이 null일 수도 있으므로 `?.let`을 사용해 예외 처리
                    response.body()?.let { resp ->
                        if (resp.isSuccess) {
                            Log.d("GetBoardPost", "게시물 전체 조회 성공")

                            val posts = resp.result?.map { post ->
                                Post(
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
                            } ?: emptyList() // 게시물이 없을 경우 빈 리스트 반환

                            boardPostDatas.clear()
                            boardPostDatas.addAll(posts)
                            Log.d("HotPostList", boardPostDatas.toString())
                            adapter.notifyDataSetChanged()
                        } else {
                            Log.e("GetBoardPost/ERROR", "API 요청은 성공했지만 isSuccess가 false")
                        }
                    } ?: Log.e("GetBoardPost/ERROR", "응답 body가 null입니다.")
                } else {
                    // 500 오류 처리 (게시물이 없을 경우에도 빈 화면 유지)
                    Log.e("GetBoardPost/ERROR", "응답 코드: ${response.code()}")
                    if (response.code() == 500) {
                        boardPostDatas.clear()
                        adapter.notifyDataSetChanged() // 빈 화면 표시
                    }
                }
            }

            override fun onFailure(call: Call<BoardPost>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", "네트워크 오류: ${t.message}")
            }
        })
    }

}