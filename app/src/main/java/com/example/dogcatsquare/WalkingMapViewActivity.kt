package com.example.dogcatsquare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.databinding.ActivityMapwalkingMapviewBinding

class WalkingMapViewActivity : AppCompatActivity() {

    lateinit var binding: ActivityMapwalkingMapviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapwalkingMapviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button2.setOnClickListener {
            val intent = Intent(this, WalkingReviewActivity::class.java)
            startActivity(intent)
        }
    }
}