//package com.example.video_downloader_xxx.ui.fragment.browser.home
//
//import android.content.Context
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import com.example.video_downloader_xxx.util.DownloadStatus
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.withContext
//import java.io.File
//
//class VideoDownloadWorker(
//    context: Context,
//    params: WorkerParameters
//) : CoroutineWorker(context, params) {
//
//    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
//        try {
//            val url = inputData.getString("url") ?: return@withContext Result.failure()
//            val title = inputData.getString("title") ?: "video"
//
//            downloadFile(url, title)
//            Result.success()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Result.failure()
//        }
//    }
//
//    private suspend fun downloadFile(url: String, outputFile: File): Flow<DownloadStatus> = flow {
//        emit(DownloadStatus.PENDING)
//    }
//}