package com.example.dogcatsquare.ui.community

import android.os.Bundle
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

        btnSearch.setOnClickListener {
            val boardName = etSearchBoard.text.toString()
            if (boardName.isNotEmpty()) {
                searchBoard(boardName)
            } else {
                Toast.makeText(this, "게시판 이름을 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchBoard(boardName: String) {
        RetrofitClient.instance.searchBoard(boardName)
            .enqueue(object : Callback<BoardSearchResponseDto> {
                override fun onResponse(call: Call<BoardSearchResponseDto>, response: Response<BoardSearchResponseDto>) {
                    if (response.isSuccessful) {
                        val boardList = response.body()?.result ?: emptyList()
                        boardAdapter.updateData(boardList)
                    } else {
                        Toast.makeText(this@SearchBoardActivity, "검색 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BoardSearchResponseDto>, t: Throwable) {
                    Toast.makeText(this@SearchBoardActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
