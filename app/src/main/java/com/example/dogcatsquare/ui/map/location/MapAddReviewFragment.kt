package com.example.dogcatsquare.ui.map.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.map.PlaceReviewRequest
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.databinding.FragmentMapAddReviewBinding
import com.example.dogcatsquare.ui.map.walking.SelectedImageAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class MapAddReviewFragment : Fragment() {
    private var _binding: FragmentMapAddReviewBinding? = null
    private val binding get() = _binding!!
    private var placeId: Int = -1

    private var selectedBitmaps: MutableList<Bitmap> = mutableListOf()
    private lateinit var imageAdapter: SelectedImageAdapter

    // ✅ 갤러리 다중 선택을 위한 modern launcher
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let { handleSelectedUris(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            placeId = it.getInt("placeId", -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapAddReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupBackButton()
        setupReviewEditText()
        setupImageAddButton()
        setupDoneButton()
    }

    // ✅ 1. 리사이클러뷰 및 어댑터 설정
    private fun setupRecyclerView() {
        imageAdapter = SelectedImageAdapter(selectedBitmaps) { position ->
            selectedBitmaps.removeAt(position)
            updateImageUI()
        }
        binding.rvSelectedImages.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    // ✅ 2. 갤러리 이미지 처리 (최대 5장)
    private fun handleSelectedUris(uris: List<Uri>) {
        for (uri in uris) {
            if (selectedBitmaps.size >= 5) {
                Toast.makeText(requireContext(), "사진은 최대 5장까지 가능합니다.", Toast.LENGTH_SHORT).show()
                break
            }
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                }
                selectedBitmaps.add(bitmap)
            } catch (e: Exception) {
                Log.e("MapAddReview", "이미지 변환 실패: ${e.message}")
            }
        }
        updateImageUI()
    }

    private fun updateImageUI() {
        imageAdapter.notifyDataSetChanged()
        binding.tvImageCount.text = "${selectedBitmaps.size}/5"
        updateDoneButtonState(checkValidReview())
    }

    private fun setupImageAddButton() {
        binding.addImgBt.setOnClickListener {
            if (selectedBitmaps.size >= 5) {
                Toast.makeText(requireContext(), "사진은 최대 5장까지 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                galleryLauncher.launch("image/*")
            }
        }
    }

    private fun setupReviewEditText() {
        binding.etReview.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDoneButtonState(checkValidReview())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkValidReview(): Boolean {
        val hasEnoughText = binding.etReview.getText().toString().trim().length >= 20
        val hasImages = selectedBitmaps.isNotEmpty()
        return hasEnoughText && hasImages
    }

    private fun updateDoneButtonState(isEnabled: Boolean) {
        binding.doneButton.setImageResource(
            if (isEnabled) R.drawable.bt_activated_complete
            else R.drawable.bt_deactivated_complete
        )
        binding.doneButton.isEnabled = isEnabled
    }

    // ✅ 3. 서버 업로드 로직 (Multipart 변환)
    private fun uploadReviewWithImages() {
        lifecycleScope.launch {
            try {
                binding.doneButton.isEnabled = false

                val token = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    ?.getString("token", null) ?: return@launch

                // Bitmap 리스트를 MultipartBody.Part 리스트로 변환
                val imageParts = selectedBitmaps.mapIndexed { index, bitmap ->
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val requestBody = outputStream.toByteArray().toRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("placeReviewImages", "review_image_$index.jpg", requestBody)
                }

                val reviewContent = binding.etReview.getText().toString()
                val reviewRequest = PlaceReviewRequest(content = reviewContent, placeReviewImages = emptyList())

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.createPlaceReview(
                        token = "Bearer $token",
                        placeId = placeId,
                        request = reviewRequest,
                        images = imageParts
                    )
                }

                if (response.isSuccess) {
                    Toast.makeText(requireContext(), "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    goBackWithRefresh()
                } else {
                    Toast.makeText(requireContext(), response.message ?: "등록 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                binding.doneButton.isEnabled = true
            }
        }
    }

    private fun goBackWithRefresh() {
        requireActivity().supportFragmentManager.fragments
            .filterIsInstance<MapDetailFragment>()
            .firstOrNull()?.let { detailFragment ->
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                    .show(detailFragment)
                    .commit()
                detailFragment.refreshPlaceDetails()
            }
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun setupDoneButton() {
        binding.doneButton.setOnClickListener {
            if (checkValidReview()) uploadReviewWithImages()
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun handleError(e: Exception) {
        Log.e("MapAddReview", "에러 발생: ${e.message}")
        Toast.makeText(requireContext(), "오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(placeId: Int) = MapAddReviewFragment().apply {
            arguments = Bundle().apply { putInt("placeId", placeId) }
        }
    }
}