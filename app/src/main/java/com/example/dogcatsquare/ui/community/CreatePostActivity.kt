package com.example.dogcatsquare.ui.community

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.R
import com.example.dogcatsquare.api.RetrofitClient
import com.example.dogcatsquare.data.community.PostRequest
import com.example.dogcatsquare.data.community.ApiResponse
import com.example.dogcatsquare.data.community.SharedPrefManager
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CreatePostActivity : AppCompatActivity() {

    private lateinit var btnComplete: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etLink: EditText
    private lateinit var ivBack: ImageView
    private lateinit var charCount: TextView
    private lateinit var addPhoto: RelativeLayout

    private var selectedImageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        btnComplete = findViewById(R.id.btnComplete)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etLink = findViewById(R.id.edit_link)
        ivBack = findViewById(R.id.iv_back)
        charCount = findViewById(R.id.char_count)
        addPhoto = findViewById(R.id.add_photo)

        // 뒤로가기 버튼 클릭
        ivBack.setOnClickListener {
            finish()
        }

        // 제목 & 내용 입력 감지
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateCompleteButtonState()
            }
        }

        etTitle.addTextChangedListener(textWatcher)
        etContent.addTextChangedListener(textWatcher)

        // 내용 글자 수 제한 표시
        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                charCount.text = "${etContent.text.length}/300"
            }
        })

        // 사진 추가 버튼 클릭 시 갤러리 열기
        addPhoto.setOnClickListener {
            openGallery()
        }

        // 완료 버튼 클릭
        btnComplete.setOnClickListener {
            uploadPost()
        }
    }

    // 완료 버튼 활성화/비활성화
    private fun updateCompleteButtonState() {
        val isTitleNotEmpty = etTitle.text.toString().isNotBlank()
        val isContentNotEmpty = etContent.text.toString().isNotBlank()

        btnComplete.setImageResource(
            if (isTitleNotEmpty && isContentNotEmpty) R.drawable.bt_activated_complete
            else R.drawable.bt_deactivated_complete
        )
    }

    // 갤러리 열어서 이미지 선택
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageFile = File(uri.path ?: "")
            Toast.makeText(this, "이미지 선택 완료", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    // 게시글 업로드 API 호출
    private fun uploadPost() {
        val userId = SharedPrefManager.getUserId(this).toLong()
        val token = SharedPrefManager.getJwtToken(this)

        if (userId <= 0 || token.isNullOrEmpty()) {
            Log.e("AUTH_ERROR", "로그인이 필요합니다. userId: $userId, token: $token")
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val videoUrl = etLink.text.toString().trim()

        // 제목, 내용, 링크가 비어있는지 체크
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.length < 2 || title.length > 15) {
            Toast.makeText(this, "제목은 2~15자여야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.length > 300) {
            Toast.makeText(this, "내용은 300자를 초과할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // PostRequest 객체 생성 (비어있는 값 체크 후 설정)
        val postRequest = PostRequest(
            boardId = 1,
            title = title,
            content = content,
            video_URL = if (videoUrl.isNotBlank()) videoUrl else "",
            created_at = "2025-01-30T15:46:13.718Z"
        )

        val json = Gson().toJson(postRequest)
        Log.d("API_REQUEST", "보낼 JSON: $json")
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        // 이미지가 선택되지 않은 경우 null이 아닌 빈 리스트를 보냄
        val imageParts: List<MultipartBody.Part> = selectedImageFile?.let {
            val requestFile = it.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
            listOf(MultipartBody.Part.createFormData("communityImages", it.name, requestFile))
        } ?: emptyList() // ✅ null 방지

        // Retrofit API 호출
        val call = RetrofitClient.instance.createPost(userId, "Bearer $token", requestBody, imageParts)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreatePostActivity, "게시글이 등록되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "게시글 등록 실패: ${response.code()} - $errorBody")
                    Toast.makeText(this@CreatePostActivity, "게시글 등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_FAILURE", "네트워크 오류: ${t.message}")
                Toast.makeText(this@CreatePostActivity, "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
