package com.example.video_downloader_xxx.data.local.reposities.video

import com.example.video_downloader_xxx.data.local.entities.DownloadedVideoEntity
import kotlinx.coroutines.flow.Flow

interface VideoInfoRepository {
    suspend fun insert(video: DownloadedVideoEntity)
    suspend fun updateName(id: String, name: String)
    suspend fun delete(video: DownloadedVideoEntity)
    suspend fun deleteAll()
    suspend fun getAll(): List<DownloadedVideoEntity>
    fun observeAll(): Flow<List<DownloadedVideoEntity>>
}