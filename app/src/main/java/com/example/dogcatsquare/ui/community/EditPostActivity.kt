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
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.model.community.ApiResponse
import com.example.dogcatsquare.data.model.community.PostResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
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

class EditPostActivity : AppCompatActivity() {

    private lateinit var btnComplete: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etLink: EditText
    private lateinit var ivBack: ImageView
    private lateinit var addPhoto: RelativeLayout
    private lateinit var rvImagePreview: RecyclerView
    private lateinit var imageAdapter: ImagePreviewAdapter

    /** 파일이 있으면 새로 선택한 이미지, url이 있으면 기존 서버 이미지 */
    data class ImageItem(val file: File? = null, val url: String? = null)
    private val imageItems = mutableListOf<ImageItem>()

    private val PICK_IMAGE_REQUEST = 1
    private var postId: Long = -1L
    private var originalImageUrl: String? = null
    private var postType: String = "post" // UI 용 (기존 로직 유지)

    // ★ 추가: 서버에 보낼 게시판 타입(한글 표기)
    private var boardTypeFromIntent: String = "자유게시판"

    private fun getToken(): String? =
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("token", null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        btnComplete = findViewById(R.id.btnComplete)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etLink = findViewById(R.id.edit_link)
        ivBack = findViewById(R.id.iv_back)
        addPhoto = findViewById(R.id.add_photo)

        rvImagePreview = findViewById(R.id.rv_image_preview)
        rvImagePreview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImagePreviewAdapter(imageItems)
        rvImagePreview.adapter = imageAdapter

        ivBack.setOnClickListener { finish() }
        btnComplete.setOnClickListener { updatePost() }
        addPhoto.setOnClickListener { openGallery() }

        // 전달값 세팅
        postId = intent.getIntExtra("postId", -1).toLong()
        postType = intent.getStringExtra("postType") ?: "post" // UI 용 (기존 유지)
        boardTypeFromIntent = intent.getStringExtra("boardType") ?: "자유게시판" // ★ 추가
        etTitle.setText(intent.getStringExtra("title"))
        etContent.setText(intent.getStringExtra("content"))
        etLink.setText(intent.getStringExtra("videoUrl"))
        originalImageUrl = intent.getStringExtra("imageUrl")
        if (!originalImageUrl.isNullOrBlank()) {
            imageItems.add(ImageItem(url = originalImageUrl))
            imageAdapter.notifyDataSetChanged()
        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateButtonState()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etTitle.addTextChangedListener(watcher)
        etContent.addTextChangedListener(watcher)
        updateButtonState()
    }

    private fun updateButtonState() {
        val enabled =
            etTitle.text.toString().trim().isNotEmpty() &&
                    etContent.text.toString().trim().isNotEmpty()
        btnComplete.isEnabled = enabled
        btnComplete.setImageResource(
            if (enabled) R.drawable.bt_activated_complete else R.drawable.bt_deactivated_complete
        )
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
            val maxCount = 5
            val currentCount = imageItems.size

            when {
                data?.clipData != null -> {
                    val count = data.clipData!!.itemCount
                    val allowed = maxCount - currentCount
                    if (allowed <= 0) {
                        Toast.makeText(this, "최대 5개까지 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    for (i in 0 until minOf(count, allowed)) {
                        val uri = data.clipData!!.getItemAt(i).uri
                        imageItems.add(ImageItem(file = getCompressedImageFile(uri)))
                    }
                }
                data?.data != null -> {
                    if (currentCount < maxCount) {
                        imageItems.add(ImageItem(file = getCompressedImageFile(data.data!!)))
                    } else {
                        Toast.makeText(this, "최대 5개까지 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun getCompressedImageFile(uri: Uri): File {
        // 간단히 사용 (Deprecated 경고는 무시)
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val out = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(out).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            fos.flush()
        }
        return out
    }

    // ★ 수정: 서버 요구 스키마에 맞게 boardType 포함
    private data class UpdatePostPayload(
        @SerializedName("boardType") val boardType: String,
        @SerializedName("title") val title: String,
        @SerializedName("content") val content: String,
        @SerializedName("videoUrl") val videoUrl: String? = null
    )

    private fun updatePost() {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (postId <= 0) {
            Toast.makeText(this, "잘못된 게시글입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val videoUrl = etLink.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1) JSON 파트
        val payload = UpdatePostPayload(
            boardType = boardTypeFromIntent, // ★ 추가
            title = title,
            content = content,
            videoUrl = videoUrl.ifBlank { null }
        )
        val requestJson: RequestBody =
            Gson().toJson(payload).toRequestBody("application/json; charset=utf-8".toMediaType())

        // 2) 이미지 파트 (없으면 빈 리스트)
        val imageParts: List<MultipartBody.Part> =
            imageItems.filter { it.file != null }.map { item ->
                val file = item.file!!
                val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("communityImages", file.name, body)
            }

        // 3) 호출
        val api = RetrofitObj.getRetrofit(this).create(BoardApiService::class.java)
        api.updatePost("Bearer $token", postId, requestJson, imageParts)
            .enqueue(object : Callback<ApiResponse<PostResponse>> {
                override fun onResponse(
                    call: Call<ApiResponse<PostResponse>>,
                    response: Response<ApiResponse<PostResponse>>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body?.isSuccess == true) {
                        val resultIntent = Intent().apply {
                            putExtra("UPDATED_POST_ID", postId)
                            putExtra("UPDATED_TITLE", title)
                            putExtra("UPDATED_CONTENT", content)
                            putExtra("UPDATED_VIDEO_URL", videoUrl)
                            putExtra(
                                "UPDATED_IMAGE_URL",
                                imageItems.firstOrNull { it.file != null }?.file?.absolutePath
                                    ?: originalImageUrl
                            )
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        // ★ 메시지 우선 표시하도록 개선
                        val msg = body?.message ?: run {
                            try {
                                response.errorBody()?.string()
                            } catch (_: Exception) {
                                null
                            }
                        } ?: "수정 실패"
                        Log.e("EditPostActivity", "수정 실패: http=${response.code()} msg=$msg")
                        Toast.makeText(this@EditPostActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<PostResponse>>, t: Throwable) {
                    Log.e("EditPostActivity", "네트워크 오류: ${t.message}", t)
                    Toast.makeText(this@EditPostActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    class ImagePreviewAdapter(private val items: List<ImageItem>) :
        RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.item_image_preview)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image_preview, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            if (item.file != null) {
                Glide.with(holder.imageView.context)
                    .load(item.file)
                    .centerCrop()
                    .into(holder.imageView)
            } else if (!item.url.isNullOrEmpty()) {
                Glide.with(holder.imageView.context)
                    .load(item.url)
                    .centerCrop()
                    .into(holder.imageView)
            } else {
                holder.imageView.setImageDrawable(null)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}