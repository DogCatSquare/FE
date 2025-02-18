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
    // 기존의 imagePreview 대신 RecyclerView 사용
    private lateinit var rvImagePreview: RecyclerView
    private lateinit var imageAdapter: ImagePreviewAdapter

    // ImageItem: file가 있으면 새로운 선택, url이 있으면 기존 이미지
    data class ImageItem(val file: File? = null, val url: String? = null)

    // 새로 선택한 이미지와 기존 이미지를 함께 저장하는 리스트
    private val imageItems = mutableListOf<ImageItem>()
    private val PICK_IMAGE_REQUEST = 1
    private var postId: Long = -1L
    private var originalImageUrl: String? = null

    // 게시글 종류 ("post" 또는 "tip") – UI 구분용으로만 사용
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
        // RecyclerView 초기화 (미리보기용)
        rvImagePreview = findViewById(R.id.rv_image_preview)
        rvImagePreview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImagePreviewAdapter(imageItems)
        rvImagePreview.adapter = imageAdapter

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
        // 기존 이미지가 있다면 리스트에 추가하여 미리보기로 보여줌
        if (!originalImageUrl.isNullOrEmpty()) {
            imageItems.add(ImageItem(url = originalImageUrl))
            imageAdapter.notifyDataSetChanged()
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
        // 다중 선택 허용
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // 새로운 이미지 선택 시 기존 이미지(원본 URL)는 대체합니다.
            imageItems.clear()

            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri: Uri = data.clipData!!.getItemAt(i).uri
                    val file = getCompressedImageFile(imageUri)
                    imageItems.add(ImageItem(file = file))
                }
            } else if (data?.data != null) {
                val imageUri: Uri = data.data!!
                val file = getCompressedImageFile(imageUri)
                imageItems.add(ImageItem(file = file))
            }
            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun getCompressedImageFile(uri: Uri): File {
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        val compressedFile = File(this.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
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

        // PostRequest 객체 생성
        val postRequest = PostRequest(
            boardId = 1,
            title = title,
            content = content,
            video_URL = videoUrl.ifBlank { "" },
            created_at = "2025-01-30T15:46:13.718Z"
        )

        val jsonPostRequest = Gson().toJson(postRequest)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonPostRequest)

        // 새로 선택한 이미지(File)가 있다면 MultipartBody.Part로 변환합니다.
        // 만약 사용자가 이미지를 선택하지 않았다면 imageParts는 null로 두어 서버에서 기존 이미지를 유지하도록 합니다.
        val imageParts: List<MultipartBody.Part>? =
            if (imageItems.any { it.file != null }) {
                imageItems.filter { it.file != null }.map { item ->
                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), item.file!!)
                    MultipartBody.Part.createFormData("communityImages", item.file!!.name, requestFile)
                }
            } else {
                null
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
                        // 새 이미지가 있다면 첫 번째 파일 경로, 없으면 기존 URL 사용
                        putExtra("UPDATED_IMAGE_URL", imageItems.firstOrNull { it.file != null }?.file?.absolutePath ?: originalImageUrl)
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

    // 리사이클러뷰 어댑터: ImageItem을 받아서 파일 또는 URL을 로드하여 미리보기로 표시
    class ImagePreviewAdapter(private val items: List<ImageItem>) : RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>() {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.item_image_preview)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_preview, parent, false)
            return ViewHolder(view)
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
            }
        }
        override fun getItemCount(): Int = items.size
    }
}
