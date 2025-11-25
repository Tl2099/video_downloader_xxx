package com.example.video_downloader_xxx.ui.fragment.browser.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_downloader_xxx.data.local.entities.WebsiteHistoryEntity
import com.example.video_downloader_xxx.data.mapper.toWebHistory
import com.example.video_downloader_xxx.data.model.WebHistory
import com.example.video_downloader_xxx.data.repository.webHistory.WebsiteHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WebsiteHistoryViewModel(
    private val repo: WebsiteHistoryRepository
) : ViewModel() {

    val recent = repo.getRecentWebsite().stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    val history = repo.getHistory().stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    fun addVisit(item: WebsiteHistoryEntity) {
        viewModelScope.launch {
            repo.addVisit(item.toWebHistory())
        }
    }

    fun delete(url: String) {
        viewModelScope.launch {
            repo.delete(url)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repo.deleteAll()
        }
    }
}
