package com.dhruv.instachatviewer.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dhruv.instachatviewer.databinding.ActivityDownloadGuideBinding

class DownloadGuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }
    }
}
