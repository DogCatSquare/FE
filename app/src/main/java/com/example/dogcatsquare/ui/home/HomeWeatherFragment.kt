
package com.example.dogcatsquare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.home.WeatherResult
import com.example.dogcatsquare.databinding.FragmentHomeWeatherBinding

class HomeWeatherFragment : Fragment() {
    lateinit var binding: FragmentHomeWeatherBinding

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

        arguments?.let {
            val weatherData = it.getParcelable<WeatherResult>("weatherData")

            weatherData?.let { weather ->
                binding.homeWeatherInfoTv.text = weather.mainMessage + "\n" + weather.subMessage
                binding.homeWeatherLocTv.text = weather.location
                binding.homeWeatherTemperatureIv.text = weather.currentTemp
                binding.maxTempTv.text = weather.maxTemp
                binding.minTempTv.text = weather.minTemp
                binding.rainTv.text = weather.rainProbability

                // 이미지 로드 (Glide 사용)
                Glide.with(this)
                    .load(weather.imageUrl)
                    .into(binding.homeWeatherIv)
            }
        }

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

        return binding.root
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
