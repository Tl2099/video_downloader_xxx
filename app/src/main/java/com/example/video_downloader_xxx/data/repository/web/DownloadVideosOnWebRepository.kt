package com.example.video_downloader_xxx.data.repository.web

import com.example.video_downloader_xxx.data.model.VideoInfo

interface DownloadVideosOnWebRepository {
    suspend fun getVideoInfo(url: String): VideoInfo?
}