package com.example.video_downloader_xxx.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.video_downloader_xxx.data.local.entities.DownloadedVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedVideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: DownloadedVideoEntity)

    @Query("UPDATE downloaded_videos SET title = :name WHERE id = :id")
    suspend fun updateName(id: String, name: String)

    @Delete
    suspend fun delete(video: DownloadedVideoEntity)

    @Query("DELETE FROM downloaded_videos")
    suspend fun deleteAll()

    @Query("SELECT * FROM downloaded_videos ORDER BY downloadedAt DESC")//ASC
    suspend fun getAll(): List<DownloadedVideoEntity>

    @Query("SELECT * FROM downloaded_videos ORDER BY downloadedAt DESC")
    fun observeAll(): Flow<List<DownloadedVideoEntity>>
}