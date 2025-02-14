package com.example.dogcatsquare.ui.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.api.RetrofitClient
import com.example.dogcatsquare.data.community.BoardSearchResponseDto
import com.example.dogcatsquare.databinding.ActivityBoardSettingsBinding
import com.example.dogcatsquare.ui.community.BoardAdapter
import com.example.dogcatsquare.ui.community.BoardCreateActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoardSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardSettingsBinding
    private lateinit var boardAdapter: BoardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 게시판 만들기 버튼
        binding.btnCreateBoard.setOnClickListener {
            val intent = Intent(this, BoardCreateActivity::class.java)
            startActivity(intent)
        }


        // RecyclerView 초기화 (XML과 ID 확인!)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        boardAdapter = BoardAdapter(emptyList())
        binding.recyclerView.adapter = boardAdapter

        // 앱 실행 시 모든 게시판 불러오기
        getAllBoards()

        // 검색 버튼 클릭 시 실행
        binding.btnSearchBoard.setOnClickListener {
            val searchQuery = binding.etSearchBoard.text.toString().trim()
            Log.d("BoardSettingsActivity", "✅ 검색 버튼 클릭됨, 입력값: $searchQuery")
            if (searchQuery.isNotEmpty()) {
                searchBoard(searchQuery)
            } else {
                getAllBoards() // 검색어가 없으면 전체 게시판 조회
            }
        }

        // 엔터 키 입력 시 검색 실행
        binding.etSearchBoard.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                binding.btnSearchBoard.performClick() // 엔터 입력 시 검색 버튼 클릭 실행
                true
            } else {
                false
            }
        }
    }

    // 모든 게시판 조회 API
    private fun getAllBoards() {
        RetrofitClient.instance.getAllBoards()
            .enqueue(object : Callback<BoardSearchResponseDto> {
                override fun onResponse(
                    call: Call<BoardSearchResponseDto>,
                    response: Response<BoardSearchResponseDto>
                ) {
                    if (response.isSuccessful) {
                        val boardList = response.body()?.result ?: emptyList()
                        boardAdapter.submitList(boardList)
                        Log.d("BoardSettingsActivity", "모든 게시판 조회 완료: ${boardList.size}개")
                    } else {
                        Toast.makeText(this@BoardSettingsActivity, "게시판 조회 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BoardSearchResponseDto>, t: Throwable) {
                    Log.e("BoardSettingsActivity", "서버 연결 실패", t)
                    Toast.makeText(this@BoardSettingsActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // 게시판 검색 API
    private fun searchBoard(boardName: String) {
        RetrofitClient.instance.searchBoard(boardName)
            .enqueue(object : Callback<BoardSearchResponseDto> {
                override fun onResponse(
                    call: Call<BoardSearchResponseDto>,
                    response: Response<BoardSearchResponseDto>
                ) {
                    if (response.isSuccessful) {
                        val boardList = response.body()?.result ?: emptyList()

                        // 검색어 포함 여부에 따라 정렬 (제목 포함 > 키워드 포함 순)
                        val sortedList = boardList.sortedByDescending { board ->
                            val isTitleMatch = board.boardName.contains(boardName, ignoreCase = true)
                            val isKeywordMatch = board.keywords.any { it.contains(boardName, ignoreCase = true) }

                            when {
                                isTitleMatch -> 2
                                isKeywordMatch -> 1
                                else -> 0
                            }
                        }

                        boardAdapter.submitList(sortedList)
                        Log.d("BoardSettingsActivity", "검색 결과 업데이트 완료: ${sortedList.size}개")
                    } else {
                        Toast.makeText(this@BoardSettingsActivity, "검색 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BoardSearchResponseDto>, t: Throwable) {
                    Log.e("BoardSettingsActivity", "서버 연결 실패", t)
                    Toast.makeText(this@BoardSettingsActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
