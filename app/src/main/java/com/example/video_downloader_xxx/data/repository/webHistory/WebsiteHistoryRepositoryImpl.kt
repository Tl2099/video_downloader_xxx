package com.example.video_downloader_xxx.data.repository.webHistory

import com.example.video_downloader_xxx.data.local.dao.WebsiteHistoryDao
import com.example.video_downloader_xxx.data.mapper.toWebHistory
import com.example.video_downloader_xxx.data.mapper.toWebsiteHistoryEntity
import com.example.video_downloader_xxx.data.model.WebHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WebsiteHistoryRepositoryImpl(
    private val dao: WebsiteHistoryDao
): WebsiteHistoryRepository {
    override suspend fun addVisit(website: WebHistory) {
        dao.upsert(website.toWebsiteHistoryEntity())
    }

    override fun getRecentWebsite(limit: Int) =
        dao.getRecent(limit).map {
            it.map { entity ->  entity.toWebHistory() }
        }

    override fun getHistory(): Flow<List<WebHistory>> =
        dao.getHistory().map { it.map { entity -> entity.toWebHistory() } }

    override suspend fun delete(url: String) {
        dao.deleteByUrl(url)
    }

    override suspend fun deleteAll() {
        dao.clearAll()
    }
}