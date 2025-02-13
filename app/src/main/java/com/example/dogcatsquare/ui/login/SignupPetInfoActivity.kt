package com.example.dogcatsquare.ui.login

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
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ActivitySignupPetInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

class SignupPetInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupPetInfoBinding

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null // 선택된 이미지의 URI를 저장하기 위한 변수

    // 초기 반려동물 선택 상태
    var selectedAnimal: String? = "DOG"

    var petNameCheck: Boolean = false
    var selectDogCatCheck: Boolean = true // 기본적으로 선택되어 있어서
    var petBirthCheck: Boolean = false
    var breedCheck: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupPetInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // 배경화면 클릭 시 키보드 숨기기
        binding.signupPetInfoActivity.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN){
                currentFocus?.let { view ->
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    view.clearFocus()
                }
            }
            false // 터치 이벤트를 소비하지 않음
        }

        // 이미지 가져오기
        binding.petIv.setOnClickListener {
            openGallery()
        }

        // 생일 입력 버튼
        binding.birthSelectBtn.setOnClickListener {
            showBottomSheetDialog { selectedDate ->
                binding.birthSelectBtn.text = selectedDate
            }
        }

        // 강아지, 고양이 선택 버튼
        binding.dogSelectBtn.setOnClickListener {
            selectedAnimal = "DOG"

            // 버튼 스타일 업데이트
            binding.dogSelectBtn.setStrokeColorResource(R.color.main_color1)
            binding.dogSelectBtn.setTextColor(ContextCompat.getColor(this, R.color.main_color1))
            binding.catSelectBtn.setStrokeColorResource(R.color.gray4)
            binding.catSelectBtn.setTextColor(ContextCompat.getColor(this, R.color.gray4))


            Toast.makeText(this, "${selectedAnimal}", Toast.LENGTH_SHORT).show()
        }

        binding.catSelectBtn.setOnClickListener {
            selectedAnimal = "CAT"

            // 버튼 스타일 업데이트
            binding.dogSelectBtn.setStrokeColorResource(R.color.gray4)
            binding.dogSelectBtn.setTextColor(ContextCompat.getColor(this, R.color.gray4))
            binding.catSelectBtn.setStrokeColorResource(R.color.main_color1)
            binding.catSelectBtn.setTextColor(ContextCompat.getColor(this, R.color.main_color1))

//            Toast.makeText(this, "${selectedAnimal}", Toast.LENGTH_SHORT).show()
        }

        // 반려동물 이름 글자 수 감지
        val petNameET = binding.petNameEt
        val charCountTV = binding.charCountTv

        // EditText에 TextWatcher 추가
        petNameET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                charCountTV.text = "$currentLength/10"

                petNameCheck = true
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 반려동물 종 텍스트 입력 감지
        val breedET = binding.petSpeciesEt

        breedET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 입력하기만 하면 true로
                breedCheck = true
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 다음 버튼 누르면 넘어가기
        // 추후에 api 연결 시 데이터도 넘기기
        binding.signupPetNextBtn.setOnClickListener {
            checkPetInfo()
        }
    }

    // 갤러리 여는 함수
    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK) // ACTION_PICK 인텐트를 사용하여 갤러리에서 이미지를 선택하는 인텐트 생성
        intent.type = "image/*" // 인텐트의 타입을 "image/*"로 설정하여 모든 이미지 파일을 필터링
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // 갤러리 이미 함수
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // 갤러리에서 이미지 선택 후 호출되는 콜백 메서드
        super.onActivityResult(requestCode, resultCode, data) // 부모 클래스의 onActivityResult를 호출

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            // 선택된 이미지의 URI를 가져옴
            val imageUri: Uri = data.data!!

            // 이미지 압축 및 리사이즈
            val compressedUri = getCompressedImageUri(imageUri)

            // Glide를 사용하여 원본 이미지 첨부
            Glide.with(this)
                .load(compressedUri)
                .signature(ObjectKey(System.currentTimeMillis().toString())) // 캐시 무효화
                .placeholder(R.drawable.ic_profile_default)
                .into(binding.petIv)

            selectedImageUri = compressedUri
        }
    }

    // 이미지 압축 함수
    private fun getCompressedImageUri(uri: Uri): Uri {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

        // 이미지 크기를 줄이기 위한 비율 설정 (50%로 설정)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, true)

        // 압축된 이미지 파일을 저장할 임시 파일 생성
        val compressedFile = File(this.cacheDir, "compressed_image.jpg")
        val outputStream = FileOutputStream(compressedFile)

        // 압축 품질 설정 (85%로 설정)
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        outputStream.flush()
        outputStream.close()

        return Uri.fromFile(compressedFile)
    }

    // 날짜 선택 바텀시트
    private fun showBottomSheetDialog(onBirthSelected: (String) -> Unit) {
        // 바텀시트 다이얼로그 생성
        val bottomSheetDialog = BottomSheetDialog(this)

        // 레이아웃 인플레이트
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_pick_date, null)
        bottomSheetDialog.setContentView(view)

        // NumberPicker 및 버튼 초기화
        val yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.month_picker)
        val dayPicker = view.findViewById<NumberPicker>(R.id.day_picker)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)

        // NumberPicker 범위 설정
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2100
        yearPicker.value = 2023

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        val months = (1..12).map { "${it}월" }.toTypedArray()
        monthPicker.displayedValues = months

        dayPicker.minValue = 1
        dayPicker.maxValue = 31
        val days = (1..31).map { "${it}일" }.toTypedArray() // "1일", "2일" ...
        dayPicker.displayedValues = days

        // 완료 버튼 클릭 이벤트
        confirmButton.setOnClickListener {
            val year = yearPicker.value
            val month = String.format("%02d", monthPicker.value) // 01~09 변환
            val day = String.format("%02d", dayPicker.value)     // 01~09 변환

            val selectedDate = "${year}-${month}-${day}"
            Toast.makeText(this, "선택된 날짜: $selectedDate", Toast.LENGTH_SHORT).show()

            binding.birthSelectBtn.setTextColor(ContextCompat.getColor(this, R.color.black))
            onBirthSelected(selectedDate)
            petBirthCheck = true

            bottomSheetDialog.dismiss() // 바텀시트 닫기
        }

        // 바텀시트 다이얼로그 표시
        bottomSheetDialog.show()
    }

    // 반려동물 정보 확인하고 넘어가기 -> 추후 수정
    private fun checkPetInfo() {
        if (petNameCheck && petBirthCheck && selectDogCatCheck && breedCheck) {
            val bundle = intent.extras ?: Bundle() // 기존 Bundle 가져오기

//            selectedImageUri?.let { uri ->
//                bundle.putString("petImageUri", uri.toString()) // Uri를 문자열로 변환하여 저장
//            }

            Log.d("CHECK_PET_INFO", "selectedImageUri before intent: $selectedImageUri")

            bundle.apply {
                putString("imageUri", selectedImageUri?.toString())
                putString("petName", binding.petNameEt.text.toString())  // 반려동물 이름
                putString("dogCat", selectedAnimal)                      // 강아지/고양이 선택
                putString("birth", binding.birthSelectBtn.text.toString()) // 생일
                putString("breed", binding.petSpeciesEt.text.toString()) // 반려동물 종
            }

            val intent = Intent(this, SignupMyInfoActivity::class.java).apply {
                putExtras(bundle) // Bundle을 Intent에 추가
            }

            startActivity(intent)
        }
    }
}