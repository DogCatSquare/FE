package com.example.dogcatsquare.ui.community

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.R

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
