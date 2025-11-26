package com.example.video_downloader_xxx.data.repository.bookmark

import com.example.video_downloader_xxx.data.model.Bookmark

interface BookmarkRepository {
    suspend fun getBookmarks(): List<Bookmark>
    suspend fun addBookmark(bookmark: Bookmark)
    suspend fun removeBookmark(bookmark: Bookmark)
    suspend fun isBookmarked(url: String): Boolean
    suspend fun deleteAll()
}
