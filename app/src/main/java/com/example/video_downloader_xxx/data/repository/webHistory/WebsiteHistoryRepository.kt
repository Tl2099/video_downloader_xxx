package com.example.video_downloader_xxx.data.repository.webHistory

import com.example.video_downloader_xxx.data.local.entities.BookmarkEntity
import com.example.video_downloader_xxx.data.model.Bookmark
import com.example.video_downloader_xxx.data.model.WebHistory
import kotlinx.coroutines.flow.Flow

interface WebsiteHistoryRepository {
    suspend fun addVisit(website: WebHistory)
    fun getRecentWebsite(limit: Int = 7): Flow<List<WebHistory>>
    fun getHistory(): Flow<List<WebHistory>>
    suspend fun delete(url: String)
    suspend fun deleteAll()
}