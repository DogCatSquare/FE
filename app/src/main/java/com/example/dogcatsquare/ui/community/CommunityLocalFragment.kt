package com.example.dogcatsquare.ui.community

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentCommunityLocalBinding

class CommunityLocalFragment : Fragment(R.layout.fragment_community_local) {

    private lateinit var binding: FragmentCommunityLocalBinding
    private lateinit var localPostAdapter: LocalPostAdapter

    val localPosts = mutableListOf(
        LocalPost(
            id = "1",
            username = "닉네임1",
            dogbreed = "포메라니안",
            images = listOf(R.drawable.sample_image1, R.drawable.sample_image2),
            content = "새로 사준 장난감으로 놀아줬더니 기절한 듯이 잠들었어요\n" +
                    "이제 5개월인데 미친 듯이 놀아서 너무 귀엽네요 \n" +
                    "새벽에 우다다만 안하면 정말 좋을 텐데 방법이 없을까...",
            title = "새로운 장난감을 사줬어요",
            video_URL = "",
            thumbnail_URL = "https://example.com/sample_thumbnail.jpg"
        ),
        LocalPost(
            id = "2",
            username = "닉네임2",
            dogbreed = "말티즈",
            images = emptyList(),
            content = "새로 사준 장난감으로 놀아줬더니 기절한 듯이 잠들었어요\n" +
                    "이제 5개월인데 미친 듯이 놀아서 너무 귀엽네요 \n" +
                    "새벽에 우다다만 안하면 정말 좋을 텐데 방법이 없을까...",
            title = "강아지 산책 후기",
            video_URL = "",
            thumbnail_URL = ""
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityLocalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true) // ✅ 프래그먼트에서 메뉴 사용 활성화

        // 글쓰기 버튼 클릭 이벤트
        binding.btnAddPost.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            startActivity(intent)
        }

        // 게시글 수정 및 삭제 기능 추가
        localPostAdapter = LocalPostAdapter(
            requireContext(),
            localPosts,
            ::editPost, // 게시글 수정 함수
            ::deletePost, // 게시글 삭제 함수
            isCompactView = false
        )

        // RecyclerView 설정
        binding.rvLocalPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = localPostAdapter
        }
        binding.rvLocalPosts.post {
            localPostAdapter.notifyDataSetChanged()
        }
    }

    // ✅ 옵션 메뉴 생성 (게시글 수정, 삭제 버튼 추가)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_menu, menu)
        Log.d("CommunityLocalFragment", "onCreateOptionsMenu 실행됨! 메뉴 로드 완료") // ✅ 로그 추가
        super.onCreateOptionsMenu(menu, inflater)
    }

    // ✅ 옵션 메뉴 클릭 이벤트
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("CommunityLocalFragment", "onOptionsItemSelected 실행됨! 선택된 메뉴 ID: ${item.itemId}") // ✅ 로그 추가

        return when (item.itemId) {
            R.id.menu_edit -> {
                Log.d("CommunityLocalFragment", "게시글 수정 버튼 클릭됨!") // ✅ 로그 추가
                val selectedPost = localPosts[0] // ✅ 첫 번째 게시글을 임시 선택
                editPost(selectedPost)
                true
            }
            R.id.menu_delete -> {
                Log.d("CommunityLocalFragment", "게시글 삭제 버튼 클릭됨!") // ✅ 로그 추가
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ✅ 게시글 수정 기능 (수정 화면으로 이동)
    private fun editPost(post: LocalPost) {
        Log.d("CommunityLocalFragment", "editPost() 실행됨!") // ✅ 로그 추가

        val postId = post.id.toLongOrNull() ?: -1L
        if (postId == -1L) {
            Log.e("CommunityLocalFragment", "postId 변환 실패! postId: ${post.id}")
            return
        }

        val intent = Intent(requireContext(), EditPostActivity::class.java).apply {
            putExtra("postId", postId)
            putExtra("title", post.title)
            putExtra("content", post.content)
            putExtra("videoUrl", post.video_URL)
            putExtra("imageUrl", post.thumbnail_URL)
        }

        Log.d("CommunityLocalFragment", "EditPostActivity 시작! postId: $postId") // ✅ 로그 추가
        editPostLauncher.launch(intent)
    }

    // ✅ 게시글 수정 후 결과를 받아 RecyclerView 업데이트
    private val editPostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val updatedPostId = result.data?.getLongExtra("UPDATED_POST_ID", -1L)
                val updatedTitle = result.data?.getStringExtra("UPDATED_TITLE") ?: ""
                val updatedContent = result.data?.getStringExtra("UPDATED_CONTENT") ?: ""
                val updatedVideoUrl = result.data?.getStringExtra("UPDATED_VIDEO_URL") ?: ""
                val updatedImageUrl = result.data?.getStringExtra("UPDATED_IMAGE_URL") ?: ""

                Log.d("CommunityLocalFragment", "수정된 게시글 ID: $updatedPostId") // ✅ 로그 추가

                if (updatedPostId != null && updatedPostId != -1L) {
                    refreshPost(updatedPostId, updatedTitle, updatedContent, updatedVideoUrl, updatedImageUrl)
                }
            }
        }

    // ✅ 수정된 게시글 갱신 함수
    private fun refreshPost(updatedPostId: Long, title: String, content: String, videoUrl: String, imageUrl: String) {
        val index = localPosts.indexOfFirst { it.id.toLongOrNull() == updatedPostId }
        if (index != -1) {
            // ✅ 제목, 내용, 영상, 이미지 업데이트
            localPosts[index] = localPosts[index].copy(
                title = title,
                content = content,
                video_URL = videoUrl,
                thumbnail_URL = imageUrl
            )
            localPostAdapter.notifyItemChanged(index)
        } else {
            Log.e("CommunityLocalFragment", "업데이트할 게시글을 찾을 수 없음: $updatedPostId")
        }
    }

    // ✅ 게시글 삭제 기능
    private fun deletePost(position: Int) {
        localPosts.removeAt(position)
        localPostAdapter.notifyItemRemoved(position)
    }
}
