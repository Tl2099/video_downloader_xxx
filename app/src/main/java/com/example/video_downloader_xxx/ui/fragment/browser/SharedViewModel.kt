package com.example.video_downloader_xxx.ui.fragment.browser

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_downloader_xxx.data.model.Social
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.data.repository.browser.SocialRepository
import com.example.video_downloader_xxx.data.repository.browser.VideoDownloadManager
import com.example.video_downloader_xxx.data.repository.web.DownloadVideosOnWebRepository
import com.example.video_downloader_xxx.util.DownloadState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class SharedViewModel(
    private val manager: VideoDownloadManager,
    private val repository: SocialRepository,
    private val repositoryDownload: DownloadVideosOnWebRepository
) : ViewModel() {

    private val _videoList = MutableStateFlow<List<VideoInfo>>(emptyList())
    val videoList: StateFlow<List<VideoInfo>> = _videoList.asStateFlow()

    private val _videoDetected = MutableStateFlow<VideoInfo?>(null)
    val videoDetected: StateFlow<VideoInfo?> = _videoDetected.asStateFlow()

    private val _downloadVideoState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadVideoState = _downloadVideoState.asStateFlow()

    private val _social = MutableStateFlow<List<Social>>(emptyList())
    val social: StateFlow<List<Social>> = _social.asStateFlow()

    private val _onFindVideoDone = MutableSharedFlow<Unit>()
    val onFindVideoDone = _onFindVideoDone.asSharedFlow()

    init {
        loadSocials()
    }

    fun addDetectedVideo(videoInfo: VideoInfo) {
        val currentList = _videoList.value.toMutableList()
        if (!currentList.any { it.videoUrl == videoInfo.videoUrl }) {
            currentList.add(videoInfo)
            _videoList.value = currentList

            viewModelScope.launch {
                _onFindVideoDone.emit(Unit)
            }
        }
    }

    fun clearDetectedVideos() {
        _videoList.value = emptyList()
    }

    private fun loadSocials() {
        _social.value = repository.getDefaultSocials()
    }

    fun toggleSelect(video: VideoInfo){
        _videoList.value = _videoList.value.map {
            if(it.videoUrl == video.videoUrl) {
                it.copy(isSelected = !it.isSelected)
            }else{
                it
            }
        }
    }

    fun getSelectedVideos(): List<VideoInfo> = _videoList.value.filter { it.isSelected }

    fun rename(video: VideoInfo, newName: String){
        _videoList.value = _videoList.value.map {
            if(it.videoUrl == video.videoUrl) {
                it.copy(title = newName)
            }else {
                it
            }
        }
    }

    fun fetchVideoInfo(url: String){
        viewModelScope.launch {
            try {
                _videoList.value = manager.getVideoInfo(url).map { it.copy(isSelected = true) }
                _videoDetected.value = repositoryDownload.getVideoInfo(url)
                Log.i("SharedViewModel", "fetchVideoInfo: ${_videoDetected.value} ")
                if (_videoList.value.isEmpty()) {
                    _downloadVideoState.value = DownloadState.Error("Không thể phân tích video.")
                    return@launch
                }
                _onFindVideoDone.emit(Unit)
            } catch (e: Exception) {
                Log.e("DownloadViewModel", "Error fetching video info: ${e.message}", e)
                _downloadVideoState.value =
                    DownloadState.Error("Error fetching video info: ${e.message}")
            }
        }
    }

//    fun checkUrl(url: String) {
//        viewModelScope.launch {
//            val info = manager.getVideoInfo(url).map { it.copy(isSelected = true) }
//            _videoDetected.value = info
//            _onFindVideoDone.emit(Unit)
//        }
//    }

    fun downloadVideo(videoInfo: VideoInfo, outFile: File) {
        viewModelScope.launch {
            _downloadVideoState.value = DownloadState.Idle
            manager.downloadVideo(videoInfo, outFile).collect { progress ->
                Log.i("DownloadViewModel", "downloadVideo: ${videoInfo.sourceUrl} $progress")
                if (progress.percent in 0f..99f) {
                    _downloadVideoState.value = DownloadState.Downloading(progress.percent.toInt())
                } else if (progress.percent >= 100f) {
                    _downloadVideoState.value = DownloadState.Success(outFile)
                }
            }
        }
    }

//    fun start(url: String, outFile: File) {
//        viewModelScope.launch {
//            _downloadVideoState.value = DownloadState.Idle
//
//            fetchVideoInfo(url)
////            val info = manager.getVideoInfo(url)
////            if(info == null){
////                _downloadVideoState.value = DownloadState.Error("Không thể phân tích video.")
////                return@launch
////            }
////            Log.i("Info_ttdat", "start: ${info.sourceUrl} ${info.videoUrl}")
//
//            //val info = videoList.value.get()
////            if(info != null){
////                downloadVideo(info, outFile)
////            }
//
////            manager.downloadVideo(info, outFile)
////                .collect { progress ->
////                    if (progress.percent in 0f..99f) {
////                        _downloadVideoState.value =
////                            DownloadState.Downloading(progress.percent.toInt())
////                    } else if (progress.percent >= 100f) {
////                        _downloadVideoState.value = DownloadState.Success(outFile)
////                    }
////                }
//        }
//    }
}