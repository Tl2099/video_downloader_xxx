package com.example.video_downloader_xxx.ui.fragment.browser.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.data.repository.web.DownloadVideosOnWebRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WebViewModel(
    private val repo: DownloadVideosOnWebRepository
): ViewModel() {

    private val _videoDetected = MutableStateFlow<VideoInfo?>(null)
    val videoDetected: StateFlow<VideoInfo?> = _videoDetected

    fun checkUrl(url: String) {
        viewModelScope.launch {
            val info = repo.getVideoInfo(url)
            _videoDetected.value = info
        }
    }
}