package com.example.video_downloader_xxx.data.local.reposities.video

import com.example.video_downloader_xxx.data.local.dao.DownloadedVideoDao
import com.example.video_downloader_xxx.data.local.entities.DownloadedVideoEntity
import kotlinx.coroutines.flow.Flow

class VideoInfoRepositoryImpl(private val dao: DownloadedVideoDao): VideoInfoRepository {
    override suspend fun insert(video: DownloadedVideoEntity) {
        dao.insert(video)
    }

    override suspend fun updateName(id: String, name: String) {
        dao.updateName(id, name)
    }

    override suspend fun delete(video: DownloadedVideoEntity) {
        dao.delete(video)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun getAll(): List<DownloadedVideoEntity> {
        return dao.getAll()
    }

    override fun observeAll(): Flow<List<DownloadedVideoEntity>> {
        return dao.observeAll()
    }
}