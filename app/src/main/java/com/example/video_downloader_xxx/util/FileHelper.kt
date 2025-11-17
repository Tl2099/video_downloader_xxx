package com.example.video_downloader_xxx.util

import android.content.Context
import android.os.Build
import android.os.Environment
import android.webkit.MimeTypeMap
import java.io.File

object FileHelper {
    fun getAppVideoDir(context: Context): File {
        val appFolderName = context.getString(
            context.resources.getIdentifier("app_name", "string", context.packageName)
        )

        val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                appFolderName
            )
        } else {
            @Suppress("DEPRECATION")
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                appFolderName
            )
        }

        val videoDir = File(dir, "video")

        if (!videoDir.exists()) {
            videoDir.mkdirs()
        }

        return videoDir
    }

    fun getVideoFolder(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "videos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }


    fun createVideoFile(context: Context, url: String): File {
        val ext = MimeTypeMap.getFileExtensionFromUrl(url)
        val dir = getAppVideoDir(context)
        val fileName = "video_${System.currentTimeMillis()}.${ext}"
        return File(dir, fileName)
    }

    fun String.isValidUrl(): Boolean {
        val urlRegex = "^(https?://)?([\\w.-]+)\\.([a-z]{2,6})([/\\w .-]*)*/?$"
        return this.matches(urlRegex.toRegex())
    }
}