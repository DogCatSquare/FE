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

