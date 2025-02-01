package com.example.dogcatsquare.ui.mypage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitObj
import com.example.dogcatsquare.data.api.PetRetrofitItf
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.login.CheckNicknameResponse
import com.example.dogcatsquare.data.login.Pet
import com.example.dogcatsquare.data.login.SignUpResponse
import com.example.dogcatsquare.data.mypage.FetchUserRequest
import com.example.dogcatsquare.data.mypage.FetchUserResponse
import com.example.dogcatsquare.data.mypage.GetUserResponse
import com.example.dogcatsquare.data.pet.GetAllPetResponse
import com.example.dogcatsquare.databinding.FragmentEditInfoBinding
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
import java.util.Locale

class EditInfoFragment : Fragment() {
    lateinit var binding : FragmentEditInfoBinding

    companion object {
        const val ADD_PET_REQUEST_CODE = 1001
    }

    private var petDatas = ArrayList<Pet>()

    private var nickname_check: Boolean = false
    private var pw_check: Boolean = false
    private var phone_check: Boolean = false
    private var isEmailUpdate = false // 이메일 변경 여부를 추적

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    private fun getUserId(): Int {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("userId", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditInfoBinding.inflate(inflater, container, false)

        setupAddPetRecyclerView()

        // 배경 클릭 시 키보드 숨기기
        binding.editInfoFragment.setOnClickListener {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }

        getUser()

        binding.nicknameCheckBtn.setOnClickListener {
            val nickname = binding.myNicknameEt.text.toString()
            if (!isNicknameUsed(nickname)) {
                binding.signupNicknameCheckTv.text = "사용 가능한 닉네임입니다"
                binding.signupNicknameCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))
                nickname_check = true
            }
            else {
                binding.signupNicknameCheckTv.text = "이미 사용 중인 닉네임입니다"
                binding.signupNicknameCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }
        }

        binding.myProfileIv.setOnClickListener {
            openGallery()
        }

        // 반려동물 추가
        binding.addPetBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, AddPetFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        binding.myEmailEt.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, EditEmailFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // 이메일 인증 화면에서 결과 받기
        parentFragmentManager.setFragmentResultListener("emailResult", this) { _, bundle ->
            val updatedEmail = bundle.getString("email", "")
            if (!updatedEmail.isNullOrEmpty()) {
                binding.myEmailEt.setText(updatedEmail.lowercase(Locale.getDefault()))
                binding.myEmailEt.setAllCaps(false)
                binding.myEmailEt.filters = arrayOf()
                isEmailUpdate = true
            }
        }

        val imagePart: MultipartBody.Part? = selectedImageUri?.let { uri ->
            val file = getFileFromUri(uri)
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        }

        setupValidation()

        val fetchUserRequest = FetchUserRequest(
            nickname = binding.myNicknameEt.text.toString().takeIf { it.isNotBlank() },
            phoneNumber = binding.myPhoneEt.text.toString().takeIf { it.isNotBlank() },
            password = binding.pwCheckEt.text.toString().takeIf { it.isNotBlank() }
        )

        val gson = Gson()
        val requestJson = gson.toJson(fetchUserRequest)

        // JSON 문자열을 RequestBody로 변환
        val requestBody = requestJson.toRequestBody("application/json".toMediaTypeOrNull())

        Log.d("JSON_REQUEST", requestJson)

