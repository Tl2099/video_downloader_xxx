package com.example.video_downloader_xxx.ui.fragment.browser.home

//class VideoDownloadService : Service() {
//    private val binder = DownloadBinder()
//    private val downloads = mutableMapOf<String, DownloadTask>()
//
//
//    inner class DownloadBinder : Binder() {
//        fun getService(): VideoDownloadService = this@VideoDownloadService
//    }
//
//    override fun onBind(p0: Intent?): IBinder? = binder
//
//    fun startDownload(videoInfo: VideoInfo){
//        val task = DownloadTask(videoInfo) { progress ->
//            updateProgress(videoInfo.id, progress)
//        }
//        downloads[videoInfo.id] = task
//        task.start()
//    }
//
//    private fun updateProgress(videoId: String, progress: Int){
//        val intent = Intent("DOWNLOAD_PROGRESS")
//        intent.putExtra("video_id", videoId)
//        intent.putExtra("progress", progress)
//        sendBroadcast(intent)
//    }
//}
//
//class DownloadTask(
//    private val videoInfo: VideoInfo,
//    private val onProgress: (Int) -> Unit
//) {
//    fun start() {
//        Thread {
//            try {
//                downloadFile(videoInfo.sourceUrl)
//            }catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }.start()
//    }
//
//    private fun downloadFile(url: String) {
//        val cline = OkHttpClient()
//        val request = Request.Builder().url(url).build()
//
//        cline.newCall(request).execute().use { response ->
//            val body = response.body ?: return
//            val contentLength = body.contentLength()
//            val inputStream = body.byteStream()
//
//            val file = File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//                "${videoInfo.title}.mp4"
//            )
//
//            val outputStream = FileOutputStream(file)
//            val buffer = ByteArray(8192)
//            var downloaded = 0L
//            var bytesRead: Int
//
//            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
//                outputStream.write(buffer, 0, bytesRead)
//                downloaded += bytesRead
//
//                val progress = ((downloaded * 100) / contentLength).toInt()
//                onProgress(progress)
//
//            }
//
//            outputStream.close()
//            inputStream.close()
//        }
//    }
//}