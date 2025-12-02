package com.example.video_downloader_xxx.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Thumbnail(val url: String, val height: Double = .0, val width: Double = .0)
