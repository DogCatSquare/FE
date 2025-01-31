package com.example.dogcatsquare.ui.map.location

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.dogcatsquare.R

class CustomEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val editText: EditText
    private val textCount: TextView
    private val cardView: CardView
    private var isEditEnabled = true
    private var maxLength = 50

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_edit_text, this, true)
        editText = view.findViewById(R.id.editText)
        textCount = view.findViewById(R.id.textCount)
        cardView = view.findViewById(R.id.cardView)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomEditText,
            0, 0
        ).apply {
            try {
                val hint = getString(R.styleable.CustomEditText_hint)
                val textSize = getDimension(R.styleable.CustomEditText_textSize, 16f)
                val hintColor = getColor(R.styleable.CustomEditText_hintColor, Color.parseColor("#9E9E9E"))
                maxLength = getInteger(R.styleable.CustomEditText_maxLength, 50)
                val editTextHeight = getDimension(R.styleable.CustomEditText_editTextHeight, -1f)

                editText.hint = hint
                editText.textSize = textSize / resources.displayMetrics.scaledDensity
                editText.setHintTextColor(hintColor)
                textCount.text = "0/$maxLength"


                // 높이가 설정되어 있으면 적용
                if (editTextHeight > 0) {
                    cardView.layoutParams.height = editTextHeight.toInt()
                }
            } finally {
                recycle()
            }
        }

        setupEditText()

        isEnabled = true
        editText.isEnabled = true
        alpha = 1.0f
    }

    private fun setupEditText() {
        editText.filters = arrayOf(InputFilter.LengthFilter(maxLength))

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditEnabled) {
                    val length = s?.length ?: 0
                    textCount.text = "$length/$maxLength"
                }
            }
        })
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        isEditEnabled = enabled
        editText.isEnabled = enabled
    }

    // EditText의 텍스트를 가져오는 메서드
    fun getText(): String = editText.text.toString()

    // EditText의 텍스트를 설정하는 메서드
    fun setText(text: String) {
        editText.setText(text)
    }

    fun addTextWatcher(watcher: TextWatcher) {
        editText.addTextChangedListener(watcher)
    }
}