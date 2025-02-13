package com.example.dogcatsquare.ui.community

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.community.PostRequest
import com.example.dogcatsquare.data.community.ApiResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class CreatePostActivity : AppCompatActivity() {

    private lateinit var btnComplete: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etLink: EditText
    private lateinit var ivBack: ImageView
    private lateinit var addPhoto: RelativeLayout
    private lateinit var imagePreview: ImageView

    private var selectedImageFile: File? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        btnComplete = findViewById(R.id.btnComplete)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etLink = findViewById(R.id.edit_link)
        ivBack = findViewById(R.id.iv_back)
        addPhoto = findViewById(R.id.add_photo)
        imagePreview = findViewById(R.id.image_preview)  // XML에 추가한 image_preview

        ivBack.setOnClickListener { finish() }

        addPhoto.setOnClickListener { openGallery() }

        btnComplete.setOnClickListener { createPost() }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateButtonState() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etTitle.addTextChangedListener(textWatcher)
        etContent.addTextChangedListener(textWatcher)

        updateButtonState()
    }

    private fun updateButtonState() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val isEnabled = title.isNotEmpty() && content.isNotEmpty()
        btnComplete.isEnabled = isEnabled

        if (isEnabled) {
            btnComplete.setImageResource(R.drawable.bt_activated_complete)
        } else {
            btnComplete.setImageResource(R.drawable.bt_deactivated_complete)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            val imageUri: Uri = data.data!!
            selectedImageFile = getCompressedImageFile(imageUri)

            imagePreview.visibility = View.VISIBLE
            Glide.with(this)
                .load(selectedImageFile)
                .into(imagePreview)
        }
    }

    private fun getCompressedImageFile(uri: Uri): File {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        val compressedFile = File(this.cacheDir, "compressed_image.jpg")
        FileOutputStream(compressedFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
        }
        return compressedFile
    }

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    private fun getUserId(): Long {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("userId", 0).toLong()
    }

    private fun createPost() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val videoUrl = etLink.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val token = getToken()
        val userId = getUserId()
        if (token.isNullOrEmpty() || userId == 0L) {
            Toast.makeText(this, "인증 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val postRequest = PostRequest(
            boardId = 1,
            title = title,
            content = content,
            video_URL = if (videoUrl.isBlank()) "" else videoUrl,
            created_at = "2025-01-30T15:46:13.718Z" // 필요 시 현재 시간으로 대체
        )

        val jsonPostRequest = Gson().toJson(postRequest)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonPostRequest)

        val imageParts: List<MultipartBody.Part>? = selectedImageFile?.let { file ->
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            listOf(MultipartBody.Part.createFormData("images", file.name, requestFile))
        }

        val call = RetrofitObj.getRetrofit().create(BoardApiService::class.java)
            .createPost(token, userId, requestBody, imageParts)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreatePostActivity, "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()  // 등록 후 화면 종료 → 메인 화면으로 전환
                } else {
                    Toast.makeText(this@CreatePostActivity, "게시글 등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@CreatePostActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
