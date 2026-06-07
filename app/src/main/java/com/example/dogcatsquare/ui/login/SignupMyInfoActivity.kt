package com.example.dogcatsquare.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.GridLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.DDayRetrofitItf
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.model.home.DDay
import com.example.dogcatsquare.data.model.home.GetAllDDayResponse
import com.example.dogcatsquare.data.model.home.GetAllDDayResult
import com.example.dogcatsquare.data.model.home.NotificationRequest
import com.example.dogcatsquare.data.model.home.NotificationResponse
import com.example.dogcatsquare.data.model.home.RegisterFcmRequest
import com.example.dogcatsquare.data.model.home.RegisterFcmResponse
import com.example.dogcatsquare.data.model.login.Pet
import com.example.dogcatsquare.data.model.login.RegionData
import com.example.dogcatsquare.data.model.login.SignUpRequest
import com.example.dogcatsquare.data.model.login.SignUpResponse
import com.example.dogcatsquare.databinding.ActivitySignupMyInfoBinding
import com.google.android.gms.common.config.GservicesValue.isInitialized
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SignupMyInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupMyInfoBinding

    private var locCheck: Boolean = false

    private lateinit var doName: String
    private lateinit var si: String
    private lateinit var gu: String
    private lateinit var foodDate: String
    private var foodDuring: Int = 1
    private lateinit var padDate: String
    private var padDuring: Int = 1
    private lateinit var hospitalDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupMyInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // 지역 설정
        binding.selectLocBtn.setOnClickListener {
            showRegionBottomSheet { selectedRegion ->
                binding.selectLocBtn.text = selectedRegion
            }
        }

        // 사료 구매 캘린더 설정
        binding.feedstuffBuyDateBtn.setOnClickListener { // 원하는 버튼 ID로 변경
            showCustomCalendarBottomSheet { selectedDate ->
                Toast.makeText(this, "선택한 날짜: $selectedDate", Toast.LENGTH_SHORT).show()
                binding.feedstuffBuyDateBtn.text = selectedDate
                foodDate = selectedDate
            }
        }

        // 구매 주기 설정
        setFeedWeek()

        // 패드 구매 캘린더 설정
        binding.padsBuyDateBtn.setOnClickListener { // 원하는 버튼 ID로 변경
            showCustomCalendarBottomSheet { selectedpadDate ->
                Toast.makeText(this, "선택한 날짜: $selectedpadDate", Toast.LENGTH_SHORT).show()
                binding.padsBuyDateBtn.text = selectedpadDate
                padDate = selectedpadDate
            }
        }

        // 구매 주기 설정
        setPadWeek()

        // 다음 병원 날짜 캘린더 설정
        binding.selectHospitalDateBtn.setOnClickListener { // 원하는 버튼 ID로 변경
            showSelectHospitalCalendarBottomSheet { selectedDate ->
                Toast.makeText(this, "선택한 날짜: $selectedDate", Toast.LENGTH_SHORT).show()
                binding.selectHospitalDateBtn.text = selectedDate
                hospitalDate = selectedDate
            }
        }

        // 완료 버튼 클릭 시 메인 페이지로 넘어가기
        binding.signupBtn.setOnClickListener {
            chechMyInfo()
        }
    }

    // 내 정보 확인 후 메인 화면으로 넘어가기
    private fun chechMyInfo() {
        if (!locCheck || !this::doName.isInitialized || !this::si.isInitialized || !this::gu.isInitialized) {
            Toast.makeText(this, "지역을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (si.contains("전체") || gu.contains("전체")) {
            Toast.makeText(this, "상세 지역을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!this::foodDate.isInitialized) {
            Toast.makeText(this, "마지막 사료 구매 날짜를 설정해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!this::padDate.isInitialized) {
            Toast.makeText(this, "마지막 패드 구매 날짜를 설정해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!this::hospitalDate.isInitialized) {
            Toast.makeText(this, "다음 병원 방문 날짜를 설정해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (locCheck) {
            val signupService = RetrofitObj.getRetrofit(this).create(UserRetrofitItf::class.java)

            val signUpRequest = SignUpRequest(
                email = intent.getStringExtra("email") ?: "",
                password = intent.getStringExtra("password") ?: "",
                nickname = intent.getStringExtra("nickname") ?: "",
                phoneNumber = intent.getStringExtra("phoneNumber") ?: "",
                doName = this.doName,
                si = this.si,
                gu = this.gu,
                foodDate = this.foodDate,
                foodDuring = this.foodDuring,
                padDate = this.padDate,
                padDuring = this.padDuring,
                hospitalDate = this.hospitalDate,
                adAgree = intent.getBooleanExtra("adAgree", true),
                pet = Pet(
                    petName = intent.getStringExtra("petName") ?: "",
                    dogCat = intent.getStringExtra("dogCat") ?: "DOG",
                    breed = intent.getStringExtra("breed") ?: "",
                    birth = intent.getStringExtra("birth") ?: ""
                )
            )

            val gson = Gson()
            val requestJson = gson.toJson(signUpRequest)

            // JSON 문자열을 RequestBody로 변환
            val requestBody = requestJson.toRequestBody("application/json".toMediaTypeOrNull())

            Log.d("JSON_REQUEST", requestJson)

            // 이미지 파일이 있을 경우 MultipartBody.Part로 변환
            val imageUriString = intent.getStringExtra("imageUri")
            val imageUri: Uri? = imageUriString?.let { Uri.parse(it) }

            Log.d("DEBUG LOG TEST", "Received imageUriString: ${imageUriString.toString()}")
            Log.d("DEBUG LOG TEST", "Converted imageUri: ${imageUri.toString()}")

            val profileImage: MultipartBody.Part? = imageUri?.let { uri ->
                val file = getFileFromUri(uri)
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                MultipartBody.Part.createFormData("profileImage", file.name, requestFile)
            }

            val petImage: MultipartBody.Part? = imageUri?.let { uri ->
                val file = getFileFromUri(uri)
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                MultipartBody.Part.createFormData("petImage", file.name, requestFile)
            }

            signupService.signup(requestBody, profileImage, petImage).enqueue(object : Callback<SignUpResponse>{
                override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                    Log.d("Signup/SUCCESS", response.toString())

                    val resp: SignUpResponse? = response.body()
                    if (resp != null) {
                        Log.d("PROFILE IMAGE URL", resp.result.profileImageUrl ?: "NULL") // S3 URL 확인
                    } else {
                        Log.d("SIGNUP ERROR", "Response body is null")
                    }

                    when(response.code()) {
                        200 -> {
                            val resp: SignUpResponse = response.body()!!
                            if (resp != null) {
                                if (resp.isSuccess) {
                                    val userId = resp!!.result.id
                                    val accessToken = resp.result.token

                                    // 1) 토큰/유저ID 저장 (FCM 서비스에서 이 키들로 읽음)
                                    getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
                                        .putString("token", accessToken)
                                        .putInt("userId", userId) // 서비스/프래그먼트 모두 이 키로 통일 권장
                                        .apply()

                                    // 2) FCM 토큰 서버 등록
                                    registerFcmIfReady(accessToken, userId)

                                    fetchAndCacheDefaultDdayIds(accessToken) {
                                        maybeScheduleDefaultAlarms(accessToken, userId)
                                        moveLoginActivity(accessToken, userId, resp)
                                    }
                                } else {
                                    Log.e("Signup/FAILURE", "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}")
                                }
                            } else {
                                Log.d("Signup/FAILURE", "Response body is null")
                                Log.e("Signup/FAILURE", "응답 코드: ${resp.code}, 응답메시지: ${resp.message}")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                    Log.d("RETROFIT/FAILURE", t.message.toString())
                }

            })
        }
    }

    // FCM 토큰 등록 (Call<RegisterFcmResponse> 가정)
    private fun registerFcmIfReady(accessToken: String, userId: Int) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { fcm ->
                val api = RetrofitObj.getRetrofit(this).create(UserRetrofitItf::class.java)
                api.fcmToken("Bearer $accessToken", RegisterFcmRequest(userId, fcm))
                    .enqueue(object : Callback<RegisterFcmResponse> {
                        override fun onResponse(
                            call: Call<RegisterFcmResponse>,
                            response: Response<RegisterFcmResponse>
                        ) {
                            Log.d("FCM/REGISTER", "http=${response.code()}, body=${response.body()?.message}")
                        }
                        override fun onFailure(call: Call<RegisterFcmResponse>, t: Throwable) {
                            Log.e("FCM/REGISTER", "fail=${t.message}")
                        }
                    })
            }
            .addOnFailureListener {
                Log.e("FCM/TOKEN", "get token fail: ${it.message}")
            }
    }

    // 기본 D-Day 알람 예약 (Call<NotificationResponse> 가정)
    private fun maybeScheduleDefaultAlarms(accessToken: String, userId: Int) {

        val sp = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val foodDdayId = sp.getInt("food_dday_id", -1).takeIf { it > 0 }
        val padDdayId  = sp.getInt("pad_dday_id",  -1).takeIf { it > 0 }

        if (foodDdayId == null && padDdayId == null) {
            Log.d("ALARM/DEFAULT", "no default dday ids; skip")
            return
        }

        val api = RetrofitObj.getRetrofit(this).create(DDayRetrofitItf::class.java)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val safeFood = if (this::foodDate.isInitialized) foodDate else today
        val safePad  = if (this::padDate.isInitialized)  padDate  else today

        foodDdayId?.let { id ->
            api.setAlarm("Bearer $accessToken", id, userId,
                NotificationRequest(startDate = safeFood, termWeeks = foodDuring, enabled = true)
            ).enqueue(object : Callback<NotificationResponse> {
                override fun onResponse(
                    call: Call<NotificationResponse>,
                    response: Response<NotificationResponse>
                ) {
                    Log.d("ALARM/FOOD", "http=${response.code()}, msg=${response.body()?.message}")
                }
                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                    Log.e("ALARM/FOOD", "fail=${t.message}")
                }
            })
        }

        padDdayId?.let { id ->
            api.setAlarm("Bearer $accessToken", id, userId,
                NotificationRequest(startDate = safePad, termWeeks = padDuring, enabled = true)
            ).enqueue(object : Callback<NotificationResponse> {
                override fun onResponse(
                    call: Call<NotificationResponse>,
                    response: Response<NotificationResponse>
                ) {
                    Log.d("ALARM/PAD", "http=${response.code()}, msg=${response.body()?.message}")
                }
                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                    Log.e("ALARM/PAD", "fail=${t.message}")
                }
            })
        }
    }

    private fun safeDateOrToday(ref: kotlin.reflect.KProperty0<String>): String {
        return if (this::class.members.any { it == ref } && isInitialized(ref)) {
            ref.get()
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }

    // lateinit 초기화 여부 체크
    private fun isInitialized(prop: kotlin.reflect.KProperty0<String>): Boolean {
        return try {
            prop.get()
            true
        } catch (_: UninitializedPropertyAccessException) {
            false
        }
    }

    // 파일 가져오는 함수
    private fun getFileFromUri(uri: Uri): File {
        val inputStream = this.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp_image", ".jpg", this.cacheDir)
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        Log.d("EditProfileFragment", "Temp file path: ${tempFile.absolutePath}")
        return tempFile
    }

    // 지역 선택 바텀 시트
    private fun showRegionBottomSheet(onRegionSelected: (String) -> Unit) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_pick_loc, null)
        bottomSheetDialog.setContentView(view)

        val firstColumn: ListView = view.findViewById(R.id.region1_column)
        val secondColumn: ListView = view.findViewById(R.id.region2_column)
        val thirdColumn: ListView = view.findViewById(R.id.region3_column)
        val confirmBtn: Button = view.findViewById(R.id.confirm_button)

        val regions = RegionData.regions.map { region ->
            region.copy(subRegions = region.subRegions
                .filter { !it.name.contains("전체") }
                .map { subRegion ->
                    subRegion.copy(districts = subRegion.districts.filter { !it.contains("전체") })
                }
            )
        }

        // 선택된 항목 변수 초기화
        var selectedFirst: Int = -1
        var selectedSecond: Int = -1
        var selectedThird: Int = -1

        // 어댑터 초기화
        val firstAdapter = CustomArrayAdapter(this, regions.map { it.name }, selectedFirst)
        val secondAdapter = CustomArrayAdapter(this, mutableListOf<String>(), selectedSecond)
        val thirdAdapter = CustomArrayAdapter(this, mutableListOf<String>(), selectedThird)

        // 첫 번째 컬럼 설정
        firstColumn.adapter = firstAdapter
        firstColumn.setOnItemClickListener { _, _, position, _ ->
            selectedFirst = position
            selectedSecond = -1 // 초기화
            selectedThird = -1 // 초기화

            val secondData = regions[position].subRegions.map { it.name }
            secondAdapter.updateData(secondData, selectedSecond)
            thirdAdapter.clear()

            firstAdapter.updateSelectedPosition(selectedFirst)
        }

        // 두 번째 컬럼 설정
        secondColumn.adapter = secondAdapter
        secondColumn.setOnItemClickListener { _, _, position, _ ->
            selectedSecond = position
            selectedThird = -1 // 초기화

            val thirdData = regions[selectedFirst].subRegions[position].districts
            thirdAdapter.updateData(thirdData, selectedThird)

            secondAdapter.updateSelectedPosition(selectedSecond)
        }

        // 세 번째 컬럼 설정
        thirdColumn.adapter = thirdAdapter
        thirdColumn.setOnItemClickListener { _, _, position, _ ->
            selectedThird = position
            thirdAdapter.updateSelectedPosition(selectedThird)
        }

        // 기본 데이터 초기화
        firstAdapter.updateData(regions.map { it.name }, selectedFirst)

        // 완료 버튼 클릭
        confirmBtn.setOnClickListener {
            if (selectedFirst == -1 || selectedSecond == -1 || selectedThird == -1) {
                Toast.makeText(this@SignupMyInfoActivity, "상세 지역을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tempDoName = regions[selectedFirst].name
            val tempSi = regions[selectedFirst].subRegions[selectedSecond].name
            val tempGu = regions[selectedFirst].subRegions[selectedSecond].districts[selectedThird]

            if (tempSi.contains("전체") || tempGu.contains("전체")) {
                Toast.makeText(this@SignupMyInfoActivity, "상세 지역을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            doName = tempDoName
            si = tempSi
            gu = tempGu
            locCheck = true

            val selectedRegion = "$doName $si $gu"
            onRegionSelected(selectedRegion)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    // 캘린더 뷰 바텀시트
    private fun showCustomCalendarBottomSheet(onDateSelected: (String) -> Unit) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_pick_buy_date, null)
        bottomSheetDialog.setContentView(view)

        val yearButton: MaterialButton = view.findViewById(R.id.year_button)
        val monthButton: MaterialButton = view.findViewById(R.id.month_button)
        val calendarGrid: GridLayout = view.findViewById(R.id.calendar_grid)
        val confirmButton: Button = view.findViewById(R.id.confirm_button)

        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance() // 오늘 날짜를 기준으로 설정
        var selectedYear = calendar.get(Calendar.YEAR)
        var selectedMonth = calendar.get(Calendar.MONTH) // 0부터 시작
        var selectedDay: Int? = null // 선택된 일자 저장 변수

        // 초기 연도와 월 설정
        yearButton.text = "$selectedYear"
        monthButton.text = "${selectedMonth + 1}월"

        // 캘린더 업데이트 함수
        fun updateCalendar(year: Int, month: Int) {
            calendarGrid.removeAllViews()
            val calendarInstance = Calendar.getInstance()
            calendarInstance.set(year, month, 1)

            // 첫 날의 요일 (일요일: 1, 월요일: 2, ...)
            val firstDayOfWeek = calendarInstance.get(Calendar.DAY_OF_WEEK)
            val daysInMonth = calendarInstance.getActualMaximum(Calendar.DAY_OF_MONTH)

            // 빈칸 추가 (첫 주의 시작 요일까지)
            for (i in 1 until firstDayOfWeek) {
                val emptyView = TextView(this)
                emptyView.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                calendarGrid.addView(emptyView)
            }

            // 날짜 추가
            for (day in 1..daysInMonth) {
                val dateView = TextView(this)
                dateView.text = day.toString()
                dateView.textSize = 16f
                dateView.gravity = android.view.Gravity.CENTER
                dateView.setPadding(8, 8, 8, 8)
                dateView.setTextColor(ContextCompat.getColor(this, R.color.gray3))

                dateView.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }

                // 선택 불가 조건 추가
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }

                if (selectedDate.after(today)) {
                    // 미래 날짜는 비활성화
                    dateView.isClickable = false // 클릭 불가
                } else {
                    dateView.setOnClickListener {
                        // 선택된 날짜 업데이트
                        selectedDay = day

                        if (selectedDate.after(today)) {
                            Toast.makeText(this@SignupMyInfoActivity, "구매했던 이전 날짜룰 선택해주세요.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // 이전 선택된 날짜 초기화
                        for (i in 0 until calendarGrid.childCount) {
                            val child = calendarGrid.getChildAt(i)
                            if (child is TextView) {
                                child.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                                child.setTextColor(ContextCompat.getColor(this, R.color.gray3))
                            }
                        }

                        dateView.setBackgroundColor(ContextCompat.getColor(this, R.color.main_color1))
                        dateView.setTextColor(ContextCompat.getColor(this, R.color.white))
                        Toast.makeText(this, "${year}년 ${month + 1}월 ${day}일 선택됨", Toast.LENGTH_SHORT).show()
                    }
                }

                calendarGrid.addView(dateView)
            }
        }

        // 초기 캘린더 로드
        updateCalendar(selectedYear, selectedMonth)

        // 연도 선택 버튼 클릭
        yearButton.setOnClickListener {
            val years = (2020..2030).toList().map { it.toString() }.toTypedArray()
            showDropdownDialog("연도 선택", years) { year ->
                selectedYear = year.toInt()
                yearButton.text = year
                updateCalendar(selectedYear, selectedMonth)
            }
        }

        // 월 선택 버튼 클릭
        monthButton.setOnClickListener {
            val months = (1..12).toList().map { "${it}월" }.toTypedArray()
            showDropdownDialog("월 선택", months) { month ->
                selectedMonth = month.removeSuffix("월").toInt() - 1
                monthButton.text = month
                updateCalendar(selectedYear, selectedMonth)
            }
        }

        // 완료 버튼 클릭 이벤트
        confirmButton.setOnClickListener {
            val formattedMonth = String.format("%02d", selectedMonth + 1) // 01~09 변환
            val formattedDay = selectedDay?.let { String.format("%02d", it) } ?: "01" // 01~09 변환 (선택된 날짜 없을 때 기본값 01)
            val selectedDate = "${selectedYear}-${formattedMonth}-${formattedDay}"
            onDateSelected(selectedDate)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showSelectHospitalCalendarBottomSheet(onDateSelected: (String) -> Unit) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_pick_buy_date, null)
        bottomSheetDialog.setContentView(view)

        val yearButton: MaterialButton = view.findViewById(R.id.year_button)
        val monthButton: MaterialButton = view.findViewById(R.id.month_button)
        val calendarGrid: GridLayout = view.findViewById(R.id.calendar_grid)
        val confirmButton: Button = view.findViewById(R.id.confirm_button)

        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance() // 오늘 날짜를 기준으로 설정
        var selectedYear = calendar.get(Calendar.YEAR)
        var selectedMonth = calendar.get(Calendar.MONTH) // 0부터 시작
        var selectedDay: Int? = null // 선택된 일자 저장 변수

        // 초기 연도와 월 설정
        yearButton.text = "$selectedYear"
        monthButton.text = "${selectedMonth + 1}월"

        // 캘린더 업데이트 함수
        fun updateCalendar(year: Int, month: Int) {
            calendarGrid.removeAllViews()
            val calendarInstance = Calendar.getInstance()
            calendarInstance.set(year, month, 1)

            // 첫 날의 요일 (일요일: 1, 월요일: 2, ...)
            val firstDayOfWeek = calendarInstance.get(Calendar.DAY_OF_WEEK)
            val daysInMonth = calendarInstance.getActualMaximum(Calendar.DAY_OF_MONTH)

            // 빈칸 추가 (첫 주의 시작 요일까지)
            for (i in 1 until firstDayOfWeek) {
                val emptyView = TextView(this)
                emptyView.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                calendarGrid.addView(emptyView)
            }

            // 날짜 추가
            for (day in 1..daysInMonth) {
                val dateView = TextView(this)
                dateView.text = day.toString()
                dateView.textSize = 16f
                dateView.gravity = android.view.Gravity.CENTER
                dateView.setPadding(8, 8, 8, 8)
                dateView.setTextColor(ContextCompat.getColor(this, R.color.gray3))

                dateView.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }

                // 선택 불가 조건 추가
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }

                if (selectedDate.before(today)) {
                    // 미래 날짜는 비활성화
                    dateView.isClickable = false // 클릭 불가
                } else {
                    dateView.setOnClickListener {
                        // 선택된 날짜 업데이트
                        selectedDay = day

                        if (selectedDate.before(today)) {
                            Toast.makeText(this@SignupMyInfoActivity, "다음 병원 방문일을 선택해주세요.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // 이전 선택된 날짜 초기화
                        for (i in 0 until calendarGrid.childCount) {
                            val child = calendarGrid.getChildAt(i)
                            if (child is TextView) {
                                child.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                                child.setTextColor(ContextCompat.getColor(this, R.color.gray3))
                            }
                        }

                        // 선택된 날짜 강조-
                        dateView.setBackgroundColor(ContextCompat.getColor(this, R.color.main_color1))
                        dateView.setTextColor(ContextCompat.getColor(this, R.color.white))
                        Toast.makeText(this, "${year}년 ${month + 1}월 ${day}일 선택됨", Toast.LENGTH_SHORT).show()
                    }
                }

                calendarGrid.addView(dateView)
            }
        }

        // 초기 캘린더 로드
        updateCalendar(selectedYear, selectedMonth)

        // 연도 선택 버튼 클릭
        yearButton.setOnClickListener {
            val years = (2020..2030).toList().map { it.toString() }.toTypedArray()
            showDropdownDialog("연도 선택", years) { year ->
                selectedYear = year.toInt()
                yearButton.text = year
                updateCalendar(selectedYear, selectedMonth)
            }
        }

        // 월 선택 버튼 클릭
        monthButton.setOnClickListener {
            val months = (1..12).toList().map { "${it}월" }.toTypedArray()
            showDropdownDialog("월 선택", months) { month ->
                selectedMonth = month.removeSuffix("월").toInt() - 1
                monthButton.text = month
                updateCalendar(selectedYear, selectedMonth)
            }
        }

        // 완료 버튼 클릭 이벤트
        confirmButton.setOnClickListener {
            val formattedMonth = String.format("%02d", selectedMonth + 1) // 01~09 변환
            val formattedDay = selectedDay?.let { String.format("%02d", it) } ?: "01" // 01~09 변환 (선택된 날짜 없을 때 기본값 01)
            val selectedDate = "${selectedYear}-${formattedMonth}-${formattedDay}"
            onDateSelected(selectedDate)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showDropdownDialog(title: String, items: Array<String>, onItemSelected: (String) -> Unit) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(items) { _, which ->
            onItemSelected(items[which])
        }
        builder.show()
    }

    private fun setFeedWeek() {
        var count = 1 // 초기 값 설정

        // 감소 버튼 클릭
        binding.decreaseBtn.setOnClickListener {
            if (count > 1) { // 최소값 설정
                count--
                binding.countText.text = "${count}주"
                foodDuring = count
            }
        }

        // 증가 버튼 클릭
        binding.increaseBtn.setOnClickListener {
            if (count < 12) { // 최대값 설정
                count++
                binding.countText.text = "${count}주"
                foodDuring = count
            }
        }

        foodDuring = count
    }

    private fun setPadWeek() {
        var count = 1 // 초기 값 설정

        // 감소 버튼 클릭
        binding.padsDecreaseBtn.setOnClickListener {
            if (count > 1) { // 최소값 설정
                count--
                binding.padsCountText.text = "${count}주"
                padDuring = count
            }
        }

        // 증가 버튼 클릭
        binding.padsIncreaseBtn.setOnClickListener {
            if (count < 12) { // 최대값 설정
                count++
                binding.padsCountText.text = "${count}주"
                padDuring = count
            }
        }

        padDuring = count
    }

    private fun moveLoginActivity(accessToken: String, userId: Int, signupResponse: SignUpResponse){

        Log.d("message", signupResponse.message)
        Log.d("result", signupResponse.result.id.toString())

        // 회원가입 성공 후 받은 아이디 저장
        val token: String = signupResponse.result.token
        Log.d("토큰", token)

        // 첫 번째 Toast 메시지 표시 (회원가입 완료)
        Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()

        // 로그인 화면으로 이동
        val intent = Intent(this, LoginSplashActivity::class.java)

        // 슬라이드 효과 적용
//        val options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
        startActivity(intent)
    }

    // ▼ SignupMyInfoActivity 안에 추가
    private fun fetchAndCacheDefaultDdayIds(accessToken: String, onDone: () -> Unit) {
        val api = RetrofitObj.getRetrofit(this).create(DDayRetrofitItf::class.java)
        Log.d("ALARM/DEFAULT", "fetch ddays to cache ids...")

        // ✨ getDDays -> getAllDDays 로 변경
        api.getAllDDays("Bearer $accessToken").enqueue(object : Callback<GetAllDDayResponse> {
            override fun onResponse(
                call: Call<GetAllDDayResponse>,
                resp: Response<GetAllDDayResponse>
            ) {
                if (resp.isSuccessful) {
                    val list = resp.body()?.result.orEmpty()

                    val food = list.firstOrNull { it.title.contains("사료") }
                    val pad  = list.firstOrNull { it.title.contains("패드") }

                    getSharedPreferences("app_prefs", MODE_PRIVATE).edit().apply {
                        food?.let { putInt("food_dday_id", it.id) }
                        pad?.let  { putInt("pad_dday_id",  it.id) }
                    }.apply()

                    Log.d("ALARM/DEFAULT", "cached ids -> food=${food?.id}, pad=${pad?.id}")
                } else {
                    Log.e("ALARM/DEFAULT", "getDDays http=${resp.code()} err=${resp.errorBody()?.string()}")
                }
                onDone()
            }

            override fun onFailure(call: Call<GetAllDDayResponse>, t: Throwable) {
                Log.e("ALARM/DEFAULT", "getAllDDays fail=${t.message}", t)
                onDone()
            }
        })
    }

}