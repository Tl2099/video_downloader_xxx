package com.example.video_downloader_xxx.data.model

data class Bookmark(
    val id: Long = 0,
    val url: String,
    val title: String,
    val faviconBase64: String?,
    val createdAt: Long
)

