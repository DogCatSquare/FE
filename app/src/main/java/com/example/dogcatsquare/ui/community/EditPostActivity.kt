package com.example.dogcatsquare.ui.community

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.ApiResponse
import com.example.dogcatsquare.data.community.PostRequest
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.api.BoardApiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class EditPostActivity : AppCompatActivity() {

    private lateinit var btnComplete: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etLink: EditText
    private lateinit var ivBack: ImageView
    private lateinit var addPhoto: RelativeLayout
    private lateinit var imagePreview: ImageView

    private var selectedImageFile: File? = null
    private val PICK_IMAGE_REQUEST = 1
    private var postId: Long = -1L
    private var originalImageUrl: String? = null

    // 게시글 종류 ("post" 또는 "tip") – UI 구분용으로만 사용할 수 있음.
    private var postType: String = "post"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post) // activity_edit_post.xml 레이아웃 사용

        btnComplete = findViewById(R.id.btnComplete)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etLink = findViewById(R.id.edit_link)
        ivBack = findViewById(R.id.iv_back)
        addPhoto = findViewById(R.id.add_photo)
        imagePreview = findViewById(R.id.image_preview)

        ivBack.setOnClickListener { finish() }

        btnComplete.setOnClickListener {
            Log.d("EditPostActivity", "수정 버튼 클릭됨!")
            updatePost() // postType에 관계없이 updatePost API 호출
        }

        // Intent에서 데이터 가져오기
        postId = intent.getLongExtra("postId", -1L)
        postType = intent.getStringExtra("postType") ?: "post" // "tip"일 수도 있으나, API는 동일함.

        etTitle.setText(intent.getStringExtra("title"))
        etContent.setText(intent.getStringExtra("content"))
        etLink.setText(intent.getStringExtra("videoUrl")) // 꿀팁인 경우 빈 문자열을 보낼 수 있음

        originalImageUrl = intent.getStringExtra("imageUrl")
        if (!originalImageUrl.isNullOrEmpty()) {
            imagePreview.visibility = View.VISIBLE
            Glide.with(this).load(originalImageUrl).into(imagePreview)
        } else {
            imagePreview.visibility = View.GONE
        }

        addPhoto.setOnClickListener { openGallery() }

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
            Glide.with(this).load(selectedImageFile).into(imagePreview)
        }
    }

    private fun getCompressedImageFile(uri: Uri): File {
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        val compressedFile = File(this.cacheDir, "compressed_image.jpg")
        FileOutputStream(compressedFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
        }
        return compressedFile
    }

    private fun updatePost() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val videoUrl = etLink.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // PostRequest 객체를 생성합니다.
        // 꿀팁일 경우 videoUrl 등 필요하지 않은 필드는 빈 문자열로 보낼 수 있습니다.
        val postRequest = PostRequest(
            boardId = 1,
            title = title,
            content = content,
            video_URL = videoUrl.ifBlank { "" },
            created_at = "2025-01-30T15:46:13.718Z"
        )

        val jsonPostRequest = Gson().toJson(postRequest)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonPostRequest)

        val imageParts: List<MultipartBody.Part>? = selectedImageFile?.let { file ->
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            listOf(MultipartBody.Part.createFormData("communityImages", file.name, requestFile))
        }

        val call = RetrofitObj.getRetrofit().create(BoardApiService::class.java)
            .updatePost(postId, requestBody, imageParts)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val resultIntent = Intent().apply {
                        putExtra("UPDATED_POST_ID", postId)
                        putExtra("UPDATED_TITLE", title)
                        putExtra("UPDATED_CONTENT", content)
                        putExtra("UPDATED_VIDEO_URL", videoUrl)
                        putExtra("UPDATED_IMAGE_URL", selectedImageFile?.absolutePath ?: originalImageUrl)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    Log.e("EditPostActivity", "수정 실패: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@EditPostActivity, "수정 실패", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("EditPostActivity", "네트워크 오류: ${t.message}")
                Toast.makeText(this@EditPostActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
