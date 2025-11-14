//package com.example.video_downloader_xxx.service
//
//import com.example.video_downloader_xxx.data.model.VideoInfo
//import com.example.video_downloader_xxx.util.DownloadState
//import com.example.video_downloader_xxx.util.DownloadStatus
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import java.io.File
//import java.io.FileOutputStream
//
//class DownloadTask(
//    private val info: VideoInfo,
//    private val client: OkHttpClient,
//    private val onProgress: (VideoInfo) -> Unit,
//    private val onComplete: (VideoInfo) -> Unit,
//    private val onError: (VideoInfo, String) -> Unit
//) {
//
//    @Volatile private var isPaused = false
//    @Volatile private var isCancelled = false
//
//    private var downloadedBytes = 0L
//    private var totalBytes = 0L
//
//    fun pause() {
//        isPaused = true
//    }
//
//    fun cancel() {
//        isCancelled = true
//        isPaused = true
//    }
//
//    suspend fun start(outputFile: File) = withContext(Dispatchers.IO) {
//
//    try {
//            while (!isCancelled) {
//
//                downloadedBytes = if (outputFile.exists()) outputFile.length() else 0
//
//                val request = Request.Builder()
//                    .url(info.videoUrl!!)
//                    .apply {
//                        if (downloadedBytes > 0) {
//                            addHeader("Range", "bytes=$downloadedBytes-")
//                        }
//                    }
//                    .build()
//
//                val response = client.newCall(request).execute()
//
//                if (!response.isSuccessful) {
//                    onError(info, "HTTP error ${response.code}")
//                    return@withContext
//                }
//
//                if (totalBytes == 0L) {
//                    val newBytes = response.body.contentLength()
//
//                    totalBytes = downloadedBytes + newBytes
//                }
//
//                val input = response.body.byteStream()
//                val output = FileOutputStream(outputFile, true)
//
//                val buffer = ByteArray(64 * 1024)
//                var read: Int
//
//                while (input.read(buffer).also { read = it } != -1) {
//
//                    if (isPaused || isCancelled) {
//                        output.close()
//                        input.close()
//                        return@withContext
//                    }
//
//                    val percent = ((downloadedBytes * 100f) / totalBytes)
//
//                    val updatedVideo = info.copy(
//                        progress = percent,
//                        downloadStatus = DownloadStatus.DOWNLOADING
//                    )
//                    onProgress(updatedVideo)
//                }
//
//                output.close()
//                input.close()
//
//                val completedVideo = info.copy(
//                    progress = 100f,
//                    localPath = outputFile.absolutePath,
//                    downloadStatus = DownloadStatus.COMPLETED
//                )
//
//                onComplete(completedVideo)
//
//                return@withContext
//            }
//
//        } catch (e: Exception) {
//            onError(info, e.message ?: "Unknown error")
//        }
//    }
//}