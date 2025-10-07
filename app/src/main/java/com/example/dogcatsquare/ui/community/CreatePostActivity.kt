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
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.model.community.ApiResponse
import com.example.dogcatsquare.data.model.community.PostCreateRequest
import com.example.dogcatsquare.data.model.community.PostResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class CreatePostActivity : AppCompatActivity() {

    private lateinit var btnComplete: Button
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etLink: EditText
    private lateinit var ivBack: ImageView
    private lateinit var addPhoto: RelativeLayout

    private lateinit var rvImagePreview: RecyclerView
    private lateinit var imageAdapter: ImagePreviewAdapter

    private val selectedImageFiles = mutableListOf<File>()
    private val PICK_IMAGE_REQUEST = 1

    private var boardId: Int = -1
    private var boardType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        boardId = intent.getIntExtra("BOARD_ID", -1)
        boardType = intent.getStringExtra("BOARD_TYPE") ?: boardTypeOf(boardId)

        btnComplete = findViewById(R.id.btnComplete)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etLink = findViewById(R.id.edit_link)
        ivBack = findViewById(R.id.iv_back)
        addPhoto = findViewById(R.id.add_photo)

        rvImagePreview = findViewById(R.id.rv_image_preview)
        rvImagePreview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImagePreviewAdapter(selectedImageFiles)
        rvImagePreview.adapter = imageAdapter

        ivBack.setOnClickListener { finish() }
        addPhoto.setOnClickListener { openGallery() }
        btnComplete.setOnClickListener { createPost() }

        etTitle.addTextChangedListener(textWatcher)
        etContent.addTextChangedListener(textWatcher)
        updateButtonState()
    }

    private fun boardTypeOf(boardId: Int): String? = when (boardId) {
        1 -> "자유게시판"
        2 -> "정보공유게시판"
        3 -> "질문상담게시판"
        4 -> "입양임보게시판"
        5 -> "실종목격게시판"
        else -> null
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = updateButtonState()
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (etContent.hasFocus()) {
                findViewById<TextView>(R.id.char_count).text = "${s?.length ?: 0}/20"
            }
        }
    }

    private fun updateButtonState() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val isEnabled = title.length >= 2 && content.isNotEmpty()

        btnComplete.isEnabled = isEnabled
        if (isEnabled) {
            btnComplete.setBackgroundColor(ContextCompat.getColor(this, R.color.main_color1))
            btnComplete.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            btnComplete.setBackgroundColor(ContextCompat.getColor(this, R.color.gray1))
            btnComplete.setTextColor(ContextCompat.getColor(this, R.color.gray4))
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageFiles.clear()
            val maxCount = 5

            when {
                data?.clipData != null -> {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until minOf(count, maxCount)) {
                        val file = getCompressedImageFile(data.clipData!!.getItemAt(i).uri)
                        selectedImageFiles.add(file)
                    }
                    if (count > maxCount) {
                        Toast.makeText(this, "최대 5개까지 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                data?.data != null -> {
                    if (selectedImageFiles.size < maxCount) {
                        val file = getCompressedImageFile(data.data!!)
                        selectedImageFiles.add(file)
                    } else {
                        Toast.makeText(this, "최대 5개까지 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun getCompressedImageFile(uri: Uri): File {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val compressedFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(compressedFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.flush()
        }
        return compressedFile
    }

    private fun getToken(): String? =
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("token", null)

    private fun getUserId(): Long =
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("userId", 0).toLong()

    private fun createPost() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (boardType.isNullOrBlank()) {
            Toast.makeText(this, "게시판을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val token = getToken()
        val userId = getUserId()
        if (token.isNullOrEmpty() || userId == 0L) {
            Toast.makeText(this, "인증 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val linkRaw: String? = etLink.text.toString().trim().ifBlank { null }

        val payload = PostCreateRequest(
            boardType = boardType!!,
            title = title,
            content = content,
            videoUrlCamel = linkRaw,   // "videoUrl"
            videoUrlSnake = linkRaw    // "video_URL"
        )

        val json = Gson().toJson(payload)
        val jsonBody: RequestBody =
            json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val imageParts = if (selectedImageFiles.isNotEmpty()) {
            selectedImageFiles.map { file ->
                val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("communityImages", file.name, body)
            }
        } else null

        Log.d("CreatePost", "json=$json")

        val svc = RetrofitObj.getRetrofit(this).create(BoardApiService::class.java)
        svc.createPost("Bearer $token", userId, jsonBody, imageParts)
            .enqueue(object : Callback<ApiResponse<PostResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<PostResponse>>,
                    response: Response<ApiResponse<PostResponse>>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body?.isSuccess == true) {
                        Toast.makeText(this@CreatePostActivity, "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(
                            this@CreatePostActivity,
                            "등록 실패: http=${response.code()} msg=${body?.message ?: response.errorBody()?.string()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<PostResponse>>, t: Throwable) {
                    Toast.makeText(this@CreatePostActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    class ImagePreviewAdapter(private val images: List<File>) :
        RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.item_image_preview)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image_preview, parent, false)
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