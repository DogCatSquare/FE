package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.R

class CreatePostActivity : AppCompatActivity() {

    private lateinit var btnComplete: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var ivBack: ImageView // 뒤로가기 버튼

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post) // Replace with your layout file name

        btnComplete = findViewById(R.id.btnComplete)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        ivBack = findViewById(R.id.iv_back) // 뒤로가기 버튼 초기화

        // 뒤로가기 버튼 클릭 이벤트
        ivBack.setOnClickListener {
            finish() // 현재 액티비티 종료
        }

        // TextWatcher to observe changes in the EditText fields
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Check if both fields have text
                val isTitleNotEmpty = etTitle.text.toString().isNotBlank()
                val isContentNotEmpty = etContent.text.toString().isNotBlank()

                if (isTitleNotEmpty && isContentNotEmpty) {
                    // Change to active button
                    btnComplete.setImageResource(R.drawable.bt_activated_complete)
                } else {
                    // Change to inactive button
                    btnComplete.setImageResource(R.drawable.bt_deactivated_complete)
                }
            }
        }

        // Attach TextWatcher to the EditText fields
        etTitle.addTextChangedListener(textWatcher)
        etContent.addTextChangedListener(textWatcher)
    }
}
