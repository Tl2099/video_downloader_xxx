package com.example.video_downloader_xxx.ui.fragment.browser.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.data.repository.VideoDownloadManager
import com.example.video_downloader_xxx.util.DownloadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class DownloadViewModel(
    private val manager: VideoDownloadManager
) : ViewModel() {

    private val _videoInfo = MutableStateFlow<VideoInfo?>(null)
    val videoInfo: StateFlow<VideoInfo?> = _videoInfo.asStateFlow()

    private val _downloadVideoState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadVideoState = _downloadVideoState.asStateFlow()

    fun fetchVideoInfo(url: String) {
        viewModelScope.launch {
            try {
                _videoInfo.value = manager.getVideoInfo(url)
                if (_videoInfo.value == null) {
                    _downloadVideoState.value = DownloadState.Error("Không thể phân tích video.")
                    return@launch
                }
                Log.i(
                    "Info_ttdat",
                    "start: ${_videoInfo.value?.sourceUrl} ${_videoInfo.value?.videoUrl}"
                )
            } catch (e: Exception) {
                Log.e("DownloadViewModel", "Error fetching video info: ${e.message}", e)
                _downloadVideoState.value =
                    DownloadState.Error("Error fetching video info: ${e.message}")
            }
        }
    }

    fun downloadVideo(videoInfo: VideoInfo, outFile: File) {
        viewModelScope.launch {
            _downloadVideoState.value = DownloadState.Idle
            manager.downloadVideo(videoInfo, outFile).collect { progress ->
                if (progress.percent in 0f..99f) {
                    _downloadVideoState.value = DownloadState.Downloading(progress.percent.toInt())
                } else if (progress.percent >= 100f) {
                    _downloadVideoState.value = DownloadState.Success(outFile)
                }
            }
        }
    }

    fun start(url: String, outFile: File) {
        viewModelScope.launch {
            _downloadVideoState.value = DownloadState.Idle

            fetchVideoInfo(url)
//            val info = manager.getVideoInfo(url)
//            if(info == null){
//                _downloadVideoState.value = DownloadState.Error("Không thể phân tích video.")
//                return@launch
//            }
//            Log.i("Info_ttdat", "start: ${info.sourceUrl} ${info.videoUrl}")

            val info = videoInfo.value
            if(info != null){
                downloadVideo(info, outFile)
            }

//            manager.downloadVideo(info, outFile)
//                .collect { progress ->
//                    if (progress.percent in 0f..99f) {
//                        _downloadVideoState.value =
//                            DownloadState.Downloading(progress.percent.toInt())
//                    } else if (progress.percent >= 100f) {
//                        _downloadVideoState.value = DownloadState.Success(outFile)
//                    }
//                }
        }
    }
}