package com.example.video_downloader_xxx.data.repository.library

import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.util.DownloadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DownloadRepository {
    private val _downloadingVideos = MutableStateFlow<List<VideoInfo>>(emptyList())
    val downloadingVideos: StateFlow<List<VideoInfo>> = _downloadingVideos.asStateFlow()

    private val _completedVideos = MutableStateFlow<List<VideoInfo>>(emptyList())
    val completedVideos: StateFlow<List<VideoInfo>> = _completedVideos.asStateFlow()

    fun addDownloading(video: VideoInfo) {
        val current = _downloadingVideos.value.toMutableList()

        if (current.none { it.videoUrl == video.videoUrl }) {
            current.add(video.copy(downloadStatus = DownloadStatus.DOWNLOADING, progress = 0f))
            _downloadingVideos.value = current
        }
    }

    fun updateProgress(videoUrl: String, percent: Float) {
        _downloadingVideos.value =
            _downloadingVideos.value.map { v ->
                if (v.videoUrl == videoUrl) {
                    v.copy(progress = percent)
                } else v
            }
    }
    
}