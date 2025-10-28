package com.example.video_downloader_xxx.data.model

import com.example.video_downloader_xxx.util.DownloadStatus
import java.util.UUID

data class VideoInfo (
    val id: String = UUID.randomUUID().toString(),
    val sourceUrl: String,
    val videoUrl: String? = null,
    val title: String,
    val thumbnailUrl: String? = null,
    val duration: String? = null,
    val fileSize: String? = null,
    val downloadStatus: DownloadStatus = DownloadStatus.PENDING
)