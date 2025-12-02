package com.example.video_downloader_xxx.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SubtitleFormat(
    val ext: String,
    val url: String,
    val name: String? = null,
    val protocol: String? = null,
)
