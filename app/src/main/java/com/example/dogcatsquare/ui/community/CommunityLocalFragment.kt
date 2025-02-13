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
import com.example.dogcatsquare.data.community.LocalPost
import com.example.dogcatsquare.databinding.FragmentCommunityLocalBinding

class CommunityLocalFragment : Fragment(R.layout.fragment_community_local) {

    private lateinit var binding: FragmentCommunityLocalBinding
    private lateinit var localPostAdapter: LocalPostAdapter

    // Dummy 데이터: id는 Long 타입으로 지정
    private val localPosts = mutableListOf(
        LocalPost(
            id = 1L,
            username = "닉네임1",
            dogbreed = "포메라니안",
            title = "새로운 장난감을 사줬어요",
            content = "새로 사준 장난감으로 놀아줬더니 기절한 듯이 잠들었어요\n이제 5개월인데 미친 듯이 놀아서 너무 귀엽네요\n새벽에 우다다만 안하면 정말 좋을 텐데 방법이 없을까...",
            video_URL = "",
            thumbnail_URL = "https://example.com/sample_thumbnail.jpg",
            images = listOf(R.drawable.sample_image1, R.drawable.sample_image2)
        ),
        LocalPost(
            id = 2L,
            username = "닉네임2",
            dogbreed = "말티즈",
            title = "강아지 산책 후기",
            content = "새로 사준 장난감으로 놀아줬더니 기절한 듯이 잠들었어요\n이제 5개월인데 미친 듯이 놀아서 너무 귀엽네요\n새벽에 우다다만 안하면 정말 좋을 텐데 방법이 없을까...",
            video_URL = "",
            thumbnail_URL = "",
            images = emptyList()
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

        setHasOptionsMenu(true) // 프래그먼트에서 메뉴 사용 활성화

        // 글쓰기 버튼 클릭 이벤트
        binding.btnAddPost.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            startActivity(intent)
        }

        // 게시글 수정 및 삭제 기능을 위한 어댑터 초기화
        localPostAdapter = LocalPostAdapter(
            requireContext(),
            localPosts,
            onEditPost = { post -> editPost(post) },
            onDeletePost = { position -> deletePost(position) },
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

    // 옵션 메뉴 생성 (게시글 수정, 삭제 버튼 등)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_menu, menu)
        Log.d("CommunityLocalFragment", "onCreateOptionsMenu 실행됨! 메뉴 로드 완료")
        super.onCreateOptionsMenu(menu, inflater)
    }

    // 옵션 메뉴 클릭 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("CommunityLocalFragment", "onOptionsItemSelected 실행됨! 선택된 메뉴 ID: ${item.itemId}")
        return when (item.itemId) {
            R.id.menu_edit -> {
                Log.d("CommunityLocalFragment", "게시글 수정 버튼 클릭됨!")
                val selectedPost = localPosts[0] // 임시로 첫 번째 게시글 선택
                editPost(selectedPost)
                true
            }
            R.id.menu_delete -> {
                Log.d("CommunityLocalFragment", "게시글 삭제 버튼 클릭됨!")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 게시글 수정 기능: EditPostActivity로 이동
    private fun editPost(post: LocalPost) {
        Log.d("CommunityLocalFragment", "editPost() 실행됨!")
        // 이제 post.id는 Long 타입이므로 바로 사용
        if (post.id == -1L) {
            Log.e("CommunityLocalFragment", "postId 변환 실패! postId: ${post.id}")
            return
        }
        val intent = Intent(requireContext(), EditPostActivity::class.java).apply {
            putExtra("postId", post.id)
            putExtra("title", post.title)
            putExtra("content", post.content)
            putExtra("videoUrl", post.video_URL)
            putExtra("imageUrl", post.thumbnail_URL)
        }
        Log.d("CommunityLocalFragment", "EditPostActivity 시작! postId: ${post.id}")
        editPostLauncher.launch(intent)
    }

    // 게시글 수정 후 결과를 받아 RecyclerView 업데이트
    private val editPostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val updatedPostId = result.data?.getLongExtra("UPDATED_POST_ID", -1L)
                val updatedTitle = result.data?.getStringExtra("UPDATED_TITLE") ?: ""
                val updatedContent = result.data?.getStringExtra("UPDATED_CONTENT") ?: ""
                val updatedVideoUrl = result.data?.getStringExtra("UPDATED_VIDEO_URL") ?: ""
                val updatedImageUrl = result.data?.getStringExtra("UPDATED_IMAGE_URL") ?: ""
                Log.d("CommunityLocalFragment", "수정된 게시글 ID: $updatedPostId")
                if (updatedPostId != null && updatedPostId != -1L) {
                    refreshPost(updatedPostId, updatedTitle, updatedContent, updatedVideoUrl, updatedImageUrl)
                }
            }
        }

    // 수정된 게시글 갱신 함수
    private fun refreshPost(
        updatedPostId: Long,
        title: String,
        content: String,
        videoUrl: String,
        imageUrl: String
    ) {
        val index = localPosts.indexOfFirst { it.id == updatedPostId }
        if (index != -1) {
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

    // 게시글 삭제 기능
    private fun deletePost(position: Int) {
        localPosts.removeAt(position)
        localPostAdapter.notifyItemRemoved(position)
    }
}
