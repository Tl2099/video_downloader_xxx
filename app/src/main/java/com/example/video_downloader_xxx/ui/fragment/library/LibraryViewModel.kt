package com.example.video_downloader_xxx.ui.fragment.library

import androidx.lifecycle.ViewModel
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.data.repository.library.DownloadRepository
import kotlinx.coroutines.flow.StateFlow

class LibraryViewModel: ViewModel() {
    val downloadingVideos: StateFlow<List<VideoInfo>> = DownloadRepository.downloadingVideos

    val completedVideos: StateFlow<List<VideoInfo>> = DownloadRepository.completedVideos
}