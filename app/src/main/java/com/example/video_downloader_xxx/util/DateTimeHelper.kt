package com.example.video_downloader_xxx.util

fun formatTimestamp(time: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(time))
}
