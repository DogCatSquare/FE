package com.example.dogcatsquare.ui.community

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentCommunityLocalBinding

class CommunityLocalFragment : Fragment(R.layout.fragment_community_local) {

    private lateinit var binding: FragmentCommunityLocalBinding
    private lateinit var localPostAdapter: LocalPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityLocalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 글쓰기 버튼 클릭 이벤트
        binding.btnAddPost.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            startActivity(intent)
        }

        // 더미 데이터 생성 (LocalPost 타입으로 변경)
        val localPosts = listOf(
            LocalPost(
                username = "닉네임",
                dogbreed = "포메라니안",
                images = listOf(R.drawable.sample_image1, R.drawable.sample_image2),
                content = "새로 사준 장난감으로 놀아줬더니 기절한 듯이 잠들었어요. 이제 5개월인데 미친 듯이 놀아서 너무 귀엽네요. 새벽에 우다다만 안하면 정말 좋을 텐데 방법이 없을까..."
            ),
            LocalPost(
                username = "닉네임",
                dogbreed = "포메라니안",
                images = emptyList(), // 이미지가 없을 경우 빈 리스트 사용
                content = "새로 사준 장난감으로 놀아줬더니 기절한 듯이 잠들었어요. 이제 5개월인데 미친 듯이 놀아서 너무 귀엽네요. 새벽에 우다다만 안하면 정말 좋을 텐데 방법이 없을까..."
            )
        )

        // 어댑터 설정
        localPostAdapter = LocalPostAdapter(localPosts)

        // RecyclerView 설정
        binding.rvLocalPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = localPostAdapter
        }
    }
}
