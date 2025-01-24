package com.example.dogcatsquare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.databinding.ActivityMapwalkingReviewBinding

class WalkingReviewActivity : AppCompatActivity() {
    lateinit var binding: ActivityMapwalkingReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapwalkingReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.CompletionBt.setOnClickListener {
            val intent = Intent(this, WalkingReviewTypeActivity::class.java)
            startActivity(intent)
        }
    }
}