package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.api.RetrofitClient
import com.example.dogcatsquare.data.community.BoardSearchResponseDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchBoardActivity : AppCompatActivity() {

    private lateinit var etSearchBoard: EditText
    private lateinit var btnSearch: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var boardAdapter: BoardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_board)

        etSearchBoard = findViewById(R.id.etSearchBoard)
        btnSearch = findViewById(R.id.btnSearchBoard)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        boardAdapter = BoardAdapter(emptyList())
        recyclerView.adapter = boardAdapter

        // 앱 실행 시 모든 게시판 조회
        getAllBoards()

        // 검색 버튼 클릭 이벤트
        btnSearch.setOnClickListener {
            val boardName = etSearchBoard.text.toString().trim()
            Log.d("SearchBoardActivity", "검색 버튼이 클릭됨")
            Log.d("SearchBoardActivity", "입력된 검색어: $boardName")

            if (boardName.isNotEmpty()) {
                searchBoard(boardName)
            } else {
                getAllBoards() // 검색어가 없으면 전체 게시판 다시 표시
            }
        }

        // 키보드 엔터 입력 시 검색 실행
        etSearchBoard.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                btnSearch.performClick() // 엔터를 누르면 검색 버튼 클릭 실행
                true
            } else {
                false
            }
        }
    }

    // 모든 게시판 조회 API 호출
    private fun getAllBoards() {
        RetrofitClient.instance.getAllBoards()
            .enqueue(object : Callback<BoardSearchResponseDto> {
                override fun onResponse(
                    call: Call<BoardSearchResponseDto>,
                    response: Response<BoardSearchResponseDto>
                ) {
                    if (response.isSuccessful) {
                        val boardList = response.body()?.result ?: emptyList()
                        boardAdapter.submitList(boardList) // RecyclerView 업데이트
                        Log.d("SearchBoardActivity", "모든 게시판 조회 완료: ${boardList.size}개")
                    } else {
                        Toast.makeText(this@SearchBoardActivity, "게시판 조회 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BoardSearchResponseDto>, t: Throwable) {
                    Log.e("SearchBoardActivity", "서버 연결 실패", t)
                    Toast.makeText(this@SearchBoardActivity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // 검색 API 호출
    private fun searchBoard(boardName: String) {
        RetrofitClient.instance.searchBoard(boardName)
            .enqueue(object : Callback<BoardSearchResponseDto> {
                override fun onResponse(
                    call: Call<BoardSearchResponseDto>,
                    response: Response<BoardSearchResponseDto>
                ) {
                    if (response.isSuccessful) {
                        val boardList = response.body()?.result ?: emptyList()

                        // 검색어 포함 여부에 따라 정렬 우선순위 적용
                        val sortedList = boardList.sortedByDescending { board ->
                            val isExactTitleMatch = board.boardName.equals(boardName, ignoreCase = true)
                            val isTitleContains = board.boardName.contains(boardName, ignoreCase = true)
                            val isKeywordMatch = board.keywords?.any { it.contains(boardName, ignoreCase = true) }

                            when {
                                isExactTitleMatch -> 3
                                isTitleContains -> 2
                                isKeywordMatch == true -> 1
                                else -> 0
                            }
                        }

                        boardAdapter.submitList(sortedList) // RecyclerView 업데이트
                        Log.d("SearchBoardActivity", "검색 결과 업데이트 완료: ${sortedList.size}개")
                    } else {
                        Toast.makeText(this@SearchBoardActivity, "검색 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BoardSearchResponseDto>, t: Throwable) {
                    Log.e("SearchBoardActivity", "서버 연결 실패", t)
                    Toast.makeText(this@SearchBoardActivity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
