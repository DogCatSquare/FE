package com.example.dogcatsquare.ui.mypage

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.model.login.DeleteUserResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.DialogDeleteBinding
import com.example.dogcatsquare.ui.login.LoginDetailActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomDeleteDialog(context: Context): Dialog(context) {
    private lateinit var itemClickListener: ItemClickListener
    private lateinit var binding: DialogDeleteBinding

    private fun getToken(): String? {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogDeleteBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // 사이즈를 조절하고 싶을 때 사용 (use it when you want to resize dialog)
        // resize(this, 0.8f, 0.4f)

        // 배경을 투명하게 (Make the background transparent)
        // 다이얼로그를 둥글게 표현하기 위해 필요 (Required to round corner)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 바깥쪽 클릭시 종료되도록 함 (Cancel the dialog when you touch outside)
        setCanceledOnTouchOutside(true)

        // 취소 가능 유무
        setCancelable(true)

        binding.cancelBtn.setOnClickListener {
            dismiss() // 다이얼로그 닫기 (Close the dialog)
        }

        binding.deleteBtn.setOnClickListener {
            // 탈퇴 로직 임시
            itemClickListener.onClick()
//            val token = getToken()
//
//            val deleteUserService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
//            deleteUserService.deleteUser("Bearer $token").enqueue(object : Callback<DeleteUserResponse> {
//                override fun onResponse(
//                    call: Call<DeleteUserResponse>,
//                    response: Response<DeleteUserResponse>
//                ) {
//                    if(response.isSuccessful) {
//                        navigateToLogin()
//                    } else {
//                        Toast.makeText(context, "회원 탈퇴에 실패했습니다", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<DeleteUserResponse>, t: Throwable) {
//                    Log.d("RETROFIT/FAILURE", t.message.toString())
//                }
//
//            })
            dismiss()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(context, LoginDetailActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 모든 액티비티 삭제 후 이동
        context.startActivity(intent)
    }

    // 사이즈를 조절하고 싶을 때 사용 (use it when you want to resize dialog)
    private fun resize(dialog: Dialog, width: Float, height: Float){
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT < 30) {
            val size = Point()
            windowManager.defaultDisplay.getSize(size)

            val x = (size.x * width).toInt()
            val y = (size.y * height).toInt()
            dialog.window?.setLayout(x, y)
        } else {
            val rect = windowManager.currentWindowMetrics.bounds

            val x = (rect.width() * width).toInt()
            val y = (rect.height() * height).toInt()
            dialog.window?.setLayout(x, y)
        }
    }

    interface ItemClickListener {
        fun onClick()
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}