package com.example.video_downloader_xxx.data.repository

import com.example.video_downloader_xxx.data.model.VideoInfo
import java.io.File

class VideoRepositoryImpl(
    private val youtubeDl: VideoDownloadManager = VideoDownloadManager(),
    private val httpRepo: VideoRepository = VideoRepository()
) {
    suspend fun fetchVideoInfo(url: String) = youtubeDl.getVideoInfo(url)

    fun downloadViaYoutubeDL(video: VideoInfo, output: File) =
        youtubeDl.downloadVideo(video, output)

    fun downloadDirect(url: String, output: File) =
        httpRepo.downloadVideo(url, output)
}