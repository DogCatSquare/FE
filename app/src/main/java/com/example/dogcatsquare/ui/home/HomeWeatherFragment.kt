package com.example.dogcatsquare.ui.home

import WeatherViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.home.WeatherResult
import com.example.dogcatsquare.databinding.FragmentHomeWeatherBinding

class HomeWeatherFragment : Fragment() {
    lateinit var binding: FragmentHomeWeatherBinding

    private val viewModel: WeatherViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeWeatherBinding.inflate(inflater, container, false)

        binding.homeBellIv.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, AlarmFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // ViewModel 관찰
        viewModel.weatherData.observe(viewLifecycleOwner) { weatherResponse ->
            weatherResponse?.let { weather ->
                Log.d("HomeWeatherFragment", "Weather 업데이트: ${weather.result.mainMessage}")
                updateWeatherUI(weather.result)
            }
        }

        return binding.root
    }

    private fun updateWeatherUI(weather: WeatherResult) {
        with(binding) {
            homeWeatherInfoTv.text = "${weather.mainMessage}\n${weather.subMessage}"
            homeWeatherLocTv.text = weather.location
            ddayTv.text = weather.ddayMessage
            ddayDateTv.text = weather.ddayDate

            Glide.with(this@HomeWeatherFragment)
                .load(weather.imageUrl)
                .into(homeWeatherIv)

            if (binding.homeWeatherInfoTv.text.contains("비 오는 날")) {
                // 상단바 색깔
                requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_gray)
                binding.homeWeatherCl.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.home_gray))

            } else if (binding.homeWeatherInfoTv.text.contains("오늘 날씨 맑음")) {
                // 상단바 색깔
                requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.light_blue)
                binding.homeWeatherCl.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
            } else if (binding.homeWeatherInfoTv.text.contains("바람 부는 날")) {
                // 상단바 색깔
                requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.light_blue)
                binding.homeWeatherCl.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
            } else if (binding.homeWeatherInfoTv.text.contains("눈 오는 날")) {
                // 상단바 색깔
                requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_yellow)
                binding.homeWeatherCl.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.home_yellow))
            } else {
                // 상단바 색깔
                requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home_yellow)
                binding.homeWeatherCl.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.home_yellow))
            }
        }
    }

    companion object {
        fun newInstance(weather: WeatherResult, position: Int): HomeWeatherFragment {
            return HomeWeatherFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("weatherData", weather)
                    putInt("position", position)
                }
            }
        }
    }
}