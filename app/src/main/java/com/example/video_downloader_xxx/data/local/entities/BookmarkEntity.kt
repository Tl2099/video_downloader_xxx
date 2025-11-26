package com.example.video_downloader_xxx.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val faviconBase64: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)



