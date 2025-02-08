package com.example.dogcatsquare.ui.map

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.databinding.ActivityMapwalkingStartBinding

class WalkingStartActivity : AppCompatActivity() {

    lateinit var binding : ActivityMapwalkingStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapwalkingStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ReviewWritingBt.setOnClickListener {
            val intent = Intent(this, WalkingMapViewActivity::class.java)
            startActivity(intent)
        }
    }
}