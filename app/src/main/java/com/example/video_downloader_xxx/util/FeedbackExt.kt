package com.example.video_downloader_xxx.util


import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

fun Context.openFeedbackEmail(
    emailAddress: String = "sonln@umaxsoft.com",
    subject: String = "User Feedback"
) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }

    try {
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(this, "Không tìm thấy ứng dụng Email", Toast.LENGTH_SHORT).show()
    }
}
