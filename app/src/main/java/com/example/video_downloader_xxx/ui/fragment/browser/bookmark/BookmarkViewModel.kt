package com.example.video_downloader_xxx.ui.fragment.browser.bookmark

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_downloader_xxx.data.local.entities.BookmarkEntity
import com.example.video_downloader_xxx.data.mapper.toDomain
import com.example.video_downloader_xxx.data.model.Bookmark
import com.example.video_downloader_xxx.data.repository.bookmark.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookmarkViewModel(
    private val repo: BookmarkRepository
) : ViewModel() {

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks

    fun loadBookmarks() {
        viewModelScope.launch {
            _bookmarks.value = repo.getBookmarks()
        }
    }

    fun toggleBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            if (repo.isBookmarked(bookmark.url)) {
                repo.removeBookmark(bookmark)
            } else {
                repo.addBookmark(bookmark)
            }
            loadBookmarks()
        }
    }

    fun delete(item: Bookmark) {
        viewModelScope.launch {
            repo.removeBookmark(item)
            loadBookmarks()
        }
    }


    suspend fun isBookmarked(url: String): Boolean {
        return repo.isBookmarked(url)
    }

    fun clearAll() {
        viewModelScope.launch {
            repo.deleteAll()
            loadBookmarks()
        }
    }

}