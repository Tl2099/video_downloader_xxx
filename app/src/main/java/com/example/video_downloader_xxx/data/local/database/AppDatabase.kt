package com.example.video_downloader_xxx.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.video_downloader_xxx.data.local.dao.DownloadedVideoDao
import com.example.video_downloader_xxx.data.local.dao.WebsiteHistoryDao
import com.example.video_downloader_xxx.data.local.entities.DownloadedVideoEntity
import com.example.video_downloader_xxx.data.local.entities.WebsiteHistoryEntity


@Database(
    entities = [
        DownloadedVideoEntity::class,
        WebsiteHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadedVideoDao(): DownloadedVideoDao
    abstract fun websiteHistoryDao(): WebsiteHistoryDao

}