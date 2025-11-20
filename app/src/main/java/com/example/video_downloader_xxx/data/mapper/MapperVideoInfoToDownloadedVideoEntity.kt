package com.example.video_downloader_xxx.data.mapper

import com.example.video_downloader_xxx.data.local.entities.DownloadedVideoEntity
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.util.DownloadStatus

fun VideoInfo.toEntity(): DownloadedVideoEntity {
    return DownloadedVideoEntity(
        id = this.id,
        title = this.title,
        videoUrl = this.videoUrl ?: "",
        sourceUrl = this.sourceUrl,
        filePath = this.localPath ?: "",
        fileSize = this.fileSize,
        duration = this.duration,
        thumbnailUrl = this.thumbnailUrl,
        downloadedAt = downloadedAt ?: System.currentTimeMillis()
    )
}

fun DownloadedVideoEntity.toVideoInfo(): VideoInfo {
    return VideoInfo(
        id = this.id,
        title = this.title,
        videoUrl = this.videoUrl,
        sourceUrl = this.sourceUrl ?: "",
        thumbnailUrl = this.thumbnailUrl,
        fileSize = this.fileSize,
        duration = this.duration,
        localPath = this.filePath,
        downloadStatus = DownloadStatus.SUCCESS,
        downloadedAt = this.downloadedAt
    )
}

