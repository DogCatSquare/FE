package com.example.dogcatsquare.ui.map.location

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.example.dogcatsquare.databinding.FragmentMapAddKeywordBinding
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.dogcatsquare.data.model.map.PlaceUserInfoRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.text.Editable
import android.text.TextWatcher
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitClient

class MapAddKeywordFragment : Fragment() {
    private var _binding: FragmentMapAddKeywordBinding? = null
    private val binding get() = _binding!!

    private var placeId: Int = 0
    private var placeName: String = ""
    private var defaultKeywords: Array<String> = emptyArray()
    private var category: String = ""
    private var currentKeywords: Array<String> = emptyArray()
    private var hasChanges = false
    private var additionalInfo: String = ""

    // 카테고리별 색상 정의
    private val categoryColors = mapOf(
        "HOSPITAL" to Pair("#EAF2FE", "#276CCB"),  // 배경색, 텍스트색
        "HOTEL" to Pair("#FEEEEA", "#F36037"),
        "RESTAURANT" to Pair("#FFFBF1", "#FF8D41"),
        "CAFE" to Pair("#FFFBF1", "#FF8D41"),
        "ETC" to Pair("#F6F6F6", "#9E9E9E")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            placeId = it.getInt("placeId")
            placeName = it.getString("placeName", "")
            defaultKeywords = it.getStringArray("defaultKeywords") ?: emptyArray()
            category = it.getString("category", "")
            currentKeywords = it.getStringArray("currentKeywords") ?: emptyArray()
            additionalInfo = it.getString("additionalInfo", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapAddKeywordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupBackButton()
        setupAddCharButton()
        setupDoneButton()
        setupDefaultKeywords()
    }

    private fun checkForChanges() {
        val currentSelectedKeywords = mutableSetOf<String>()
        val categoryBgColor = Color.parseColor(categoryColors[category]?.first)

        // 현재 선택된 키워드들 수집
        for (i in 0 until binding.flexboxLayout.childCount) {
            val view = binding.flexboxLayout.getChildAt(i)
            if (view is CardView && view.id != R.id.addChar) {
                val currentColor = view.backgroundTintList?.defaultColor
                val textView = view.getChildAt(0)

                val keyword = when (textView) {
                    is TextView -> textView.text.toString()
                    is EditText -> textView.text.toString()
                    else -> null
                }

                if ((view.tag as? Boolean == true) || (currentColor == categoryBgColor)) {
                    keyword?.let {
                        if (it.isNotBlank()) {
                            currentSelectedKeywords.add(it)
                        }
                    }
                }
            }
        }

        // 키워드 변경 여부 체크
        val keywordsChanged = currentSelectedKeywords != currentKeywords.toSet()

        // additionalInfo 변경 여부 체크 - 현재 텍스트와 초기값 비교
        val currentAdditionalInfo = binding.etKeyword.getText()
        val additionalInfoChanged = currentAdditionalInfo != additionalInfo

        // 둘 중 하나라도 변경되었으면 변경사항 있음으로 처리
        hasChanges = keywordsChanged || additionalInfoChanged

        // 완료 버튼 상태 업데이트
        updateDoneButtonState()
    }

    private fun updateDoneButtonState() {
        binding.doneButton.setImageResource(
            if (hasChanges) R.drawable.bt_activated_complete
            else R.drawable.bt_deactivated_complete
        )
        binding.doneButton.isEnabled = hasChanges
    }

    private fun setupUI() {
        binding.placeName.text = "${placeName}의"
        binding.etKeyword.setText(additionalInfo)

        // CustomEditText의 텍스트 변경 감지
        binding.etKeyword.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkForChanges()
            }
        })
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private fun setupAddCharButton() {
        binding.addChar.setOnClickListener {
            addEditableCharacteristic()
        }
    }

    private fun setupDoneButton() {
        updateDoneButtonState() // 초기 상태 설정

        binding.doneButton.setOnClickListener {
            if (!hasChanges) {
                return@setOnClickListener
            }

            val selectedKeywords = mutableSetOf<String>()
            val categoryBgColor = Color.parseColor(categoryColors[category]?.first)

            for (i in 0 until binding.flexboxLayout.childCount) {
                val view = binding.flexboxLayout.getChildAt(i)
                if (view is CardView && view.id != R.id.addChar) {
                    val currentColor = view.backgroundTintList?.defaultColor
                    val textView = view.getChildAt(0)

                    val keyword = when (textView) {
                        is TextView -> textView.text.toString()
                        is EditText -> textView.text.toString()
                        else -> null
                    }

                    if ((view.tag as? Boolean == true) || (currentColor == categoryBgColor)) {
                        keyword?.let {
                            if (it.isNotBlank()) {
                                selectedKeywords.add(it)
                            }
                        }
                    }
                }
            }

            val additionalInfo = binding.etKeyword.getText()

            lifecycleScope.launch {
                try {
                    val token = getToken()
                    if (token == null) {
                        Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.placesApiService.updatePlaceUserInfo(
                            token = "Bearer $token",
                            placeId = placeId,
                            request = PlaceUserInfoRequest(
                                keywords = selectedKeywords.toList(),
                                additionalInfo = additionalInfo
                            )
                        )
                    }

                    if (response.isSuccess) {
                        Toast.makeText(requireContext(), "정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()

                        val detailFragment = parentFragmentManager.fragments
                            .find { it is MapDetailFragment } as? MapDetailFragment
                        detailFragment?.refreshPlaceDetails()

                        val mapFragment = requireActivity().supportFragmentManager.fragments
                            .find { it is MapFragment } as? MapFragment
//                        mapFragment?.shouldRefresh = true

                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            response.message ?: "업데이트 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "네트워크 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupDefaultKeywords() {
        binding.flexboxLayout.removeView(binding.addChar)

        val (defaultBgColor, defaultTextColor) = categoryColors[category] ?:
        categoryColors["ETC"]!!

        val allKeywords = mutableSetOf<String>()
        allKeywords.addAll(defaultKeywords)
        allKeywords.addAll(currentKeywords)

        allKeywords.forEach { keyword ->
            val cardView = CardView(requireContext()).apply {
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    (35 * resources.displayMetrics.density).toInt()
                ).apply {
                    setMargins(
                        0,
                        0,
                        resources.getDimensionPixelSize(R.dimen.spacing_14),
                        resources.getDimensionPixelSize(R.dimen.spacing_8)
                    )
                }
                val isSelected = currentKeywords.contains(keyword)
                backgroundTintList = ColorStateList.valueOf(
                    if (isSelected) Color.parseColor(defaultBgColor)
                    else Color.parseColor("#F6F6F6")
                )
                radius = resources.getDimension(R.dimen.radius_4)
                cardElevation = 0f

                val textView = TextView(context).apply {
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    text = keyword
                    setTextColor(
                        if (isSelected) Color.parseColor(defaultTextColor)
                        else resources.getColor(R.color.gray4, null)
                    )
                    textSize = 14f
                    gravity = android.view.Gravity.CENTER
                    setPadding(
                        resources.getDimensionPixelSize(R.dimen.spacing_14),
                        0,
                        resources.getDimensionPixelSize(R.dimen.spacing_14),
                        0
                    )
                }
                addView(textView)

                if (keyword in defaultKeywords) {
                    setOnClickListener {
                        val isCurrentlySelected = tag as? Boolean ?: false
                        if (!isCurrentlySelected) {
                            backgroundTintList = ColorStateList.valueOf(Color.parseColor(defaultBgColor))
                            textView.setTextColor(Color.parseColor(defaultTextColor))
                        } else {
                            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F6F6F6"))
                            textView.setTextColor(resources.getColor(R.color.gray4, null))
                        }
                        tag = !isCurrentlySelected
                        checkForChanges()
                    }
                } else {
                    setOnClickListener {
                        binding.flexboxLayout.removeView(this)
                        checkForChanges()
                    }
                }
                tag = isSelected
            }
            binding.flexboxLayout.addView(cardView)
        }
        binding.flexboxLayout.addView(binding.addChar)
    }

    private fun addEditableCharacteristic() {
        val (defaultBgColor, defaultTextColor) = categoryColors[category] ?: categoryColors["ETC"]!!

        val editText = EditText(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                setPadding(0, 0, 0, 0)
            }
            minWidth = resources.getDimensionPixelSize(R.dimen.spacing_14) * 4
            setTextColor(Color.parseColor(defaultTextColor))
            textSize = 14f
            background = null
            hint = "특성 입력"
            setHintTextColor(Color.parseColor(defaultTextColor))
            gravity = android.view.Gravity.CENTER

            setPadding(
                resources.getDimensionPixelSize(R.dimen.spacing_14),
                0,
                resources.getDimensionPixelSize(R.dimen.spacing_14),
                0
            )
        }

        val newCardView = CardView(requireContext()).apply {
            layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                (35 * resources.displayMetrics.density).toInt()
            ).apply {
                setMargins(
                    0,
                    0,
                    resources.getDimensionPixelSize(R.dimen.spacing_14),
                    resources.getDimensionPixelSize(R.dimen.spacing_8)
                )
            }
            backgroundTintList = ColorStateList.valueOf(Color.parseColor(defaultBgColor))
            radius = resources.getDimension(R.dimen.radius_4)
            cardElevation = 0f

            val container = FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addView(editText)
            }
            addView(container)
        }

        val flexboxLayout = binding.flexboxLayout
        val addCharPosition = flexboxLayout.indexOfChild(binding.addChar)
        flexboxLayout.addView(newCardView, addCharPosition)

        editText.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val text = editText.text.toString()
                if (text.isBlank()) {
                    flexboxLayout.removeView(newCardView)
                } else {
                    val textView = TextView(context).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        this.text = text
                        setTextColor(Color.parseColor(defaultTextColor))
                        textSize = 14f
                        gravity = android.view.Gravity.CENTER
                        setPadding(
                            resources.getDimensionPixelSize(R.dimen.spacing_14),
                            0,
                            resources.getDimensionPixelSize(R.dimen.spacing_14),
                            0
                        )
                    }

                    newCardView.removeViewAt(0)
                    newCardView.addView(textView)

                    newCardView.setOnClickListener {
                        flexboxLayout.removeView(newCardView)
                        checkForChanges()
                    }
                    checkForChanges()
                }
            }
        }

        editText.setOnEditorActionListener { _, _, _ ->
            editText.clearFocus()
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            placeId: Int,
            placeName: String,
            defaultKeywords: Array<String>,
            category: String,
            currentKeywords: List<String>,
            additionalInfo: String
        ) = MapAddKeywordFragment().apply {
            arguments = Bundle().apply {
                putInt("placeId", placeId)
                putString("placeName", placeName)
                putStringArray("defaultKeywords", defaultKeywords)
                putString("category", category)
                putStringArray("currentKeywords", currentKeywords.toTypedArray())
                putString("additionalInfo", additionalInfo)
            }
        }
    }
}