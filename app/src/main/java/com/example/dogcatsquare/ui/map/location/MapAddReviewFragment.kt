package com.example.dogcatsquare.ui.map.location

import android.Manifest
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
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapAddReviewBinding

class MapAddReviewFragment : Fragment() {
    private var _binding: FragmentMapAddReviewBinding? = null
    private val binding get() = _binding!!

    // 갤러리 실행을 위한 ActivityResultLauncher 선언
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            addNewImageFromUri(selectedImageUri)
        }
    }

    // 권한 요청을 위한 ActivityResultLauncher 선언
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openGallery()
        } else {
            // 권한이 거부된 경우 사용자에게 알림
            Toast.makeText(context, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
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
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupReviewEditText() {
        binding.etReview.isEnabled = true
        binding.etReview.alpha = 1.0f

        // TextWatcher 추가
        binding.etReview.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                updateDoneButtonState(length >= 20)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 초기 상태 설정
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
                // Android 13 이상
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
                // Android 12 이하
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
        // 새로운 이미지뷰 생성
        val newImageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_8)
                // 이미지 크기 설정
                width = resources.getDimensionPixelSize(R.dimen.review_image_size)
                height = resources.getDimensionPixelSize(R.dimen.review_image_size)
            }

            // 이미지 스케일 타입 설정
            scaleType = ImageView.ScaleType.CENTER_CROP

            // 둥근 모서리 배경 설정
            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_image_background)
            clipToOutline = true  // 이미지를 배경 모양에 맞게 클립

            // 선택한 이미지 설정
            setImageURI(imageUri)

            // 클릭 시 삭제
            setOnClickListener {
                binding.imageContainer.removeView(this)
            }
        }

        // 컨테이너에 새 이미지뷰 추가
        val container = binding.imageContainer
        container.removeView(binding.imageView14)
        container.addView(newImageView)
        container.addView(binding.imageView14)
    }

    private fun updateDoneButtonState(isEnabled: Boolean) {
        binding.doneButton.setImageResource(
            if (isEnabled) R.drawable.bt_activated_complete
            else R.drawable.bt_deactivated_complete
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}