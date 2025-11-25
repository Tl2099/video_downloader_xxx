package com.example.video_downloader_xxx.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "website_history")
data class WebsiteHistoryEntity(
    @PrimaryKey val url: String,
    val title: String,
    val faviconUrl: String?,
    val lastVisited: Long
)
