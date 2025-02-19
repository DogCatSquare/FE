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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class CreatePostActivity() : AppCompatActivity() {

    private lateinit var btnComplete: Button
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etLink: EditText
    private lateinit var ivBack: ImageView
    private lateinit var addPhoto: RelativeLayout

    // RecyclerView로 미리보기할 예정 (XML에 RecyclerView 추가: id="rv_image_preview")
    private lateinit var rvImagePreview: RecyclerView
    private lateinit var imageAdapter: ImagePreviewAdapter

    // 단일 파일 대신 선택된 이미지 파일들을 저장
    private val selectedImageFiles = mutableListOf<File>()
    private val PICK_IMAGE_REQUEST = 1

    private var boardId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        boardId = intent.getIntExtra("BOARD_ID", -1)

        btnComplete = findViewById(R.id.btnComplete)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etLink = findViewById(R.id.edit_link)
        ivBack = findViewById(R.id.iv_back)
        addPhoto = findViewById(R.id.add_photo)
        // RecyclerView 초기화 (미리보기용)
        rvImagePreview = findViewById(R.id.rv_image_preview)
        rvImagePreview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImagePreviewAdapter(selectedImageFiles)
        rvImagePreview.adapter = imageAdapter

        ivBack.setOnClickListener { finish() }
        addPhoto.setOnClickListener { openGallery() }
        btnComplete.setOnClickListener { createPost() }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateButtonState() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                findViewById<TextView>(R.id.char_count).text = "$currentLength/20"
                updateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        updateButtonState()
    }

    private fun updateButtonState() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        val isTitleValid = title.length >= 2

        val isEnabled = isTitleValid && content.isNotEmpty()
        btnComplete.isEnabled = isEnabled

        if (isEnabled) {
            btnComplete.setBackgroundColor(ContextCompat.getColor(this, R.color.main_color1)) // 활성화된 버튼 이미지
            btnComplete.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            btnComplete.setBackgroundColor(ContextCompat.getColor(this, R.color.gray1)) // 활성화된 버튼 이미지
            btnComplete.setTextColor(ContextCompat.getColor(this, R.color.gray4))
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        // 다중 선택 허용
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // 기존에 선택한 이미지 초기화
            selectedImageFiles.clear()

            // 여러 이미지가 선택된 경우
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri: Uri = data.clipData!!.getItemAt(i).uri
                    val file = getCompressedImageFile(imageUri)
                    selectedImageFiles.add(file)
                }
            } else if (data?.data != null) {
                // 단일 이미지 선택한 경우
                val imageUri: Uri = data.data!!
                val file = getCompressedImageFile(imageUri)
                selectedImageFiles.add(file)
            }
            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun getCompressedImageFile(uri: Uri): File {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        val compressedFile = File(this.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
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
            boardId = boardId,
            title = title,
            content = content,
            video_URL = if (videoUrl.isBlank()) "" else videoUrl,
            created_at = "2025-01-30T15:46:13.718Z" // 필요 시 현재 시간으로 대체
        )

        val jsonPostRequest = Gson().toJson(postRequest)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonPostRequest)

        val imageParts: List<MultipartBody.Part>? = if (selectedImageFiles.isNotEmpty()) {
            selectedImageFiles.map { file ->
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                MultipartBody.Part.createFormData("images", file.name, requestFile)
            }
        } else {
            null
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

    // RecyclerView 어댑터 클래스: 각 항목에 선택된 이미지를 미리보기로 표시
    class ImagePreviewAdapter(private val images: List<File>) : RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>() {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.item_image_preview)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_preview, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.imageView.context)
                .load(images[position])
                .centerCrop()
                .into(holder.imageView)
        }
        override fun getItemCount(): Int = images.size
    }
}
