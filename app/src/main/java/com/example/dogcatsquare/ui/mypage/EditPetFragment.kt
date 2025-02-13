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
import com.example.dogcatsquare.data.api.PetRetrofitItf
import com.example.dogcatsquare.data.model.pet.DeletePetResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.model.pet.FetchPetRequest
import com.example.dogcatsquare.data.model.pet.FetchPetResponse
import com.example.dogcatsquare.databinding.FragmentEditPetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.io.File
import java.io.FileOutputStream

class EditPetFragment : Fragment() {
    lateinit var binding: FragmentEditPetBinding

    private var petId: Int = -1
    private var petName: String = ""
    private var dogCat: String = ""
    private var petBirth: String = ""
    private var petBreed: String = ""
    private var petImage: String = ""

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
        binding = FragmentEditPetBinding.inflate(inflater, container, false)

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

        arguments?.let {
            petId = it.getInt("petId", -1) // 기본값 -1 설정
            petName = it.getString("petName", null)
            dogCat = it.getString("dogCat", null)
            petBirth = it.getString("petBirth", null)
            petBreed = it.getString("petBreed", null)
            petImage = (it.getString("petImage") ?: R.drawable.ic_profile_default).toString()
        }

        // 초기 세팅
        binding.editPetNameEt.setText(petName)
        binding.editBirthSelectBtn.text = petBirth
        binding.editPetSpeciesEt.setText(petBreed)
        if (dogCat == "DOG") {
            selectedAnimal = "DOG"
            binding.editDogSelectBtn.performClick()
        } else {
            selectedAnimal = "CAT"
            binding.editCatSelectBtn.performClick()
        }
        Glide.with(this)
            .load(petImage)
            .signature(ObjectKey(System.currentTimeMillis().toString())) // 캐시 무효화
            .placeholder(R.drawable.ic_profile_default)
            .into(binding.editPetIv)

        // 이미지 가져오기
        binding.editPetIv.setOnClickListener {
            openGallery()
        }

        // 생일 입력 버튼
        binding.editBirthSelectBtn.setOnClickListener {
            showBottomSheetDialog { selectedDate ->
                binding.editBirthSelectBtn.text = selectedDate
            }
        }

        // 강아지, 고양이 선택 버튼
        binding.editDogSelectBtn.setOnClickListener {
            selectedAnimal = "DOG"

            // 버튼 스타일 업데이트
            binding.editDogSelectBtn.setStrokeColorResource(R.color.main_color1)
            binding.editDogSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))
            binding.editCatSelectBtn.setStrokeColorResource(R.color.gray4)
            binding.editCatSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray4))


            Toast.makeText(requireContext(), "${selectedAnimal}", Toast.LENGTH_SHORT).show()
        }

        binding.editCatSelectBtn.setOnClickListener {
            selectedAnimal = "CAT"

            // 버튼 스타일 업데이트
            binding.editDogSelectBtn.setStrokeColorResource(R.color.gray4)
            binding.editDogSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray4))
            binding.editCatSelectBtn.setStrokeColorResource(R.color.main_color1)
            binding.editCatSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))

            Toast.makeText(requireContext(), "${selectedAnimal}", Toast.LENGTH_SHORT).show()
        }

        // 반려동물 이름 글자 수 감지
        val petNameET = binding.editPetNameEt
        val charCountTV = binding.charCountTv

        // EditText에 TextWatcher 추가
        petNameET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                charCountTV.text = "$currentLength/10"

//                petNameCheck = true
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 반려동물 종 텍스트 입력 감지
        val breedET = binding.editPetSpeciesEt

        breedET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 입력하기만 하면 true로
//                breedCheck = true
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.deletePetDoneBtn.setOnClickListener {
            deletePetDone()
        }

        binding.editPetDoneBtn.setOnClickListener {
            editPetDone()
        }

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
                .signature(ObjectKey(System.currentTimeMillis().toString())) // 캐시 무효화
                .into(binding.editPetIv)

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

            binding.editBirthSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            onBirthSelected(selectedDate)
//            petBirthCheck = true

            bottomSheetDialog.dismiss() // 바텀시트 닫기
        }

        // 바텀시트 다이얼로그 표시
        bottomSheetDialog.show()
    }

    private fun deletePetDone() {
        val token = getToken()

        val deletePetService = RetrofitObj.getRetrofit().create(PetRetrofitItf::class.java)
        deletePetService.deletePet("Bearer $token", petId).enqueue(object: Callback<DeletePetResponse> {
            override fun onResponse(call: Call<DeletePetResponse>, response: Response<DeletePetResponse>) {
                Log.d("RETROFIT/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    response.body()?.let { resp ->
                        if (resp.isSuccess) {
                            Log.d("DeletePet/SUCCESS", "Delete Pet")

                            Toast.makeText(context, "반려동물 정보가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        } else {
                            Log.e(
                                "DeletePet/FAILURE",
                                "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}"
                            )
                            Toast.makeText(context, "오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DeletePetResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }

    private fun editPetDone() {
        val token = getToken()

        val fetchPetRequest = FetchPetRequest(
            petName = binding.editPetNameEt.text.toString(),
            dogCat = selectedAnimal.toString(),
            breed =  binding.editPetSpeciesEt.text.toString(),
            birth = binding.editBirthSelectBtn.text.toString()
        )

        val gson = Gson()
        val requestJson = gson.toJson(fetchPetRequest)

        // JSON 문자열을 RequestBody로 변환
        val requestBody = requestJson.toRequestBody("application/json".toMediaTypeOrNull())

        val imagePart: MultipartBody.Part? = selectedImageUri?.let { uri ->
            val file = getFileFromUri(uri)
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("petImage", file.name, requestFile)
        }

        Log.d("JSON_REQUEST", requestJson)

        val editPetService = RetrofitObj.getRetrofit().create(PetRetrofitItf::class.java)
        editPetService.fetchPet("Bearer $token", petId, requestBody, imagePart).enqueue(object: Callback<FetchPetResponse> {
            override fun onResponse(call: Call<FetchPetResponse>, response: Response<FetchPetResponse>) {
                Log.d("RETROFIT/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    response.body()?.let { resp ->
                        if (resp.isSuccess) {
                            Log.d("FetchPet/SUCCESS", "Pet updated successfully")

                            Toast.makeText(context, "반려동물 정보 수정 완료", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        } else {
                            Log.e("FetchPet/FAILURE", "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}")
                            Toast.makeText(context, "반려동물 정보 수정 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("FetchPet/ERROR", "응답 코드: ${response.code()}, 에러 메시지: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<FetchPetResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }
}