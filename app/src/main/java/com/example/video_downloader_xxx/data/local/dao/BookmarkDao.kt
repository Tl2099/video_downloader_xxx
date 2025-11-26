package com.example.video_downloader_xxx.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.video_downloader_xxx.data.local.entities.BookmarkEntity


@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    suspend fun getAllBookmarks(): List<BookmarkEntity>

    @Insert
    suspend fun insertBookmark(entity: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(entity: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("SELECT COUNT(*) FROM bookmarks WHERE url = :url")
    suspend fun exists(url: String): Int

    @Query("DELETE FROM bookmarks")
    suspend fun clearAll()
}
