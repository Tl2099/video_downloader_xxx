package com.example.video_downloader_xxx.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.video_downloader_xxx.data.local.entities.WebsiteHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebsiteHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WebsiteHistoryEntity)

    @Query("SELECT * FROM website_history ORDER BY lastVisited DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<WebsiteHistoryEntity>>

    @Query("SELECT * FROM website_history ORDER BY lastVisited DESC")
    fun getHistory(): Flow<List<WebsiteHistoryEntity>>

    @Query("DELETE FROM website_history WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("DELETE FROM website_history")
    suspend fun clearAll()
}