package com.example.video_downloader_xxx.ui.fragment.browser.home

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
//        emit(DownloadStatus.DOWNLOADING(0))
//    }
//}