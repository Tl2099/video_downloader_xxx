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
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File

class SharedViewModel(
    private val manager: VideoDownloadManager,
    private val repository: SocialRepository,
    private val repositoryDownload: DownloadVideosOnWebRepository
) : ViewModel() {

    private val _videoWebList = MutableStateFlow<List<VideoInfo>>(emptyList())
    val videoWebList: StateFlow<List<VideoInfo>> = _videoWebList.asStateFlow()

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

    private val _lastAnalyzedUrl = MutableStateFlow<String?>(null)
    val lastAnalyzedUrl: StateFlow<String?> get() = _lastAnalyzedUrl.asStateFlow()

    private var fetchJob: Job? = null

    init {
        loadSocials()
    }

    fun setLastAnalyzedUrl(url: String) {
        _lastAnalyzedUrl.value = url
    }

    fun getLastAnalyzedUrl(): String? {
        return _lastAnalyzedUrl.value
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

    fun clearDetectedWebVideos() {
        _videoWebList.value = emptyList()
    }

    private fun loadSocials() {
        _social.value = repository.getDefaultSocials()
    }

    fun toggleSelect(video: VideoInfo) {
        _videoList.value = _videoList.value.map {
            if (it.videoUrl == video.videoUrl) {
                it.copy(isSelected = !it.isSelected)
            } else {
                it
            }
        }
    }

    fun toggleVideoSelect(video: VideoInfo) {
        Log.d("DEBUG", "toggleSelect called for: ${video.id} | ${video.videoUrl}")
        _videoWebList.value = _videoWebList.value.map {
            Log.d("DEBUG", "Checking: ${it.id} | ${it.videoUrl} | selected=${it.isSelected}")
            if (it.videoUrl == video.videoUrl) {
                Log.d("DEBUG", "MATCH found - toggling ${it.id}")
                it.copy(isSelected = !it.isSelected)
            } else {
                it
            }
        }
        Log.d("DEBUG", "Final list size: ${_videoList.value.size}")
    }

    fun getSelectedVideos(): List<VideoInfo> = _videoList.value.filter { it.isSelected }

    fun getSelectedWebVideos(): List<VideoInfo> = _videoWebList.value.filter { it.isSelected }

    fun rename(video: VideoInfo, newName: String) {
        _videoList.value = _videoList.value.map {
            if (it.videoUrl == video.videoUrl) {
                it.copy(title = newName)
            } else {
                it
            }
        }
    }

    fun renameVideoWeb(video: VideoInfo, newName: String) {
        _videoWebList.value = _videoWebList.value.map {
            if (it.videoUrl == video.videoUrl) {
                it.copy(title = newName)
            } else {
                it
            }
        }
    }

    fun fetchVideoInfo(url: String) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                withTimeout(100000L) {
                    _videoList.update {
                        manager.getVideoInfo(url).map { it.copy(isSelected = true) }
                    }

                    //_videoDetected.value = repositoryDownload.getVideoInfo(url)
                    Log.i("SharedViewModel", "fetchVideoInfo: ${_videoDetected.value} ")
                    if (_videoList.value.isEmpty()) {
                        _downloadVideoState.value =
                            DownloadState.Error("Không thể phân tích video.")
                        return@withTimeout
                    }
                    _onFindVideoDone.emit(Unit)
                }
            } catch (ex: TimeoutCancellationException) {
                _downloadVideoState.emit(
                    DownloadState.Error("Analyze timeout, please retry: ${ex.message}")
                )
            } catch (e: Exception) {
                Log.e("DownloadViewModel", "Error fetching video info: ${e.message}", e)
                _downloadVideoState.value =
                    DownloadState.Error("Error fetching video info: ${e.message}")
            } finally {
                fetchJob = null
            }
        }
    }

    fun cancelFetch() {
        fetchJob?.cancel()
        fetchJob = null
    }

    fun addVideo(url: String) {
        viewModelScope.launch {
            Log.i("TTDAT_SHAREVM", "url : $url")
            val video = repositoryDownload.getVideoInfo(url)
            Log.i("TTDAT_SHAREVM", "addVideo: ${video?.videoUrl}")
            if (video != null) {
                _videoWebList.update { currentList ->
                    if (currentList.any { it.videoUrl == video.videoUrl }) {
                        Log.i("TTDAT_SHAREVM", "Video trùng URL, bỏ qua!")
                        currentList
                    } else {
                        currentList + video
                    }
                }
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