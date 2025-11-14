package com.example.video_downloader_xxx.data.repository.library

import android.util.Log
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.util.DownloadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DownloadRepository {
    private const val TAG = "DownloadRepository"

    private val _downloadingVideos = MutableStateFlow<List<VideoInfo>>(emptyList())
    val downloadingVideos: StateFlow<List<VideoInfo>> = _downloadingVideos.asStateFlow()

    private val _completedVideos = MutableStateFlow<List<VideoInfo>>(emptyList())
    val completedVideos: StateFlow<List<VideoInfo>> = _completedVideos.asStateFlow()

    /**
     * Thêm video vào danh sách đang download
     * Sử dụng video.id làm key unique
     */
    fun addDownloading(video: VideoInfo) {
        val current = _downloadingVideos.value.toMutableList()
        Log.d(TAG, "addDownloading - Current list size: ${current.size}")
        Log.d(TAG, "addDownloading - Video: ${video.title}, ID: ${video.id}")

        // Check bằng ID thay vì videoUrl vì ID là unique
        if (current.none { it.id == video.id }) {
            current.add(video.copy(downloadStatus = DownloadStatus.DOWNLOADING, progress = 0f))
            _downloadingVideos.value = current
            Log.d(TAG, "addDownloading - Added successfully. New size: ${current.size}")
        } else {
            Log.w(TAG, "addDownloading - Video already exists: ${video.title} (ID: ${video.id})")
        }
    }

    /**
     * Cập nhật progress của video theo ID
     * @param videoId: video.id (UUID unique)
     */
    fun updateProgress(videoId: String, percent: Float) {
        Log.d(TAG, "updateProgress - ID: $videoId, Progress: $percent%")

        _downloadingVideos.value = _downloadingVideos.value.map { v ->
            if (v.id == videoId) {
                v.copy(progress = percent)
            } else v
        }
    }

    /**
     * Đánh dấu video đã hoàn thành
     * @param videoId: video.id (UUID unique)
     */
    fun markCompleted(videoId: String, localPath: String?) {
        Log.d(TAG, "markCompleted - ID: $videoId, Path: $localPath")

        val currentDownloading = _downloadingVideos.value.toMutableList()
        val video = currentDownloading.find { it.id == videoId }

        if (video != null) {
            currentDownloading.remove(video)
            _downloadingVideos.value = currentDownloading
            Log.d(TAG, "markCompleted - Removed from downloading. Remaining: ${currentDownloading.size}")

            val completedList = _completedVideos.value.toMutableList()
            completedList.add(
                video.copy(
                    downloadStatus = DownloadStatus.COMPLETED,
                    progress = 100f,
                    localPath = localPath
                )
            )
            _completedVideos.value = completedList
            Log.d(TAG, "markCompleted - Added to completed. Total completed: ${completedList.size}")
        } else {
            Log.w(TAG, "markCompleted - Video not found in downloading list: $videoId")
        }
    }

    /**
     * Đánh dấu video download lỗi
     * @param videoId: video.id (UUID unique)
     */
    fun markFailed(videoId: String) {
        Log.e(TAG, "markFailed - ID: $videoId")

        _downloadingVideos.value = _downloadingVideos.value.map { v ->
            if (v.id == videoId) {
                v.copy(downloadStatus = DownloadStatus.FAILED)
            } else v
        }
    }

    /**
     * Xóa video khỏi danh sách downloading
     */
    fun removeDownloading(videoId: String) {
        val current = _downloadingVideos.value.toMutableList()
        current.removeAll { it.id == videoId }
        _downloadingVideos.value = current
        Log.d(TAG, "removeDownloading - Removed ID: $videoId. Remaining: ${current.size}")
    }

    /**
     * Xóa video khỏi danh sách completed
     */
    fun removeCompleted(videoId: String) {
        val current = _completedVideos.value.toMutableList()
        current.removeAll { it.id == videoId }
        _completedVideos.value = current
        Log.d(TAG, "removeCompleted - Removed ID: $videoId. Remaining: ${current.size}")
    }

    /**
     * Clear tất cả downloads (dùng cho testing)
     */
    fun clearAll() {
        _downloadingVideos.value = emptyList()
        _completedVideos.value = emptyList()
        Log.d(TAG, "clearAll - All lists cleared")
    }
}