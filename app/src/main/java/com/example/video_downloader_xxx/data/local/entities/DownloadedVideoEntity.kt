package com.example.video_downloader_xxx.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "downloaded_videos")
data class DownloadedVideoEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val title: String,
    val videoUrl: String,
    val sourceUrl: String?,
    val filePath: String,
    val fileSize: String?,
    val duration: String?,
    val thumbnailUrl: String?,
    val downloadedAt: Long,
)

