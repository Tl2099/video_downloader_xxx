package com.example.video_downloader_xxx.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistResult(
    val uploader: String? = null,
    val availability: String? = null,
    val channel: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("_type") val type: String? = null,
    val entries: List<PlaylistEntry>? = emptyList(),
    @SerialName("webpage_url") val webpageUrl: String? = null,
    @SerialName("original_url") val originalUrl: String? = null,
    @SerialName("extractor_key") val extractorKey: String? = null,
) : YoutubeDLInfo