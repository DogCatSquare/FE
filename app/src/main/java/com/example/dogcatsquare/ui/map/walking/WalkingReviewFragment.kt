package com.example.dogcatsquare.ui.map.walking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapwalkingReviewBinding

class WalkingReviewFragment : Fragment() {

    lateinit var binding: FragmentMapwalkingReviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mapwalking_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.apply {
            val toolbar: Toolbar = view.findViewById(R.id.walking_review_toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.title = "산책코스 추천하기"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        val nextBt: Button = view.findViewById(R.id.Completion_bt)
        nextBt.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()

            val walkingReviewTypeFragment = WalkingReviewTypeFragment()

            transaction.replace(R.id.main_frm, walkingReviewTypeFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
}