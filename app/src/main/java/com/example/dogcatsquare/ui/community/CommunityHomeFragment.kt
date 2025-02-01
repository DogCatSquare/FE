package com.example.dogcatsquare.ui.community

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentCommunityHomeBinding

class CommunityHomeFragment : Fragment(R.layout.fragment_community_home) {

    private lateinit var binding: FragmentCommunityHomeBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var tipsAdapter: TipsAdapter
    private lateinit var localPostAdapter: LocalPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 더미 데이터 생성
        val popularPosts = listOf(
            Post("닉네임", "포메라니안", "제목을 입력해주세요", "내용을 입력해주세요 내용을 입력해주세요...", "1시간 전", null, 6, 1),
            Post("닉네임", "포메라니안", "제목을 입력해주세요", "내용을 입력해주세요 내용을 입력해주세요...", "1시간 전", null, 6, 1)
        )

        val tips = listOf(
            Tip("강아지 산책할 때 주의할 점", "내용을 입력해주세요 내용을 입력해주세요...", R.drawable.ic_sample_image),
            Tip("강아지 간식 추천", "내용을 입력해주세요 내용을 입력해주세요...", R.drawable.ic_sample_image)
        )

        val localPosts = listOf(
            LocalPost(
                id = "post1",
                username = "닉네임1",
                dogbreed = "포메라니안",
                title = "강아지와 놀기", // 🔹 추가된 필드
                content = "새로 사준 장난감으로 놀아줬더니 기절한 듯이 잠들었어요ㅎ\n이제 5개월인데 미친 듯이 놀아서 너무 귀엽네요 새벽에...",
                video_URL = null, // 🔹 추가된 필드
                thumbnail_URL = null, // 🔹 추가된 필드
                images = listOf(R.drawable.sample_image1, R.drawable.sample_image2)
            ),
            LocalPost(
                id = "post2",
                username = "닉네임2",
                dogbreed = "말티즈",
                title = "새로운 애완동물 용품 추천", // 🔹 추가된 필드
                content = "새로 사준 장난감으로 놀아줬더니 기절한 듯이 잠들었어요ㅎ\n이제 5개월인데 미친 듯이 놀아서 너무 귀엽네요 새벽에...",
                video_URL = null,
                thumbnail_URL = null,
                images = emptyList()
            )
        )


        // RecyclerView 설정
        setupPopularPostsRecyclerView(popularPosts)
        setupTipsRecyclerView(tips)
        setupLocalPostsRecyclerView(localPosts) // 수정된 List<LocalPost> 전달
    }

    private fun setupPopularPostsRecyclerView(popularPosts: List<Post>) {
        postAdapter = PostAdapter(popularPosts)
        binding.rvPopularPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun setupTipsRecyclerView(tips: List<Tip>) {
        tipsAdapter = TipsAdapter(tips, isCompactView = true) { selectedTip ->
            // 클릭 이벤트 처리
            Toast.makeText(requireContext(), "${selectedTip.title} 클릭됨", Toast.LENGTH_SHORT).show()
        }
        binding.rvTips.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = tipsAdapter
        }
    }

    private fun setupLocalPostsRecyclerView(localPosts: List<LocalPost>) {
        localPostAdapter = LocalPostAdapter(
            requireContext(),
            localPosts.toMutableList(),
            onEditPost = { post -> editPost(post) },
            onDeletePost = { position -> deletePost(position) },
            isCompactView = true
        )
        binding.rvLocalPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = localPostAdapter
        }
    }

    private fun editPost(post: LocalPost) {
        val intent = Intent(requireContext(), EditPostActivity::class.java)
        intent.putExtra("POST_ID", post.id)
        startActivity(intent)
    }

    private fun deletePost(position: Int) {
        localPostAdapter.removePost(position)
    }
}