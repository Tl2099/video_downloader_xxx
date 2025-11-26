package com.example.video_downloader_xxx.data.model

data class WebHistory(
    val url: String,
    val title: String,
    val faviconUrl: String?,
    val lastVisited: Long = System.currentTimeMillis()
)