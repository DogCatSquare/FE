package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.R

class BoardCreateActivity : AppCompatActivity() {

    private lateinit var btnComplete: ImageView
    private lateinit var etBoardName: EditText
    private lateinit var etBoardDescription: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_create)

        btnComplete = findViewById(R.id.btn_complete)
        etBoardName = findViewById(R.id.edit_board_name)
        etBoardDescription = findViewById(R.id.edit_board_description)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isBoardNameNotEmpty = etBoardName.text.toString().isNotBlank()
                val isBoardDescriptionNotEmpty = etBoardDescription.text.toString().isNotBlank()

                if (isBoardNameNotEmpty && isBoardDescriptionNotEmpty) {
                    btnComplete.setImageResource(R.drawable.bt_activated_complete)
                } else {
                    btnComplete.setImageResource(R.drawable.bt_deactivated_complete)
                }
            }
        }

        etBoardName.addTextChangedListener(textWatcher)
        etBoardDescription.addTextChangedListener(textWatcher)
    }
}
