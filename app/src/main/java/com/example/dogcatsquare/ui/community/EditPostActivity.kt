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
import android.widget.ImageButton
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
import com.example.dogcatsquare.data.model.community.PostDetail
import com.example.dogcatsquare.data.model.community.UpdatePostRequest
import com.example.dogcatsquare.data.network.RetrofitObj
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
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

    /** 화면에 표시할 아이템: file이 있으면 새 이미지, url이 있으면 기존 서버 이미지 */
    data class ImageItem(val file: File? = null, val url: String? = null)
    private val imageItems = mutableListOf<ImageItem>()

    /** 서버에 이미 존재하는 이미지 URL 목록(유지용) */
    private val existingImageUrls = mutableListOf<String>()
    // 필요 시 삭제 목록을 모으려면:
    // private val removedImageUrls = mutableListOf<String>()

    private val PICK_IMAGE_REQUEST = 1
    private var postId: Long = -1L
    private var originalVideoUrl: String? = null
    private var postType: String = "post"
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
        imageAdapter = ImagePreviewAdapter(
            items = imageItems,
            onRemove = { position, item -> handleRemoveAt(position, item) }
        )
        rvImagePreview.adapter = imageAdapter

        ivBack.setOnClickListener { finish() }
        btnComplete.setOnClickListener { updatePost() }
        addPhoto.setOnClickListener { openGallery() }

        // ===== 인텐트에서 원본 데이터 세팅 =====
        postId = intent.getIntExtra("postId", -1).toLong()
        postType = intent.getStringExtra("postType") ?: "post"
        boardTypeFromIntent = intent.getStringExtra("boardType") ?: "자유게시판"

        etTitle.setText(intent.getStringExtra("title"))
        etContent.setText(intent.getStringExtra("content"))

        // 링크(비디오) 원본 값 보관 + 표시
        originalVideoUrl = intent.getStringExtra("videoUrl")
        etLink.setText(originalVideoUrl ?: "")
        if (originalVideoUrl.isNullOrBlank()) fetchPostForEdit()

        // 기존 이미지 URL 배열 받기 (PostDetail 화면에서 putStringArrayListExtra("images", ...))
        intent.getStringArrayListExtra("images")?.let { urls ->
            if (urls.isNotEmpty()) {
                existingImageUrls.addAll(urls)
                urls.forEach { url -> imageItems.add(ImageItem(url = url)) }
                imageAdapter.notifyDataSetChanged()
            }
        }

        // 혹시 단일 imageUrl만 넘겨오는 경우도 커버
        intent.getStringExtra("imageUrl")?.let { single ->
            if (single.isNotBlank() && existingImageUrls.isEmpty()) {
                existingImageUrls.add(single)
                imageItems.add(ImageItem(url = single))
                imageAdapter.notifyDataSetChanged()
            }
        }

        if (originalVideoUrl.isNullOrBlank()) {
            fetchPostForEdit()
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

    /** 상세 재조회로 링크/이미지 보강 */
    private fun fetchPostForEdit() {
        val token = getToken() ?: return
        if (postId <= 0) return

        val api = RetrofitObj.getRetrofit(this).create(BoardApiService::class.java)
        api.getPost("Bearer $token", postId.toInt())
            .enqueue(object : Callback<ApiResponse<PostDetail>> {
                override fun onResponse(
                    call: Call<ApiResponse<PostDetail>>,
                    resp: Response<ApiResponse<PostDetail>>
                ) {
                    if (!resp.isSuccessful) {
                        Log.e("EditPostActivity", "fetchPostForEdit http=${resp.code()}")
                        return
                    }
                    val post = resp.body()?.result ?: return

                    // 스키마가 video_URL이어도 @SerializedName으로 매핑된 videoUrl 사용
                    originalVideoUrl = post.videoUrl
                    etLink.setText(originalVideoUrl ?: "")

                    // 기존 이미지가 비어있으면 서버에서 받은 것으로 채움
                    if (existingImageUrls.isEmpty() && !post.images.isNullOrEmpty()) {
                        existingImageUrls.clear()
                        existingImageUrls.addAll(post.images!!)
                        imageItems.clear()
                        imageItems.addAll(existingImageUrls.map { ImageItem(url = it) })
                        imageAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<PostDetail>>, t: Throwable) {
                    Log.e("EditPostActivity", "fetchPostForEdit fail: ${t.message}")
                }
            })
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
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val out = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(out).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            fos.flush()
        }
        return out
    }

    private fun normalizeBoardType(raw: String?): String {
        val v = raw?.trim()?.replace(" ", "") ?: ""
        return when {
            v.contains("자유") -> "자유게시판"
            v.contains("정보") -> "정보공유게시판"
            v.contains("질문") || v.contains("상담") -> "질문상담게시판"
            v.contains("입양") || v.contains("임보") -> "입양임보게시판"
            v.contains("실종") || v.contains("목격") -> "실종목격게시판"
            else -> "자유게시판"
        }
    }

    // ===== 업데이트 로직 (링크 항상 포함) =====
    private fun updatePost() {
        val token = getToken() ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (postId <= 0) {
            Toast.makeText(this, "잘못된 게시글입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val inputLinkRaw = etLink.text.toString().trim() // 🔹 한 번만 읽기

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // ===== 링크 전송 규칙 (항상 포함) =====
        // - 빈칸이면 "" (삭제)
        // - 기존과 동일/변경 모두 실제 문자열을 그대로 전송 → 백엔드 Replace 정책 대응
        val linkToSend: String = if (inputLinkRaw.isBlank()) "" else inputLinkRaw

        val payload = UpdatePostRequest(
            boardType = normalizeBoardType(intent.getStringExtra("boardType")),
            title = title,
            content = content,
            videoUrlCamel = linkToSend,   // always include
            videoUrlSnake = linkToSend,   // always include
            images = if (existingImageUrls.isEmpty()) null else existingImageUrls,
            removeImageUrls = null        // remove 방식 쓸 경우 리스트 전달
        )

        val json = Gson().toJson(payload)
        val jsonBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        val api = RetrofitObj.getRetrofit(this).create(BoardApiService::class.java)

        // 새로 추가된 로컬 이미지 파일만 멀티파트로
        val imageParts = imageItems.filter { it.file != null }.map { item ->
            val body = item.file!!.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("communityImages", item.file!!.name, body)
        }

        Log.d(
            "EditPostActivity",
            "PUT json=$json, newImages=${imageParts.size}, originalLink=$originalVideoUrl, inputLink='$inputLinkRaw'"
        )

        api.updatePost(
            token = "Bearer $token",
            postId = postId,
            request = jsonBody,         // @Part("request") RequestBody
            communityImages = if (imageParts.isEmpty()) null else imageParts
        ).enqueue(object : Callback<ApiResponse<Unit>> {
            override fun onResponse(
                call: Call<ApiResponse<Unit>>,
                response: Response<ApiResponse<Unit>>
            ) {
                val body = response.body()
                if (response.isSuccessful && body?.isSuccess == true) {
                    Toast.makeText(this@EditPostActivity, "수정되었습니다.", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    val msg = body?.message ?: response.errorBody()?.string() ?: "수정 실패"
                    Log.e("EditPostActivity", "수정 실패: http=${response.code()} msg=$msg")
                    Toast.makeText(this@EditPostActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Unit>>, t: Throwable) {
                Log.e("EditPostActivity", "네트워크 오류: ${t.message}", t)
                Toast.makeText(this@EditPostActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** X 버튼 눌렀을 때: UI 리스트와 서버 유지리스트 동시 정리 */
    private fun handleRemoveAt(position: Int, item: ImageItem) {
        item.url?.let { url ->
            // 기존 서버 이미지 삭제요청 → existingImageUrls에서 제거
            val removed = existingImageUrls.remove(url)
            Log.d("EditPostActivity", "remove url=$url, removed=$removed")
            // remove 방식 사용할 경우:
            // removedImageUrls.add(url)
        }
        // 파일/URL 공통으로 미리보기에서 제거
        if (position in 0 until imageItems.size) {
            imageItems.removeAt(position)
            imageAdapter.notifyItemRemoved(position)
            imageAdapter.notifyItemRangeChanged(position, imageItems.size - position)
        }
    }

    // ===== 어댑터 (X 버튼 포함) =====
    class ImagePreviewAdapter(
        private val items: MutableList<ImageItem>,
        private val onRemove: (position: Int, item: ImageItem) -> Unit
    ) : RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.item_image_preview)
            val btnRemove: ImageButton = itemView.findViewById(R.id.btn_remove)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_edit_image_preview, parent, false) // ✅ X버튼 있는 레이아웃
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            if (item.file != null) {
                Glide.with(holder.imageView.context).load(item.file).centerCrop().into(holder.imageView)
            } else if (!item.url.isNullOrEmpty()) {
                Glide.with(holder.imageView.context).load(item.url).centerCrop().into(holder.imageView)
            } else {
                holder.imageView.setImageDrawable(null)
            }
            holder.btnRemove.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onRemove(pos, items[pos])
                }
            }
        }

        override fun getItemCount(): Int = items.size
    }
}