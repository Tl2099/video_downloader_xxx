package com.example.video_downloader_xxx.ui.fragment.browser

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_downloader_xxx.util.DownloadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class DownloadViewModel(
    private val manager: VideoDownloadManager
) : ViewModel() {

    private val _downloadVideoState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadVideoState = _downloadVideoState.asStateFlow()

    fun start(url: String, outFile: File) {
        viewModelScope.launch {
            _downloadVideoState.value = DownloadState.Idle

            val info = manager.getVideoInfo(url)
            if (info == null) {
                _downloadVideoState.value = DownloadState.Error("Không thể phân tích video.")
                return@launch
            }

            Log.i("Info_ttdat", "start: ${info.sourceUrl} ${info.videoUrl}")

            manager.downloadVideo(info, outFile)
                .collect { progress ->
                    if (progress.percent in 0f..99f) {
                        _downloadVideoState.value =
                            DownloadState.Downloading(progress.percent.toInt())
                    } else if (progress.percent >= 100f) {
                        _downloadVideoState.value = DownloadState.Success(outFile)
                    }
                }
        }
    }
}