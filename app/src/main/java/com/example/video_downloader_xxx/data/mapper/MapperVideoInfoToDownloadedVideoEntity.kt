package com.example.video_downloader_xxx.data.mapper

import com.example.video_downloader_xxx.data.local.entities.BookmarkEntity
import com.example.video_downloader_xxx.data.local.entities.DownloadedVideoEntity
import com.example.video_downloader_xxx.data.local.entities.WebsiteHistoryEntity
import com.example.video_downloader_xxx.data.model.Bookmark
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.data.model.WebHistory
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
        thumbnailUrl = this.thumbnail,
        downloadedAt = downloadedAt ?: System.currentTimeMillis()
    )
}

fun DownloadedVideoEntity.toVideoInfo(): VideoInfo {
    return VideoInfo(
        id = this.id,
        title = this.title,
        videoUrl = this.videoUrl,
        sourceUrl = this.sourceUrl ?: "",
        thumbnail = this.thumbnailUrl,
        fileSize = this.fileSize,
        duration = this.duration,
        localPath = this.filePath,
        downloadStatus = DownloadStatus.SUCCESS,
        downloadedAt = this.downloadedAt
    )
}

fun WebsiteHistoryEntity.toWebHistory() = WebHistory(
    url = url,
    title = title,
    faviconUrl = faviconUrl,
    lastVisited = lastVisited
)

fun WebHistory.toWebsiteHistoryEntity() = WebsiteHistoryEntity(
    url = url,
    title = title,
    faviconUrl = faviconUrl,
    lastVisited = lastVisited
)

fun BookmarkEntity.toDomain() = Bookmark(
    id = id,
    url = url,
    title = title,
    faviconBase64 = faviconBase64,
    createdAt = createdAt
)

fun Bookmark.toEntity() = BookmarkEntity(
    id = id,
    url = url,
    title = title,
    faviconBase64 = faviconBase64,
    createdAt = createdAt
)



