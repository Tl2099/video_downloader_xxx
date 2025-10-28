package com.example.video_downloader_xxx.domain.usecase

import com.example.video_downloader_xxx.data.repository.VideoRepository
import com.example.video_downloader_xxx.util.DownloadState
import kotlinx.coroutines.flow.Flow
import java.io.File

class DownloadVideoUseCase(private val repo: VideoRepository) {
    suspend operator fun invoke(url: String, outputFile: File): Flow<DownloadState> =
        repo.downloadVideo(url, outputFile)
}