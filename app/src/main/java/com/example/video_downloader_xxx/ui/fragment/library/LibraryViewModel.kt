package com.example.video_downloader_xxx.ui.fragment.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.video_downloader_xxx.data.local.reposities.video.VideoInfoRepository
import com.example.video_downloader_xxx.data.mapper.toEntity
import com.example.video_downloader_xxx.data.mapper.toVideoInfo
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.data.repository.library.DownloadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(private val repo: VideoInfoRepository): ViewModel() {
    val downloadingVideos: StateFlow<List<VideoInfo>> = DownloadRepository.downloadingVideos

    //val completedVideos: StateFlow<List<VideoInfo>> = DownloadRepository.completedVideos

    val videos = repo.observeAll()
        .map { list -> list.map { it.toVideoInfo() }}
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun rename(video: VideoInfo, newName: String) = viewModelScope.launch {
        repo.updateName(video.toEntity().id, newName)
    }

    fun saveDownloaded(video: VideoInfo){
        viewModelScope.launch {
            repo.insert(video.toEntity())
        }
    }

    fun delete(video: VideoInfo){
        viewModelScope.launch {
            repo.delete(video.toEntity())
        }
    }

    fun deleteAll(){
        viewModelScope.launch {
            repo.deleteAll()
        }
    }
}