package com.example.dogcatsquare.ui.community

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.api.RetrofitClient
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.community.BoardSearchResponseDto
import com.example.dogcatsquare.data.community.DeleteMyBoardResponse
import com.example.dogcatsquare.data.community.MyBoardResponse
import com.example.dogcatsquare.data.community.MyBoardResult
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ActivityBoardSettingsBinding
import com.example.dogcatsquare.ui.community.BoardAdapter
//import com.example.dogcatsquare.ui.community.BoardCreateActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

class BoardSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardSettingsBinding
    private lateinit var boardAdapter: BoardAdapter
    private lateinit var myBoardRVAdapter: AddMyBoardRVAdapter

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

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
        boardAdapter = BoardAdapter(this) { myBoard ->
            addMyBoard(myBoard)
        }
        binding.recyclerView.adapter = boardAdapter
        boardAdapter.notifyDataSetChanged()

        binding.addMyBoardRecyclerView.layoutManager = LinearLayoutManager(this)
        myBoardRVAdapter = AddMyBoardRVAdapter(this) { myBoard ->
            deleteMyBoard(myBoard) // 🔹 삭제 클릭 시 API 호출
        }
        binding.addMyBoardRecyclerView.adapter = myBoardRVAdapter
        myBoardRVAdapter.notifyDataSetChanged()

        // 앱 실행 시 모든 게시판 불러오기
        getAllMyBoards()
        getAllBoards()

        // 검색 버튼 클릭 시 실행
        binding.btnSearchBoard.setOnClickListener {
            val searchQuery = binding.etSearchBoard.text.toString().trim()
            Log.d("BoardSettingsActivity", "검색 버튼 클릭됨, 입력값: $searchQuery")
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

    override fun onResume() {
        super.onResume()
        val token = getToken()
        if (token != null) {
            boardAdapter.notifyDataSetChanged()
            myBoardRVAdapter.notifyDataSetChanged()
            getAllMyBoards()
            getAllBoards()
        }
    }

    private fun addMyBoard(myBoard: MyBoardResult) {
        val token = getToken()

        val currentList = myBoardRVAdapter.currentList.toMutableList()

        // 🔹 마이게시판이 최대 5개까지만 추가되도록 제한
        if (currentList.size >= 5) {
            Toast.makeText(this@BoardSettingsActivity, "마이게시판은 최대 5개까지만 추가할 수 있습니다", Toast.LENGTH_SHORT).show()
        } else {
            val addMyBoardService = RetrofitObj.getRetrofit().create(BoardApiService::class.java)
            addMyBoardService.addMyBoard("Bearer $token", myBoard.id)
                .enqueue(object : Callback<MyBoardResponse> {
                    override fun onResponse(
                        call: Call<MyBoardResponse>,
                        response: Response<MyBoardResponse>
                    ) {
                        Log.d("AddMyBoard/SUCCESS", response.toString())
                        val resp: MyBoardResponse = response.body()!!

                        if (resp != null) {
                            if (resp.isSuccess) {
                                Log.d("AddMyBoard", "마이게시판 추가 완료")

                                // 🔹 현재 리스트 가져오기
                                val currentList = myBoardRVAdapter.currentList.toMutableList()
                                currentList.add(myBoard)
                                myBoardRVAdapter.submitList(currentList.toList()) {
                                    myBoardRVAdapter.notifyDataSetChanged()
                                }
                            }
                        } else {
                            Log.e("AddMyBoard/ERROR", "응답 코드: ${response.code()}")
                            Toast.makeText(
                                this@BoardSettingsActivity,
                                "이미 마이 게시판에 추가한 게시판입니다",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }

                    override fun onFailure(call: Call<MyBoardResponse>, t: Throwable) {
                        Log.d("RETROFIT/FAILURE", t.message.toString())
                    }
                })
        }
    }

    private fun deleteMyBoard(myBoard: MyBoardResult) {
        val token = getToken()
        val deleteMyBoardService = RetrofitObj.getRetrofit().create(BoardApiService::class.java)

        deleteMyBoardService.deleteMyBoard("Bearer $token", myBoard.id)
            .enqueue(object : Callback<DeleteMyBoardResponse> {
                override fun onResponse(call: Call<DeleteMyBoardResponse>, response: Response<DeleteMyBoardResponse>) {
                    if (response.isSuccessful) {
                        // 🔹 삭제된 항목을 제외한 새로운 리스트 생성
                        val updatedList = myBoardRVAdapter.currentList.filter { it.id != myBoard.id }
                        myBoardRVAdapter.submitList(updatedList) // ✅ UI 즉시 갱신
                        Log.d("BoardSettingsActivity", "마이게시판 삭제 완료")
                    } else {
                        Toast.makeText(this@BoardSettingsActivity, "마이게시판 삭제 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DeleteMyBoardResponse>, t: Throwable) {
                    Log.e("BoardSettingsActivity", "서버 연결 실패", t)
                }
            })
    }

    // 마이게시판 조회 API
    private fun getAllMyBoards() {
        val token = getToken()
        val getAllMyBoardsService = RetrofitObj.getRetrofit().create(BoardApiService::class.java)
        getAllMyBoardsService.getMyBoards("Bearer $token").enqueue(object : Callback<MyBoardResponse> {
            override fun onResponse(
                call: Call<MyBoardResponse>,
                response: Response<MyBoardResponse>
            ) {
                val myBoardResponse = response.body()!!

                if (response.isSuccessful) {
                    val myBoardList = myBoardResponse.result
                    myBoardRVAdapter.submitList(myBoardList)
                } else {
                    Toast.makeText(this@BoardSettingsActivity, "마이게시판 조회 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyBoardResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }
        })
    }

    // 모든 게시판 조회 API
    private fun getAllBoards() {
        val token = getToken()
        RetrofitClient.instance.getAllBoards("Bearer $token")
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
        val token = getToken()
        RetrofitClient.instance.searchBoard("Bearer $token", boardName)
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
                            val isKeywordMatch = board.keywords?.any { it.contains(boardName, ignoreCase = true) }

                            when {
                                isTitleMatch -> 2
                                isKeywordMatch == true -> 1
                                else -> 0
                            }
                        }

                        boardAdapter.submitList(sortedList)
                        Log.d("BoardSettingsActivity", "검색 결과 업데이트 완료: ${sortedList.size}개")
                    } else {
                        Toast.makeText(this@BoardSettingsActivity, "검색 결과가 존재 하지 않습니다", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BoardSearchResponseDto>, t: Throwable) {
                    Log.e("BoardSettingsActivity", "서버 연결 실패", t)
                    Toast.makeText(this@BoardSettingsActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
