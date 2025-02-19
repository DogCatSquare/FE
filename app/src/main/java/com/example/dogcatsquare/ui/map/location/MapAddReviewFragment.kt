package com.example.dogcatsquare.ui.map.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.data.map.PlaceReviewRequest
import com.example.dogcatsquare.databinding.FragmentMapAddReviewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.io.InputStream

class MapAddReviewFragment : Fragment() {
    private var _binding: FragmentMapAddReviewBinding? = null
    private val binding get() = _binding!!
    private val selectedImages = mutableListOf<Uri>()
    private var placeId: Int = -1

    // 갤러리 실행을 위한 ActivityResultLauncher 선언
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            if (binding.imageContainer.childCount <= 6) { // imageView14 포함하여 6개
                addNewImageFromUri(selectedImageUri)
            } else {
                Toast.makeText(requireContext(), "이미지는 최대 5개까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 권한 요청을 위한 ActivityResultLauncher 선언
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(context, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            placeId = it.getInt("placeId", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapAddReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupReviewEditText()
        setupImageAddButton()
        setupDoneButton()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupDoneButton() {
        binding.doneButton.setOnClickListener {
            val reviewText = binding.etReview.getText()
            if (reviewText.length >= 20) {
                uploadReviewWithImages()
            } else {
                Toast.makeText(requireContext(), "리뷰는 20자 이상 작성해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupReviewEditText() {
        binding.etReview.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDoneButtonState(s?.length ?: 0 >= 20)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        updateDoneButtonState(false)
    }

    private fun setupImageAddButton() {
        binding.imageView14.setOnClickListener {
            checkAndRequestPermission()
        }
    }

    private fun checkAndRequestPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openGallery()
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }
            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openGallery()
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun addNewImageFromUri(imageUri: Uri) {
        selectedImages.add(imageUri)

        val newImageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_8)
                width = resources.getDimensionPixelSize(R.dimen.review_image_size)
                height = resources.getDimensionPixelSize(R.dimen.review_image_size)
            }

            scaleType = ImageView.ScaleType.CENTER_CROP
            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_image_background)
            clipToOutline = true

            setImageURI(imageUri)

            setOnClickListener {
                binding.imageContainer.removeView(this)
                selectedImages.remove(imageUri)
            }
        }

        val container = binding.imageContainer
        container.removeView(binding.imageView14)
        container.addView(newImageView)
        container.addView(binding.imageView14)
    }

    private fun uploadReviewWithImages() {
        lifecycleScope.launch {
            try {
                binding.doneButton.isEnabled = false

                val token = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    ?.getString("token", null)

                if (token == null) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 선택된 이미지들을 MultipartBody.Part로 변환
                val imageParts = selectedImages.map { uri ->
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val file = createTempFileFromInputStream(inputStream)

                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("placeReviewImages", file.name, requestFile)
                }

                val reviewContent = binding.etReview.getText()
                val reviewRequest = PlaceReviewRequest(
                    content = reviewContent,
                    placeReviewImages = emptyList()
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.createPlaceReview(
                        token = "Bearer $token",
                        placeId = placeId,
                        request = reviewRequest,
                        images = imageParts
                    )
                }

                if (response.isSuccess) {
                    Toast.makeText(requireContext(), "리뷰가 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                    (parentFragment as? MapDetailFragment)?.refreshPlaceDetails()
                } else {
                    Toast.makeText(requireContext(), response.message ?: "리뷰 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                binding.doneButton.isEnabled = true
            }
        }
    }

    private fun createTempFileFromInputStream(inputStream: InputStream?): File {
        inputStream?.use { input ->
            val file = File.createTempFile("image_", ".jpg", requireContext().cacheDir)
            file.outputStream().use { output ->
                input.copyTo(output)
            }
            return file
        } ?: throw IOException("Failed to create temp file")
    }

    private fun updateDoneButtonState(isEnabled: Boolean) {
        binding.doneButton.setImageResource(
            if (isEnabled) R.drawable.bt_activated_complete
            else R.drawable.bt_deactivated_complete
        )
        binding.doneButton.isEnabled = isEnabled
    }

    private fun handleError(e: Exception) {
        val errorMessage = when (e) {
            is retrofit2.HttpException -> {
                when (e.code()) {
                    401 -> "로그인이 필요합니다."
                    403 -> "권한이 없습니다."
                    404 -> "데이터를 찾을 수 없습니다."
                    else -> "서버 오류가 발생했습니다. (${e.code()})"
                }
            }
            is java.io.IOException -> "네트워크 연결을 확인해주세요."
            else -> "알 수 없는 오류가 발생했습니다: ${e.message}"
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(placeId: Int) = MapAddReviewFragment().apply {
            arguments = Bundle().apply {
                putInt("placeId", placeId)
            }
        }
    }
}