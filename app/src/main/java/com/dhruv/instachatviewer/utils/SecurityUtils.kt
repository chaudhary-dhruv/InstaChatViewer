package com.dhruv.instachatviewer.utils

import android.os.Build
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

object SecurityUtils {

    fun protectSensitiveContent(activity: AppCompatActivity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.setRecentsScreenshotEnabled(false)
        }
    }
}
