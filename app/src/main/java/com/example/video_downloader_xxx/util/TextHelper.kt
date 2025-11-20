package com.example.video_downloader_xxx.util

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

object TextHelper {

    fun guessNameFromUrl(url: String): String {
        return try {
            val path = android.net.Uri.parse(url).lastPathSegment ?: "video"
            path.substringAfterLast('/').substringBefore('?').substringBefore('#')
                .ifBlank { "video" }
        } catch (_: Exception) {
            "video"
        }
    }

    fun extractExtension(url: String?): String {
        if (url.isNullOrBlank()) return ""
        val seg = url.substringBefore('?').substringBefore('#')
        val ext = seg.substringAfterLast('.', "")
        return if (ext.length in 1..5 && ext.all { it.isLetter() }) ext.lowercase() else ""
    }

    fun sanitizeFileName(name: String): String {
        val stripped = name.replace(Regex("""[\\/:*?"<>|]"""), " ")
            .replace(Regex("""\s+"""), " ").trim()
        return stripped.take(128).ifBlank { "video" }
    }

    fun String.isYouTubeUrl(): Boolean {
        val url = this.lowercase()
        return url.contains("youtube.com") ||
                url.contains("youtu.be") ||
                url.contains("m.youtube.com")
    }

    fun validateName(
        input: String,
        editText: EditText
    ): Boolean {
        return when {
            input.isBlank() -> {
                editText.error = "Tên không được để trống"; false
            }

            input.length > 128 -> {
                editText.error = "Tên quá dài (<= 128 ký tự)"; false
            }

            input.contains(Regex("""[\\/:*?"<>|]""")) -> {
                editText.error = "Tên chứa ký tự không hợp lệ"; false
            }

            else -> {
                editText.error = null; true
            }
        }
    }

}
