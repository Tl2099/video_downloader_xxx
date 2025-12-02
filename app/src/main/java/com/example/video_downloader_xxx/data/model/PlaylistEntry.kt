package com.example.video_downloader_xxx.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistEntry(
    @SerialName("_type") val type: String? = null,
    val ieKey: String? = null,
    val id: String? = null,
    val url: String? = null,
    val title: String? = null,
    val duration: Double? = .0,
    val uploader: String? = null,
    val channel: String? = null,
    val thumbnails: List<Thumbnail>? = emptyList(),
)