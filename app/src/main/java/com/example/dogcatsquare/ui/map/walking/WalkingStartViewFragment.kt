package com.example.dogcatsquare.ui.map.walking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.dogcatsquare.R
import com.example.dogcatsquare.ui.map.location.MapFragment

class WalkingStartViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mapwalking_startview, container, false)

        val address = arguments?.getString("address", "서대문 안산지락길")

        (activity as? AppCompatActivity)?.apply {
            val toolbar: Toolbar = view.findViewById(R.id.walking_start_toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.title = "서대문 안산지락길"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            // Toolbar의 네비게이션 클릭 리스너 설정
            toolbar.setNavigationOnClickListener {
                // MapFragment로 돌아가기
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, MapFragment())
                    .commit()
            }
        }

        val button: Button = view.findViewById(R.id.ReviewWriting_bt)

        button.setOnClickListener {
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, WalkingMapViewFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }
}