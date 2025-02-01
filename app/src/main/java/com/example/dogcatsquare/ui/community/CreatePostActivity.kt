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
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitObj
import com.example.dogcatsquare.api.BoardApiService
import com.example.dogcatsquare.data.community.PostRequest
import com.example.dogcatsquare.data.community.ApiResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreatePostActivity : AppCompatActivity() {

    private lateinit var btnComplete: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etLink: EditText
    private lateinit var ivBack: ImageView
    private lateinit var charCount: TextView
    private lateinit var addPhoto: RelativeLayout
    private lateinit var imagePreview: ImageView

    private var postId: Long? = null

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
        imagePreview = ImageView(this)

        ivBack.setOnClickListener { finish() }

        postId = intent.getLongExtra("postId", -1L).takeIf { it != -1L }
        if (postId != null) {
            loadPostData()
        }

        etTitle.addTextChangedListener(textWatcher)
        etContent.addTextChangedListener(textWatcher)
    }

    private fun loadPostData() {
        etTitle.setText(intent.getStringExtra("title"))
        etContent.setText(intent.getStringExtra("content"))
        etLink.setText(intent.getStringExtra("videoUrl"))
    }

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    private fun getId(): Int? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("userId", 0)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            updateCompleteButtonState()
        }
    }

    private fun updateCompleteButtonState() {
        val isTitleNotEmpty = etTitle.text.toString().isNotBlank()
        val isContentNotEmpty = etContent.text.toString().isNotBlank()

        btnComplete.setImageResource(
            if (isTitleNotEmpty && isContentNotEmpty) R.drawable.bt_activated_complete
            else R.drawable.bt_deactivated_complete
        )
    }
}
