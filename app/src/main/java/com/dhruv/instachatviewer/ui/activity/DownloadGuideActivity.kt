package com.dhruv.instachatviewer.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dhruv.instachatviewer.databinding.ActivityDownloadGuideBinding
import com.dhruv.instachatviewer.utils.Prefs

class DownloadGuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⚡ If user already imported once, go directly to chat list
        if (Prefs.hasImported(this)) {
            startActivity(Intent(this, ChatListActivity::class.java))
            finish()
            return
        }

        binding = ActivityDownloadGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }
    }
}
