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
import com.example.dogcatsquare.databinding.FragmentEditPetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.io.FileOutputStream

class EditPetFragment : Fragment() {
    lateinit var binding: FragmentEditPetBinding

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null // 선택된 이미지의 URI를 저장하기 위한 변수

    // 초기 반려동물 선택 상태
    var selectedAnimal: String? = "dog"

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

        binding.editPetDoneBtn.setOnClickListener {
            editPetDone()
        }

        // 강아지, 고양이 선택 버튼
        binding.editDogSelectBtn.setOnClickListener {
            selectedAnimal = "dog"

            // 버튼 스타일 업데이트
            binding.editDogSelectBtn.setStrokeColorResource(R.color.main_color1)
            binding.editDogSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))
            binding.editCatSelectBtn.setStrokeColorResource(R.color.gray4)
            binding.editCatSelectBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray4))


            Toast.makeText(requireContext(), "${selectedAnimal}", Toast.LENGTH_SHORT).show()
        }

        binding.editCatSelectBtn.setOnClickListener {
            selectedAnimal = "cat"

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
    // Uri를 실제 경로로 변환하는 함수
    private fun getRealPathFromURI(uri: Uri): String {
        var path = ""
        val cursor = requireContext()?.contentResolver?.query(uri, null, null, null, null)
        if (cursor != null){
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            path = cursor.getString(idx)
            cursor.close()
        }
        return path
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
            val month = monthPicker.value
            val day = dayPicker.value

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

    private fun editPetDone() {
        val petName = binding.editPetNameEt.text.toString()
        val petBreed = binding.editPetSpeciesEt.text.toString()
        val petBirth = binding.editBirthSelectBtn.text.toString()

//        if (petName.isNotBlank() && petBreed.isNotBlank() && petBirth.isNotBlank()) {
////            val newPet = Pet(name = petName, breed = petBreed, birthDate = petBirth)
//
//            // 데이터 반환
//            val targetFragment = targetFragment
//            if (targetFragment is EditInfoFragment) {
//                val bundle = Bundle()
//                bundle.putString("pet", "추가") // 데이터 추가
//                targetFragment.onActivityResult(
//                    EditInfoFragment.ADD_PET_REQUEST_CODE,
//                    Activity.RESULT_OK,
//                    Intent().apply { putExtras(bundle) }
//                )
//            }
//
//            requireActivity().supportFragmentManager.popBackStack() // 뒤로가기
//        } else {
//            Toast.makeText(requireContext(), "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
//        }

        // 결과 전달 (데이터는 전달하지 않음)
//        parentFragmentManager.setFragmentResult("addPetInfoResult", Bundle())

        // 이전 프래그먼트로 돌아가기
        parentFragmentManager.popBackStack()
    }
}