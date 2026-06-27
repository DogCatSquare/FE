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
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentMyCommunityBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import android.widget.Toast
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.ui.community.EditPostActivity

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

        binding.swipeRefresh.setOnRefreshListener {
            setupMyCommunityRecyclerView()
        }

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setupMyCommunityRecyclerView()
    }

    private fun setupMyCommunityRecyclerView() {
        communityDatas.clear()

        val myCommunityRVAdpater = MyCommunityRVAdapter(communityDatas, { post ->
            editPost(post)
        }, { post ->
            deletePost(post)
        })
        binding.myCommunityRv.adapter = myCommunityRVAdpater
        binding.myCommunityRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true).apply {
            stackFromEnd = true
        }

        getMyPost(myCommunityRVAdpater)
    }

    private fun editPost(post: com.example.dogcatsquare.data.model.post.Post) {
        val intent = Intent(requireContext(), EditPostActivity::class.java).apply {
            putExtra("postId", post.id)
            putExtra("title", post.title)
            putExtra("content", post.content)
            putExtra("videoUrl", post.video_URL)
            putExtra("imageUrl", post.thumbnail_URL)
            putExtra("boardType", post.board ?: "자유게시판")
            putStringArrayListExtra("images", ArrayList(post.images?.filterNotNull().orEmpty()))
        }
        startActivity(intent)
    }

    private fun deletePost(post: com.example.dogcatsquare.data.model.post.Post) {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("게시글 삭제")
            .setMessage("정말 이 게시글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                val boardApi = RetrofitObj.getRetrofit(requireContext()).create(BoardApiService::class.java)
                boardApi.deletePost("Bearer $token", post.id)
                    .enqueue(object : Callback<com.example.dogcatsquare.data.model.community.ApiResponse<Unit>> {
                        override fun onResponse(
                            call: Call<com.example.dogcatsquare.data.model.community.ApiResponse<Unit>>,
                            response: Response<com.example.dogcatsquare.data.model.community.ApiResponse<Unit>>
                        ) {
                            if (response.isSuccessful && response.body()?.isSuccess == true) {
                                Toast.makeText(context, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                setupMyCommunityRecyclerView()
                            } else {
                                Toast.makeText(context, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<com.example.dogcatsquare.data.model.community.ApiResponse<Unit>>, t: Throwable) {
                            Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.main_color1))
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.main_color1))
    }

    private fun getMyPost(adapter: MyCommunityRVAdapter) {
        val token = getToken()
        val userId = getUserId()

        val getMyPostService = RetrofitObj.getRetrofit(requireContext()).create(MyPageRetrofitItf::class.java)
        getMyPostService.getMyPost("Bearer $token", userId).enqueue(object : Callback<GetMyPostResponse> {
            override fun onResponse(call: Call<GetMyPostResponse>, response: Response<GetMyPostResponse>) {
                binding.swipeRefresh.isRefreshing = false
                Log.d("GetMyPost/SUCCESS", response.toString())
                if (response.isSuccessful) {
                    val resp = response.body()
                    if (resp != null && resp.isSuccess) {
                        Log.d("GetMyPost", "내 게시물 전체 조회 성공")

                        val resultList = resp.result
                        if (!resultList.isNullOrEmpty()) {
                            binding.emptyView.visibility = View.GONE
                            binding.myCommunityRv.visibility = View.VISIBLE
                            val myPost = resultList.map { post ->
                                com.example.dogcatsquare.data.model.post.Post(
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
                                    createdAt = com.example.dogcatsquare.util.DateFmt.format(post.createdAt).replace(".", "-"),
                                    profileImage_URL = post.profileImageUrl
                                )
                            }
                            communityDatas.addAll(myPost)
                            Log.d("GetMyPost", communityDatas.toString())
                            adapter.notifyDataSetChanged()
                        } else {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.myCommunityRv.visibility = View.GONE
                        }
                    } else {
                        Log.e("GetMyPost/ERROR", "응답 실패: ${resp?.message}")
                        binding.emptyView.visibility = View.VISIBLE
                        binding.myCommunityRv.visibility = View.GONE
                    }
                } else {
                    Log.e("GetMyPost/ERROR", "응답 코드 실패: ${response.code()}")
                    binding.emptyView.visibility = View.VISIBLE
                    binding.myCommunityRv.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<GetMyPostResponse>, t: Throwable) {
                binding.swipeRefresh.isRefreshing = false
                Log.d("RETROFIT/FAILURE", t.message.toString())
                binding.emptyView.visibility = View.VISIBLE
                binding.myCommunityRv.visibility = View.GONE
            }

        })
    }
}