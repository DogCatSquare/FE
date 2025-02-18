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
import com.example.dogcatsquare.data.community.LocalPost
import com.example.dogcatsquare.data.community.Post
import com.example.dogcatsquare.data.community.Tip
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

        // 더미 데이터 생성 (Post 데이터 클래스에 맞게 모든 필드 지정)
        val popularPosts = listOf(
            createPost(
                id = 1L,
                board = "자유게시판",
                username = "닉네임",
                dogbreed = "포메라니안",
                title = "제목을 입력해주세요",
                content = "내용을 입력해주세요 내용을 입력해주세요...",
                videoUrl = null,
                thumbnailUrl = null,
                profileImageUrl = null,
                images = null,
                likeCount = 6,
                commentCount = 1,
                createdAt = "1시간 전"
            ),
            createPost(
                id = 2L,
                board = "자유게시판",
                username = "닉네임",
                dogbreed = "포메라니안",
                title = "제목을 입력해주세요",
                content = "내용을 입력해주세요 내용을 입력해주세요...",
                videoUrl = null,
                thumbnailUrl = null,
                profileImageUrl = null,
                images = null,
                likeCount = 6,
                commentCount = 1,
                createdAt = "1시간 전"
            )
        )

        val tips = listOf(
            Tip(
                title = "강아지 산책할 때 주의할 점",
                content = "내용을 입력해주세요 내용을 입력해주세요...",
                thumbnailResId = R.drawable.ic_sample_image,
                nickname = "닉네임1",
                time = "1시간 전",
                likeCount = 6,
                commentCount = 1,
                dogBreed = "포메라니안",
                date = "2024-01-01"
            ),
            Tip(
                title = "강아지 간식 추천",
                content = "내용을 입력해주세요 내용을 입력해주세요...",
                thumbnailResId = R.drawable.ic_sample_image,
                nickname = "닉네임2",
                time = "2시간 전",
                likeCount = 8,
                commentCount = 3,
                dogBreed = "말티즈",
                date = "2024-01-02"
            )
        )



        val localPosts = listOf(
            LocalPost(
                id = 1L,  // Long 타입 id
                username = "닉네임1",
                dogbreed = "포메라니안",
                title = "강아지와 놀기",
                content = "새로 사준 장난감으로 놀아줬더니 기절한 듯이 잠들었어요ㅎ\n이제 5개월인데 미친 듯이 놀아서 너무 귀엽네요 새벽에...",
                video_URL = null,
                thumbnail_URL = null,
                images = listOf(R.drawable.sample_image1, R.drawable.sample_image2)
            ),
            LocalPost(
                id = 2L,
                username = "닉네임2",
                dogbreed = "말티즈",
                title = "새로운 애완동물 용품 추천",
                content = "새로 사준 장난감으로 놀아줬더니 기절한 듯이 잠들었어요ㅎ\n이제 5개월인데 미친 듯이 놀아서 너무 귀엽네요 새벽에...",
                video_URL = null,
                thumbnail_URL = null,
                images = emptyList()
            )
        )

        // RecyclerView 설정
        setupPopularPostsRecyclerView(popularPosts)
        setupTipsRecyclerView(tips)
        setupLocalPostsRecyclerView(localPosts)
    }

    // Post 객체 생성용 팩토리 함수 (Post 데이터 클래스에 맞게 수정)
    private fun createPost(
        id: Long,
        board: String,
        username: String,
        dogbreed: String,
        title: String?,
        content: String?,
        videoUrl: String? = null,
        thumbnailUrl: String? = null,
        profileImageUrl: String? = null,
        images: List<String>? = null,
        likeCount: Int,
        commentCount: Int,
        createdAt: String?
    ): Post {
        return Post(
            id = id,
            board = board,
            username = username,
            dogbreed = dogbreed,
            title = title ?: "",
            content = content ?: "",
            videoUrl = videoUrl ?: "",
            thumbnailUrl = thumbnailUrl ?: "",
            profileImageUrl = profileImageUrl ?: "",
            images = images,
            likeCount = likeCount,
            commentCount = commentCount,
            createdAt = createdAt ?: ""
        )
    }


    private fun setupPopularPostsRecyclerView(popularPosts: List<Post>) {
        postAdapter = PostAdapter(popularPosts)
        binding.rvPopularPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun setupTipsRecyclerView(tips: List<Tip>) {
        tipsAdapter = TipsAdapter(tips, isCompactView = true, isExpanded = false) { selectedTip ->
            // 토스트 대신 상세 화면으로 이동하는 코드
            val intent = Intent(requireContext(), TipDetailActivity::class.java).apply {
                putExtra("title", selectedTip.title)
                putExtra("content", selectedTip.content)
                putExtra("thumbnailResId", selectedTip.thumbnailResId)
                putExtra("dogBreed", selectedTip.dogBreed)
                putExtra("date", selectedTip.date)
                putExtra("nickname", selectedTip.nickname)
                putExtra("time", selectedTip.time)
                putExtra("likeCount", selectedTip.likeCount)
                putExtra("commentCount", selectedTip.commentCount)
            }
            startActivity(intent)
        }
        binding.rvTips.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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
