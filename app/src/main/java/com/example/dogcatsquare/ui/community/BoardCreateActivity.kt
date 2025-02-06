package com.example.dogcatsquare.ui.board

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.R
import com.example.dogcatsquare.api.RetrofitClient
import com.example.dogcatsquare.data.community.BoardRequestDto
import com.example.dogcatsquare.data.community.BoardResponseDto
import com.example.dogcatsquare.databinding.ActivityBoardCreateBinding
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoardCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼 설정
        binding.ivBack.setOnClickListener {
            finish()
        }

        // TextWatcher를 활용하여 입력값 변경 시 버튼 활성화 체크
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCompleteButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.editBoardName.addTextChangedListener(textWatcher)
        binding.editBoardDescription.addTextChangedListener(textWatcher)
        binding.editBoardKeyword.addTextChangedListener(textWatcher)

        // '완료' 버튼 클릭 시
        binding.btnComplete.setOnClickListener {
            validateAndCreateBoard()
        }

        // 초기 버튼 상태 설정
        updateCompleteButtonState()
    }

    // 📌 입력값이 올바를 경우 완료 버튼 활성화
    private fun updateCompleteButtonState() {
        val boardName = binding.editBoardName.text.toString().trim()
        val content = binding.editBoardDescription.text.toString().trim()
        val keywords = binding.editBoardKeyword.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val isBoardNameValid = boardName.isNotEmpty() && boardName.length <= 8
        val isContentValid = content.isNotEmpty() && content.length <= 300
        val isKeywordValid = keywords.size <= 3

        val isFormValid = isBoardNameValid && isContentValid && isKeywordValid

        // 입력값이 올바르면 버튼 활성화
        binding.btnComplete.setImageResource(
            if (isFormValid) R.drawable.bt_activated_complete
            else R.drawable.bt_deactivated_complete
        )

        binding.btnComplete.isEnabled = isFormValid
    }

    private fun validateAndCreateBoard() {
        val boardName = binding.editBoardName.text.toString().trim()
        val content = binding.editBoardDescription.text.toString().trim()
        val keywords = binding.editBoardKeyword.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

        // 입력값 검증
        when {
            boardName.isEmpty() || boardName.length > 8 -> {
                Toast.makeText(this, "게시판 이름은 1~8자여야 합니다.", Toast.LENGTH_SHORT).show()
                return
            }
            content.isEmpty() || content.length > 300 -> {
                Toast.makeText(this, "게시판 설명은 1~300자여야 합니다.", Toast.LENGTH_SHORT).show()
                return
            }
            keywords.size > 3 -> {
                Toast.makeText(this, "키워드는 최대 3개까지 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 수정된 API 요청
        createBoard(boardName, content, keywords)
    }


    // SharedPreferences에서 JWT 토큰 가져오기
    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    private fun createBoard(boardName: String, content: String, keywords: List<String>) {
        val token = getToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this@BoardCreateActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("BoardRequest", "보내는 데이터: boardName=$boardName, content=$content, keywords=$keywords")

        RetrofitClient.instance.createBoard("Bearer $token", boardName, content, keywords)
            .enqueue(object : Callback<BoardResponseDto> {
                override fun onResponse(call: Call<BoardResponseDto>, response: Response<BoardResponseDto>) {
                    Log.d("Board/Response", "응답 코드: ${response.code()}")
                    Log.d("Board/Response", "응답 메시지: ${response.message()}")
                    Log.d("Board/Response", "응답 바디: ${response.errorBody()?.string()}")

                    if (response.isSuccessful) {
                        Toast.makeText(this@BoardCreateActivity, "게시판이 성공적으로 등록되었습니다!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@BoardCreateActivity, "등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BoardResponseDto>, t: Throwable) {
                    Log.e("BoardCreate", "네트워크 오류: ${t.message}")
                    Toast.makeText(this@BoardCreateActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
