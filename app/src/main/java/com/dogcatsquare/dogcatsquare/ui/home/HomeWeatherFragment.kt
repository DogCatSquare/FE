
package com.dogcatsquare.dogcatsquare.ui.home

import AlarmFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dogcatsquare.dogcatsquare.R
import com.dogcatsquare.dogcatsquare.data.model.home.WeatherResult
import com.dogcatsquare.dogcatsquare.databinding.FragmentHomeWeatherBinding
import com.dogcatsquare.dogcatsquare.MainActivity

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

        binding.homeSettingIv.setOnClickListener {

            requireActivity().supportFragmentManager.beginTransaction()

                .replace(R.id.main_frm, HomeSettingFragment())

                .addToBackStack(null)

                .commitAllowingStateLoss()

        }

        // 알림 뱃지 관찰 및 UI 업데이트
        (requireActivity() as? MainActivity)?.unreadCount?.observe(viewLifecycleOwner) { count ->
            if (count != null && count > 0) {
                binding.homeBellBadgeTv.text = count.toString()
                binding.homeBellBadgeTv.visibility = android.view.View.VISIBLE
            } else {
                binding.homeBellBadgeTv.visibility = android.view.View.GONE
            }
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

        val colorResId = if (binding.homeWeatherInfoTv.text.contains("비 오는 날")) {
            R.color.home_gray
        } else if (binding.homeWeatherInfoTv.text.contains("오늘 날씨 맑음") || binding.homeWeatherInfoTv.text.contains("바람 부는 날")) {
            R.color.light_blue
        } else {
            R.color.home_yellow
        }

        binding.homeWeatherCl.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
        com.dogcatsquare.dogcatsquare.utils.StatusBarHelper.saveStatusBarColor(requireContext(), colorResId)
        com.dogcatsquare.dogcatsquare.utils.StatusBarHelper.applySavedStatusBarColor(requireActivity())

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
