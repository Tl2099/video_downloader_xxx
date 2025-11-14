//package com.example.video_downloader_xxx.service
//
//import com.example.video_downloader_xxx.data.model.VideoInfo
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import okhttp3.OkHttpClient
//import java.io.File
//
//object DownloaderManager {
//    private val client = OkHttpClient()
//    private val tasks = mutableMapOf<String, DownloadTask>()
//
//    fun start(
//        video: VideoInfo,
//        outputFile: File,
//        onProgress: (VideoInfo) -> Unit,
//        onCompleted: (VideoInfo) -> Unit,
//        onError: (VideoInfo, String) -> Unit
//    ) {
//        val task = DownloadTask(video, client, onProgress, onCompleted, onError)
//        tasks[video.id] = task
//
//        CoroutineScope(Dispatchers.IO).launch {
//            task.start(outputFile)
//        }
//    }
//
//    fun pause(videoId: String) {
//        tasks[videoId]?.pause()
//    }
//
//    fun cancel(videoId: String) {
//        tasks[videoId]?.cancel()
//        tasks.remove(videoId)
//    }
//}
