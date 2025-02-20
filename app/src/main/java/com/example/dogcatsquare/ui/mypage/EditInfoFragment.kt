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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.api.PetRetrofitItf
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.model.login.CheckNicknameResponse
import com.example.dogcatsquare.data.model.pet.PetList
import com.example.dogcatsquare.data.model.mypage.FetchUserRequest
import com.example.dogcatsquare.data.model.mypage.FetchUserResponse
import com.example.dogcatsquare.data.model.mypage.GetUserResponse
import com.example.dogcatsquare.data.model.pet.GetAllPetResponse
import com.example.dogcatsquare.databinding.FragmentEditInfoBinding
import com.example.dogcatsquare.ui.home.AddDDayFragment
import com.example.dogcatsquare.ui.viewmodel.ProfileViewModel
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

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private var petDatas = ArrayList<PetList>()

    private var nickname_check: Boolean = false
    private var pw_check: Boolean = false
    private var phone_check: Boolean = false

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    var name: String = ""
    var phone: String = ""
    var profileImg: String = ""

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
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

        arguments?.let {
            name = it.getString("nickname", null) // 기본값 -1 설정
            phone = it.getString("phone", null)
            profileImg = (it.getString("profileImg") ?: R.drawable.ic_profile_default).toString()
        }

        // 초기 세팅
        binding.myNicknameEt.setText(name)
        binding.myPhoneEt.setText(phone)
        Glide.with(this)
            .load(profileImg)
            .apply(RequestOptions.circleCropTransform())
            .signature(ObjectKey(System.currentTimeMillis().toString())) // 캐시 무효화
            .placeholder(R.drawable.ic_profile_default)
            .into(binding.myProfileIv)

        // 닉네임 체크
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

        binding.addDayBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, AddDDayFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // 프로필 편집
        binding.myProfileIv.setOnClickListener {
            openGallery()
        }

        binding.petInfoRv.isNestedScrollingEnabled = false
        // 반려동물 추가
        binding.addPetBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, AddPetFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // 이메일 인증
        binding.myEmailEt.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction() // parentFragmentManager
                .replace(R.id.main_frm, EditEmailFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        setupValidation()

        // 저장버튼 클릭 시
        binding.editDoneBtn.setOnClickListener {
            val fetchUserRequest = FetchUserRequest(
                nickname = binding.myNicknameEt.text.toString().takeIf { it.isNotBlank() },
                phoneNumber = binding.myPhoneEt.text.toString().takeIf { it.isNotBlank() },
                password = binding.pwCheckEt.text.toString().takeIf { it.isNotBlank() }
            )

            val gson = Gson()
            val requestJson = gson.toJson(fetchUserRequest)

            // JSON 문자열을 RequestBody로 변환
            val requestBody = requestJson.toRequestBody("application/json".toMediaTypeOrNull())

            val imagePart: MultipartBody.Part? = selectedImageUri?.let { uri ->
                val file = getFileFromUri(uri)
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                MultipartBody.Part.createFormData("profileImage", file.name, requestFile)
            }

            Log.d("JSON_REQUEST", requestJson)

            updateProfile(imagePart, requestBody)
        }

        return binding.root
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
            profileViewModel.setProfileImageUri(compressedUri)

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
        Log.d("EditProfileFragment", "Temp file path: ${tempFile.absolutePath}, File size: ${tempFile.length()}")
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

        binding.petInfoRv.addItemDecoration(HorizontalSpacingItemDecoration(15))

        getAllPets(addPetRVAdapter)

        addPetRVAdapter.setMyItemClickListener(object : AddPetRVAdapter.OnItemClickListener {
            override fun onItemClick(pet: PetList) {
                val fragment = EditPetFragment().apply {
                    arguments = Bundle().apply {
                        putInt("petId", pet.id) // pet.id는 해당 반려동물의 고유 ID
                        putString("petName", pet.petName)
                        putString("dogCat", pet.dogCat)
                        putString("petBirth", pet.birth.replace(". ", "-"))
                        putString("petBreed", pet.breed)
                        putString("petImage", pet.petImageUrl)
                    }
                }

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        })
    }

    // 전체 반려동물 조회
    private fun getAllPets(adapter: AddPetRVAdapter) {
        val BEARER_TOKEN = getToken()

        val petService = RetrofitObj.getRetrofit().create(PetRetrofitItf::class.java)
        petService.getAllPet("Bearer $BEARER_TOKEN").enqueue(object : Callback<GetAllPetResponse> {
            override fun onResponse(call: Call<GetAllPetResponse>, response: Response<GetAllPetResponse>) {
                Log.d("GetPet/SUCCESS", response.toString())
                val resp: GetAllPetResponse = response.body()!!

                if (resp != null) {
                    if (resp.isSuccess) {
                        Log.d("GetPet", "방려동물 전체 조회 성공")

                        val pets = resp.result.map { pet ->
                            PetList (
                                id = pet.id,
                                petName = pet.petName,
                                dogCat = pet.dogCat,
                                breed = pet.breed,
                                birth = pet.birth,
                                petImageUrl = pet.petImageUrl
                            )
                        }.toList()

                        petDatas.addAll(pets)
                        adapter.notifyDataSetChanged()
                    }

                } else {
                    Log.e("GetPet/ERROR", "응답 코드: ${response.code()}")
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
        val phoneRegex ="^01[0-9]{8}$".toRegex()

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

    private fun updateProfile(imagePart: MultipartBody.Part?, requestBody: RequestBody) {
        val BEARER_TOKEN = getToken()

        val editProfileService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        editProfileService.fetchUser("Bearer $BEARER_TOKEN", requestBody, imagePart).enqueue(object: Callback<FetchUserResponse> {
                override fun onResponse(call: Call<FetchUserResponse>, response: Response<FetchUserResponse>) {
                    Log.d("RETROFIT/SUCCESS", response.toString())

                    if (response.isSuccessful) {
                        response.body()?.let { resp ->
                            if (resp.isSuccess) {
                                Log.d("FetchUser/SUCCESS", "Profile updated successfully")
                                Log.d("FetchUser/SUCCESS", "Updated profile image URL: ${resp.result.profileImageUrl}")

                                Toast.makeText(context, "프로필 수정 완료", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.popBackStack()
                            } else {
                                Log.e("FetchUser/FAILURE", "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}")
                                Toast.makeText(context, "프로필 수정 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e("FetchUser/ERROR", "응답 코드: ${response.code()}, 에러 메시지: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<FetchUserResponse>, t: Throwable) {
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