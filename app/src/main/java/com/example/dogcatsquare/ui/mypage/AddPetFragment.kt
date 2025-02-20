package com.example.dogcatsquare.ui.mypage

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
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.api.PetRetrofitItf
import com.example.dogcatsquare.data.model.pet.AddPetRequest
import com.example.dogcatsquare.data.model.pet.AddPetResponse
import com.example.dogcatsquare.databinding.FragmentAddPetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AddPetFragment : Fragment() {
    lateinit var binding: FragmentAddPetBinding

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null // 선택된 이미지의 URI를 저장하기 위한 변수

    // 초기 반려동물 선택 상태
    var selectedAnimal: String? = "DOG"

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPetBinding.inflate(inflater, container, false)

        // 상단바 색깔
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        // 배경화면 클릭 시 키보드 숨기기
        binding.addPetFragment.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN){
                requireActivity().currentFocus?.let { view ->
                    val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    view.clearFocus()
                }
            }
            false // 터치 이벤트를 소비하지 않음
        }

        // 이미지 가져오기
        binding.addPetIv.setOnClickListener {
            openGallery()
        }

        // 생일 입력 버튼
        binding.addBirthSelectBtn.setOnClickListener {
            showBottomSheetDialog { selectedDate ->
                binding.addBirthSelectBtn.text = selectedDate
            }
        }

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.editPetDoneBtn.setOnClickListener {
            addPetDataFromServer()
        }

        // 강아지, 고양이 선택 버튼
        binding.dogSelectBtn.setOnClickListener {
            selectedAnimal = "DOG"

            // 버튼 스타일 업데이트
            binding.dogSelectBtn.setStrokeColorResource(R.color.main_color1)
            binding.dogSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))
            binding.catSelectBtn.setStrokeColorResource(R.color.gray4)
            binding.catSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray4))


            Toast.makeText(requireContext(), "${selectedAnimal}", Toast.LENGTH_SHORT).show()
        }

        binding.catSelectBtn.setOnClickListener {
            selectedAnimal = "CAT"

            // 버튼 스타일 업데이트
            binding.dogSelectBtn.setStrokeColorResource(R.color.gray4)
            binding.dogSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray4))
            binding.catSelectBtn.setStrokeColorResource(R.color.main_color1)
            binding.catSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))

            Toast.makeText(requireContext(), "${selectedAnimal}", Toast.LENGTH_SHORT).show()
        }

        // 반려동물 이름 글자 수 감지
        val petNameET = binding.addPetNameEt
        val charCountTV = binding.charCountTv

        // EditText에 TextWatcher 추가
        petNameET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                charCountTV.text = "$currentLength/10"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 반려동물 종 텍스트 입력 감지
        val breedET = binding.addPetSpeciesEt

        breedET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
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
                .apply(RequestOptions.circleCropTransform())
                .signature(ObjectKey(System.currentTimeMillis().toString())) // 캐시 무효화
                .into(binding.addPetIv)

            selectedImageUri = compressedUri
        }
    }

    // 파일 가져오는 함수
    private fun getFileFromUri(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp_image", ".jpg", requireContext().cacheDir)
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        Log.d("EditProfileFragment", "Temp file path: ${tempFile.absolutePath}")
        return tempFile
    }

    // 이미지 압축 함수
    private fun getCompressedImageUri(uri: Uri): Uri {
        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)

        // 이미지 크기를 줄이기 위한 비율 설정 (50%로 설정)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, true)

        // 압축된 이미지 파일을 저장할 임시 파일 생성
        val compressedFile = File(requireContext().cacheDir, "compressed_image.jpg")
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
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        // 레이아웃 인플레이트
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_pick_date, null)
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
            Toast.makeText(requireContext(), "선택된 날짜: $selectedDate", Toast.LENGTH_SHORT).show()

            binding.addBirthSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            onBirthSelected(selectedDate)
//            petBirthCheck = true

            bottomSheetDialog.dismiss() // 바텀시트 닫기
        }

        // 바텀시트 다이얼로그 표시
        bottomSheetDialog.show()
    }

    private fun addPetDataFromServer() {
        val BEARER_TOKEN = getToken()

        val addPetRequest = AddPetRequest(
            petName = binding.addPetNameEt.text.toString(),
            dogCat = selectedAnimal.toString(),
            breed = binding.addPetSpeciesEt.text.toString(),
            birth = binding.addBirthSelectBtn.text.toString()
        )

        val gson = Gson()
        val requestJson = gson.toJson(addPetRequest)

        // JSON 문자열을 RequestBody로 변환
        val requestBody = requestJson.toRequestBody("application/json".toMediaTypeOrNull())

        val petImage: MultipartBody.Part? = selectedImageUri?.let { uri ->
            val file = getFileFromUri(uri)
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("petImage", file.name, requestFile)
        }

        val addPetService = RetrofitObj.getRetrofit().create(PetRetrofitItf::class.java)
        addPetService.addPet("Bearer $BEARER_TOKEN", requestBody, petImage).enqueue(object : Callback<AddPetResponse> {
            override fun onResponse(call: Call<AddPetResponse>, response: Response<AddPetResponse>) {
                Log.d("AddPet/Response", response.toString())

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    userResponse?.let { resp ->
                        if (resp.isSuccess) {
                            addPetDone()
                        } else {
                            Log.e("AddPet/ERROR", "반려동물 불러오기 실패: ${resp.message}")
                        }
                    }
                } else {
                    Log.e("AddPet/ERROR", "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AddPetResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }

    private fun addPetDone() {
        // 이전 프래그먼트로 돌아가기
        parentFragmentManager.popBackStack()
    }
}