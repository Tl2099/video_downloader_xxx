package com.example.video_downloader_xxx.data.repository.bookmark

import android.util.Log
import com.example.video_downloader_xxx.data.local.dao.BookmarkDao
import com.example.video_downloader_xxx.data.mapper.toDomain
import com.example.video_downloader_xxx.data.mapper.toEntity
import com.example.video_downloader_xxx.data.model.Bookmark

class BookmarkRepositoryImpl(
    private val dao: BookmarkDao
) : BookmarkRepository {

    override suspend fun getBookmarks(): List<Bookmark> {
        return dao.getAllBookmarks().map { it.toDomain() }
    }

    override suspend fun addBookmark(bookmark: Bookmark) {
        val id = dao.insertBookmark(bookmark.toEntity())
        Log.d("BookmarkRepo", "Inserted ID = $id")
    }

    override suspend fun removeBookmark(bookmark: Bookmark) {
        dao.deleteBookmark(bookmark.toEntity())
    }

    override suspend fun isBookmarked(url: String): Boolean {
        return dao.exists(url) > 0
    }
    override suspend fun deleteAll() {
        dao.clearAll()
    }
}