        binding.editDoneBtn.setOnClickListener {
            updateProfile(imagePart, requestBody)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        // 🔥 이메일 변경 결과 수신 후 UI 업데이트 (이중 체크)
        parentFragmentManager.setFragmentResultListener("emailResult", this) { _, bundle ->
            val updatedEmail = bundle.getString("email", "")
            if (!updatedEmail.isNullOrEmpty()) {
                binding.myEmailEt.text = updatedEmail  // ✅ 이메일 버튼 텍스트 변경
                isEmailUpdate = true // 이메일 변경 여부를 추적
            }
        }
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK) // ACTION_PICK 인텐트를 사용하여 갤러리에서 이미지를 선택하는 인텐트 생성
        intent.type = "image/*" // 인텐트의 타입을 "image/*"로 설정하여 모든 이미지 파일을 필터링
        startActivityForResult(intent, PICK_IMAGE_REQUEST) // 선택된 이미지의 결과를 받도록 인텐트를 시작
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // 갤러리에서 이미지 선택 후 호출되는 콜백 메서드
        super.onActivityResult(requestCode, resultCode, data) // 부모 클래스의 onActivityResult를 호출

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null){

            // 선택된 이미지의 URI를 가져옴
            val imageUri: Uri = data.data!!

            // 이미지뷰의 이전 이미지를 제거!!
            binding.myProfileIv.setImageDrawable(null)

            // 이미지 압축 및 리사이즈
            val compressedUri = getCompressedImageUri(imageUri)

            // 압축된 이미지를 ImagView에 설정
            binding.myProfileIv.setImageURI(compressedUri)

            Glide.with(this)
                .load(compressedUri)
                .signature(ObjectKey(System.currentTimeMillis().toString())) // 캐시 무효화
                .into(binding.myProfileIv)

            // ViewModel에 선택된 이미지 URI를 설정하여 저장
//            profileViewModel.setProfileImageUri(compressedUri)

            // selectedImageUri를 선택된 이미지의 URI로 업데이트
            selectedImageUri = compressedUri
        }
    }

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


    private fun setupAddPetRecyclerView() {
        petDatas.clear()

        val addPetRVAdapter = AddPetRVAdapter(petDatas)
        binding.petInfoRv.adapter = addPetRVAdapter
        binding.petInfoRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        getAllPetsFromServer(addPetRVAdapter)

//        parentFragmentManager.setFragmentResultListener("addPetInfoResult", this) { _, _ ->
//            petDatas.clear()
//            // 기본 아이템 추가
//            petDatas.apply {
//                add(Pet("이름", DogCat.DOG.toString(), "포메라니안", "2025-01-23"))
//            }
//            addPetRVAdapter.notifyDataSetChanged() // RecyclerView 업데이트
//        }

        addPetRVAdapter.setMyItemClickListener(object : AddPetRVAdapter.OnItemClickListener {
            override fun onItemClick(pet: Pet) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, EditPetFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        })
    }

    // 전체 반려동물 조회
    private fun getAllPetsFromServer(adapter: AddPetRVAdapter) {
        val BEARER_TOKEN = getToken()

        if (BEARER_TOKEN == null) {
            Log.e("GetPets/ERROR", "토큰이 없습니다.")
            return
        }

        val petService = RetrofitObj.getRetrofit().create(PetRetrofitItf::class.java)
        petService.getAllPet("Bearer $BEARER_TOKEN").enqueue(object : Callback<GetAllPetResponse> {
            override fun onResponse(call: Call<GetAllPetResponse>, response: Response<GetAllPetResponse>) {
                Log.d("GetPets/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val petResponse = response.body()
                    petResponse?.let { resp ->
                        if (resp.isSuccess) {
//                            val petList = resp.result
//                            petDatas.clear()
//                            petDatas.addAll(petList)
                            adapter.notifyDataSetChanged()
                            Log.d("GetPets/SUCCESS", "반려동물 정보 업데이트 완료")
                        } else {
                            Log.e("GetPets/ERROR", "반려동물 불러오기 실패: ${resp.message}")
                        }
                    }
                } else {
                    Log.e("GetPets/ERROR", "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<GetAllPetResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }
        })
    }


    // 닉네임 체크
    private fun validateNickname() {
        val nickname = binding.myNicknameEt.text.toString()
        val nicknameCheckTv = binding.signupNicknameCheckTv

        // 정규식: 한글, 영어 숫자 구성, 최대 10자
        val nicknameRegex = "^[a-zA-Zㄱ-힣0-9]{1,10}$".toRegex()

        if (!nickname.matches(nicknameRegex)) {
            nicknameCheckTv.text = "한글, 영어 최대 10자"
            nicknameCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            binding.nicknameCheckBtn.isClickable = false
            nickname_check = false
        } else {
            nicknameCheckTv.text = ""
            nicknameCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))
            binding.nicknameCheckBtn.isClickable = true
        }
    }

    // 닉네임 중복 체크
    private fun isNicknameUsed(nickname: String): Boolean {
        var checkNickname: Boolean = false
        val checkNicknameService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        checkNicknameService.checkNickname(nickname).enqueue(object : Callback<CheckNicknameResponse>{
            override fun onResponse(
                call: Call<CheckNicknameResponse>,
                response: Response<CheckNicknameResponse>
            ) {
                Log.d("CheckNickname/SUCCESS", response.toString())

                when(response.code()) {
                    200 -> {
                        val resp: CheckNicknameResponse = response.body()!!
                        if (resp != null) {
                            if (resp.isSuccess) {
                                if (resp.result == false) { // 일치하는 닉네임 없음 -> 중복 x
                                    checkNickname = true
                                    Log.d("CheckNickname/SUCCESS", checkNickname.toString())
                                } else { // 일치하는 닉네임 있음 -> 중복 o
                                    checkNickname = false
                                    Log.d("CheckNickname/SUCCESS", checkNickname.toString())
                                }
                            } else {
                                Log.e("CheckNickname/FAILURE", "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}")
                            }
                        } else {
                            Log.d("CheckNickname/FAILURE", "Response body is null")
                            Log.e("CheckNickname/FAILURE", "응답 코드: ${resp.code}, 응답메시지: ${resp.message}")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<CheckNicknameResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })

        return checkNickname
    }

    // 전화번호 체크
    private fun validatephone() {
        val phone = binding.myPhoneEt.text.toString()
        val phoneRegex ="^01[0-9]\\d{8}\$".toRegex()

        if (phone.matches(phoneRegex)) {
            phone_check = true
        }
    }

    // 비밀번호 체크
    private fun validatePassword() {
        val password = binding.myPwEt.text.toString()
        val passwordCheck = binding.pwCheckEt.text.toString()
        val passwordCheckTv = binding.signupPwCheckTv

        // 정규식: 소문자, 숫자 포함 8~15자
        val passwordRegex = "^(?=.*[a-z])(?=.*\\d)[a-zA-Z\\d]{8,15}$".toRegex()

        if (!password.matches(passwordRegex)) {
            passwordCheckTv.text = "소문자, 숫자 필수 포함 8~15자"
            passwordCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        } else if (password != passwordCheck) { // 비밀번호 확인 불일치
            passwordCheckTv.text = "비밀번호가 불일치합니다"
            passwordCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        } else {
            passwordCheckTv.text = "비밀번호가 일치합니다"
            passwordCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))
            pw_check = true
        }
    }

    // onResume 메서드는 프래그먼트가 사용자와 상호작용을 재개할 때 호출 됨. 즉, 마이페이지 조회 시 최신 정보를 불러옴
//    override fun onResume() {
//        super.onResume()
//        val token = getToken()
//        val userId = getUserId()
//        if (userId != -1 && token != null) {
//            checkSignup() // 마이페이지 조회 API 연동 함수 호출
//        }
//    }

    private fun updateProfile(imagePart: MultipartBody.Part?, requestBody: RequestBody) {
        val BEARER_TOKEN = getToken()

        val editProfileService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        editProfileService.fetchUser("Bearer $BEARER_TOKEN", requestBody, imagePart).enqueue(object: Callback<FetchUserResponse> {
                override fun onResponse(call: Call<FetchUserResponse>, response: Response<FetchUserResponse>) {
                    Log.d("RETROFIT/SUCCESS", response.toString())
                    Log.e("Response code: ", "${response.code()}")
                    Log.e("Error body: ", "${response.errorBody()?.string()}")

                    when(response.code()) {
                        200 -> {
                            val resp: FetchUserResponse = response.body()!!
                            if (resp != null) {
                                if (resp.isSuccess) {
                                    if(resp.code == "COMMON200") {
                                        Log.d("FetchUser/SUCCESS", response.toString())
                                        Toast.makeText(context, "프로필 수정 완료", Toast.LENGTH_SHORT)
                                            .show()
                                        parentFragmentManager.popBackStack()
                                    } else {
                                        Log.e("FetchUser/FAILURE", "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}")
                                        Toast.makeText(context, "프로필 수정 실패", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Log.e("FetchUser/FAILURE", "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}")
                                    Toast.makeText(context, "프로필 수정 실패", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Log.d("FetchUser/FAILURE", "Response body is null")
                                Log.e("FetchUser/FAILURE", "응답 코드: ${resp.code}, 응답메시지: ${resp.message}")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<FetchUserResponse>, t: Throwable) {
                    Log.d("RETROFIT/FAILURE", t.message.toString())
                }
            })
    }

    private fun getUser() {
        val BEARER_TOKEN = getToken()

        val getUserService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        getUserService.getUser("Bearer $BEARER_TOKEN").enqueue(object: Callback<GetUserResponse> {
            override fun onResponse(
                call: Call<GetUserResponse>,
                response: Response<GetUserResponse>
            ) {
                Log.d("FetchUser/SUCCESS", response.toString())

                val resp: GetUserResponse? = response.body()
                if (resp != null){
                    if(resp.isSuccess){ // 응답 성공 시
                        binding.myNicknameEt.setText(resp.result.nickname)

                        if (!isEmailUpdate) {
                            binding.myEmailEt.setText(resp.result.email.lowercase(Locale.getDefault()))
                            binding.myEmailEt.setAllCaps(false)
                            binding.myEmailEt.filters = arrayOf()
                        }
                        binding.myPhoneEt.setText(resp.result.phoneNumber)

                        Glide.with(requireContext())
                            .load(resp.result.profileImageUrl)
                            .signature(ObjectKey(System.currentTimeMillis().toString())) // 캐시 무효화
                            .into(binding.myProfileIv)

                    } else {
                        Log.e("FetchUser/FAILURE", "응답 코드: ${resp.code}, 응답메시지: ${resp.message}")
                    }
                } else {
                    Log.d("FetchUser/FAILURE", "Response body is null")
                }
            }

            override fun onFailure(call: Call<GetUserResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }

    private fun setupValidation() {
        binding.myNicknameEt.addTextChangedListener {
            validateNickname()
        }

        binding.myPwEt.addTextChangedListener {
            validatePassword()
        }

        binding.pwCheckEt.addTextChangedListener {
            validatePassword()
        }

        binding.myPhoneEt.addTextChangedListener {
            validatephone()
        }
    }
}