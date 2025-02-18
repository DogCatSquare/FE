package com.example.dogcatsquare.ui.community

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.community.BoardRequestDto
import com.example.dogcatsquare.data.community.BoardResponseDto
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ActivityBoardCreateBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoardCreateActivity : AppCompatActivity() {
    lateinit var binding: ActivityBoardCreateBinding

    private lateinit var keywordAdapter: KeywordRVAdapter
    private val keywordList = mutableListOf<String>() // 키워드 리스트

    private var isTitle: Boolean = false
    private var isContent: Boolean = false

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()

        binding.editBoardName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                binding.boardNameCount.text = "$currentLength/8"
                isTitle = currentLength > 0
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        // 🔹 뒤로가기 버튼 설정
        binding.ivBack.setOnClickListener { finish() }

        // 🔹 입력값 변경 시 버튼 활성화 체크
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCompleteButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.editBoardDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                binding.boardDescriptionCount.text = "$currentLength/20"
                isContent = currentLength > 0
                updateCompleteButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnComplete.setOnClickListener {
            if (isTitle && isContent) {
                createBoard()
            }
        }

        updateCompleteButtonState()
    }

    private fun updateCompleteButtonState() {
        if (isTitle && isContent) {
            binding.btnComplete.isEnabled = true
            binding.btnComplete.setBackgroundColor(ContextCompat.getColor(this, R.color.main_color1)) // 활성화된 버튼 이미지
            binding.btnComplete.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.btnComplete.isEnabled = false
            binding.btnComplete.setBackgroundColor(ContextCompat.getColor(this, R.color.gray1)) // 비활성화된 버튼 이미지
            binding.btnComplete.setTextColor(ContextCompat.getColor(this, R.color.gray4))
        }
    }

    private fun setupRecyclerView() {
        keywordAdapter = KeywordRVAdapter(keywordList)
        binding.keywordRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.keywordRv.adapter = keywordAdapter
    }

    private fun setupListeners() {
        // 키워드 입력 필드에서 글자 입력 시
        binding.editBoardKeyword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.addKeywordButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
        })

        // 키워드 추가 버튼 클릭 이벤트
        binding.addKeywordButton.setOnClickListener {
            val keyword = binding.editBoardKeyword.text.toString().trim()

            if (keyword.isNotEmpty() && !keywordList.contains(keyword)) {
                if (keywordList.size < 3) { // 🔥 3개까지만 추가 가능
                    keywordList.add("#$keyword")
                    keywordAdapter.notifyDataSetChanged() // RecyclerView 갱신
                    binding.editBoardKeyword.text.clear() // 입력 필드 초기화
                } else {
                    Toast.makeText(this, "키워드는 최대 3개까지 추가할 수 있습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createBoard() {
        val boardName = binding.editBoardName.text.toString()
        val content = binding.editBoardDescription.text.toString()

        val token = getToken()
        val createBoardService = RetrofitObj.getRetrofit().create(BoardApiService::class.java)
        createBoardService.createBoard("Bearer $token", BoardRequestDto(boardName, content, keywordList)).enqueue(object :
            Callback<BoardResponseDto> {
            override fun onResponse(call: Call<BoardResponseDto>, response: Response<BoardResponseDto>) {
                Log.d("CreateBoard/SUCCESS", response.toString())
                val resp: BoardResponseDto = response.body()!!

                if (resp != null) {
                    if (resp.isSuccess) {
                        Log.d("CreateBoard", "게시판 생성 성공")
                        finish()
                    }
                } else {
                    Log.e("CreateBoard/ERROR", "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BoardResponseDto>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }
        })
    }
}
