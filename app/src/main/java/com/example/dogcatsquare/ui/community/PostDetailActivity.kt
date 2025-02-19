package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.PostDetailResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ActivityPostDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var comments: MutableList<Comment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root) // 🔥 ViewBinding 적용

        val postId = intent.getIntExtra("postId", -1)

        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener { finish() }

        // 댓글 RecyclerView 설정
        comments = mutableListOf(
            Comment("닉네임1", "더 열심히 놀아주세요!", "2021.01.01"),
            Comment("닉네임2", "대댓", "2021.01.01")
        )
        commentAdapter = CommentAdapter(comments)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter

        // 댓글 입력 후 전송 버튼 클릭 시 처리
        binding.ivSend.setOnClickListener {
            val commentText = binding.etComment.text.toString()
            if (commentText.isNotBlank()) {
                comments.add(Comment("나", commentText, "방금 전"))
                commentAdapter.notifyItemInserted(comments.size - 1)
                binding.rvComments.scrollToPosition(comments.size - 1)
                binding.etComment.text.clear()
            }
        }

        // postId 확인 후 API 호출
        Log.d("PostDetailActivity", "Received postId: $postId")
        if (postId != -1) {
            loadPostDetail(postId)
        } else {
            Toast.makeText(this, "게시글 ID가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadPostDetail(postId: Int) {
        val boardApiService = RetrofitObj.getRetrofit().create(
            com.example.dogcatsquare.data.api.BoardApiService::class.java
        )
        boardApiService.getPost(postId).enqueue(object : Callback<PostDetailResponse> {
            override fun onResponse(
                call: Call<PostDetailResponse>,
                response: Response<PostDetailResponse>
            ) {
                Log.d("PostDetailActivity", "API Response Code: ${response.code()}")
                if (response.isSuccessful) {
                    val postDetail = response.body()?.result
                    Log.d("PostDetailActivity", "Post detail received: $postDetail")
                    if (postDetail != null) {
                        binding.tvPostTitle.text = postDetail.title
                        binding.tvPostContent.text = postDetail.content
                        binding.tvLikeCount.text = postDetail.likeCount.toString()
                        binding.tvCommentCount.text = postDetail.commentCount.toString()
                        binding.tvDate.text = postDetail.createdAt
                        Glide.with(this@PostDetailActivity)
                            .load(postDetail.profileImageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(binding.ivProfile)

                        // 이미지가 존재하는 경우 표시, 없으면 GONE 처리
                        if (!postDetail.images.isNullOrEmpty()) {
                            val imageViews = listOf(
                                binding.ivPostImage1,
                                binding.ivPostImage2,
                                binding.ivPostImage3,
                                binding.ivPostImage4,
                                binding.ivPostImage5
                            )

                            // 이미지 최대 5개까지 표시
                            for (i in imageViews.indices) {
                                if (i < postDetail.images.size) {
                                    imageViews[i].visibility = View.VISIBLE
                                    Glide.with(this@PostDetailActivity)
                                        .load(postDetail.images[i])
                                        .placeholder(R.drawable.ic_placeholder)
                                        .into(imageViews[i])
                                } else {
                                    imageViews[i].visibility = View.GONE
                                }
                            }
                        } else {
                            // 이미지가 없으면 모든 ImageView 숨기기
                            binding.ivPostImage1.visibility = View.GONE
                            binding.ivPostImage2.visibility = View.GONE
                            binding.ivPostImage3.visibility = View.GONE
                            binding.ivPostImage4.visibility = View.GONE
                            binding.ivPostImage5.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(
                            this@PostDetailActivity,
                            "게시글 정보가 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@PostDetailActivity,
                        "게시글 조회 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PostDetailResponse>, t: Throwable) {
                Log.e("PostDetailActivity", "API 호출 실패", t)
                Toast.makeText(
                    this@PostDetailActivity,
                    "네트워크 오류: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}