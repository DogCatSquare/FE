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
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitObj
import com.example.dogcatsquare.api.BoardApiService
import com.example.dogcatsquare.data.community.PostRequest
import com.example.dogcatsquare.data.community.ApiResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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
    private lateinit var charCount: TextView
    private lateinit var addPhoto: RelativeLayout
//    private lateinit var imagePreview: ImageView  // 🔹 선택한 이미지 미리보기 추가

    private var selectedImageFile: File? = null
    private val PICK_IMAGE_REQUEST = 1

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    private fun getId(): Int? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getInt("userId", 0)
    }

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
//        imagePreview = findViewById(R.id.image_preview)  // 🔹 이미지 미리보기

        ivBack.setOnClickListener { finish() }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateCompleteButtonState()
            }
        }

        etTitle.addTextChangedListener(textWatcher)
        etContent.addTextChangedListener(textWatcher)

        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                charCount.text = "${etContent.text.length}/300"
            }
        })

        addPhoto.setOnClickListener { openGallery() }

        btnComplete.setOnClickListener { uploadPost() }
    }

    private fun updateCompleteButtonState() {
        val isTitleNotEmpty = etTitle.text.toString().isNotBlank()
        val isContentNotEmpty = etContent.text.toString().isNotBlank()

        btnComplete.setImageResource(
            if (isTitleNotEmpty && isContentNotEmpty) R.drawable.bt_activated_complete
            else R.drawable.bt_deactivated_complete
        )
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

            // 🔹 Uri → File 변환 후 저장
            selectedImageFile = getCompressedImageFile(imageUri)

            // 🔹 선택한 이미지 미리보기
//            Glide.with(this)
//                .load(selectedImageFile)
//                .apply(RequestOptions.circleCropTransform())
//                .signature(ObjectKey(System.currentTimeMillis().toString()))
//                .into(imagePreview)
        }
    }

    // 🔹 Uri를 File로 변환하는 압축 처리 함수
    private fun getCompressedImageFile(uri: Uri): File {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, true)

        val compressedFile = File(this.cacheDir, "compressed_image.jpg")
        FileOutputStream(compressedFile).use { outputStream ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
        }

        return compressedFile
    }

    private fun uploadPost() {
        val userId = getId()
        val token = getToken()

        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val videoUrl = etLink.text.toString().trim()

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

        val postRequest = PostRequest(
            boardId = 1,
            title = title,
            content = content,
            video_URL = if (videoUrl.isNotBlank()) videoUrl else "",
            created_at = "2025-01-30T15:46:13.718Z"
        )

        val json = Gson().toJson(postRequest)
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val imageParts: List<MultipartBody.Part> = selectedImageFile?.let { file ->
            val requestFile = file.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
            listOf(MultipartBody.Part.createFormData("communityImages", file.name, requestFile))
        } ?: emptyList()

        val call = RetrofitObj.getRetrofit().create(BoardApiService::class.java)
        if (token != null && userId != null) {
            call.createPost(token, userId, requestBody, imageParts).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CreatePostActivity, "게시글이 등록되었습니다!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_ERROR", "게시글 등록 실패: ${response.code()} - $errorBody")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("API_FAILURE", "네트워크 오류: ${t.message}")
                }
            })
        }
    }
}
