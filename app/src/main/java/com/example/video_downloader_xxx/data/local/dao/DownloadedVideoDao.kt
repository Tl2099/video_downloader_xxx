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

    @Delete
    suspend fun delete(video: DownloadedVideoEntity)

    @Query("DELETE FROM downloaded_videos")
    suspend fun deleteAll()

    @Query("SELECT * FROM downloaded_videos ORDER BY id DESC")
    suspend fun getAll(): List<DownloadedVideoEntity>

    @Query("SELECT * FROM downloaded_videos ORDER BY id DESC")
    fun observeAll(): Flow<List<DownloadedVideoEntity>>
}