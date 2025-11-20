package com.example.video_downloader_xxx.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.video_downloader_xxx.data.DataExt
import com.example.video_downloader_xxx.data.local.reposities.video.VideoInfoRepository
import com.example.video_downloader_xxx.data.mapper.toEntity
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.data.repository.browser.OkHttpVideoDownloader
import com.example.video_downloader_xxx.data.repository.browser.VideoDownloadManager
import com.example.video_downloader_xxx.data.repository.library.DownloadRepository
import com.example.video_downloader_xxx.ui.fragment.library.complete.CompleteFragment
import com.example.video_downloader_xxx.util.DownloadStatus
import com.example.video_downloader_xxx.util.FileHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList
import java.util.Queue

class VideoDownloadService : Service() {
    private val repo: VideoInfoRepository by inject()
    private val binder = DownloadBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val jobs = mutableMapOf<String, Job>()
    private val queue = LinkedList<VideoInfo>()

    private val maxConcurrent = 3

    private val pendingDownloads = mutableListOf<VideoInfo>()
    private val downloadQueue: Queue<VideoInfo> = LinkedList()
    private var isDownloading = false
    private val downloadManager = VideoDownloadManager()
    private val downloader = OkHttpVideoDownloader()
    private var currentCall: Call? = null

    companion object {
        private const val TAG = "VideoDownloadService"
        private const val MAX_CONCURRENT_DOWNLOADS = 3

        const val EXTRA_ID = "extra_id"
        const val EXTRA_SOURCE_URL = "extra_source_url"
        const val EXTRA_VIDEO_URL = "extra_video_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_THUMB = "extra_thumb"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_FILE_SIZE = "extra_file_size"
    }

    inner class DownloadBinder : Binder() {
        fun getService(): VideoDownloadService = this@VideoDownloadService
    }

    override fun onBind(p0: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: Service created")
    }

    fun isAlreadyQueuedOrDownloading(videoId: String): Boolean {
        if (jobs.containsKey(videoId)) return true

        if (pendingDownloads.any { it.id == videoId }) return true

        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: $intent")
        Log.d(TAG, "onStartCommand: ${intent?.getStringExtra(EXTRA_VIDEO_URL)}")

        val sourceUrl = intent?.getStringExtra(EXTRA_SOURCE_URL) ?: return START_NOT_STICKY
        val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Video"
        val thumb = intent.getStringExtra(EXTRA_THUMB)
        val duration = intent.getStringExtra(EXTRA_DURATION)
        val fileSize = intent.getStringExtra(EXTRA_FILE_SIZE)
        val id = intent.getStringExtra(EXTRA_ID) ?: java.util.UUID.randomUUID().toString()

        Log.d(TAG, "Data Received: ${intent.getStringExtra(EXTRA_VIDEO_URL)}")

        val video = VideoInfo(
            id = id,
            sourceUrl = sourceUrl,
            videoUrl = videoUrl,
            title = title,
            thumbnailUrl = thumb,
            duration = duration,
            fileSize = fileSize,
            downloadStatus = DownloadStatus.PENDING
        )

        startDownload(video)

        return START_STICKY
    }

//    fun startDownload(video: VideoInfo) {
//        val jobKey = video.id
//
//        Log.w(TAG, "Start download: Called")
//
//        if (jobs.containsKey(jobKey)) {
//            Log.w(TAG, "Download already in progress for id: $jobKey")
//            return
//        }
//
//        Log.i(TAG, "=== Starting new download ===")
//        Log.i(TAG, "Title: ${video.title}")
//        Log.i(TAG, "ID: ${video.id}")
//        Log.i(TAG, "VideoURL: ${video.videoUrl}")
//        Log.i(TAG, "SourceURL: ${video.sourceUrl}")
//        Log.i(TAG, "Active jobs: ${jobs.size}")
//
//        if (MAX_CONCURRENT_DOWNLOADS > 0 && jobs.size >= MAX_CONCURRENT_DOWNLOADS) {
//            Log.w(
//                TAG,
//                "Max concurrent downloads reached (${jobs.size}/$MAX_CONCURRENT_DOWNLOADS). Adding to queue."
//            )
//            pendingDownloads.add(video)
//            DownloadRepository.addDownloading(video)
//            return
//        }
//
//        DownloadRepository.addDownloading(video)
//
//        val outputDir = FileHelper.createVideoFile(this, video.videoUrl ?: "")
//
////        val safeName = video.title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
////        val outputPath = File(outputDir, "${video.id.take(8)}_$safeName.mp4")
//        //val outputPath = File(outputDir, "${video.title}.mp4")
//
//        val job = serviceScope.launch {
//            Log.d(TAG, "Start download: ${video.title}")
//
//            try {
//                downloadManager.downloadVideo(
//                    //httpDownloader.download(
//                    video,
//                    outputDir
//                ).collect { progress ->
//                    Log.d(
//                        TAG,
//                        "Download progress [${video.title}]: ${progress.percent}% - ${progress.logLine}"
//                    )
//
//                    when {
//                        progress.percent < 0f -> {
//                            Log.e(TAG, "Download error [${video.title}]: ${progress.logLine}")
//                            DownloadRepository.markFailed(video.id)
//                        }
//
//                        progress.percent in 0f..99.9f -> {
//                            DownloadRepository.updateProgress(video.id, progress.percent)
//                        }
//
//                        progress.percent >= 100f -> {
//                            val localPath = findDownloadedFilePath(outputDir, video.title)
//                            Log.i(
//                                TAG,
//                                "Download completed [${video.title}]. Local path: $localPath"
//                            )
//                            //DownloadRepository.markCompleted(video.id, localPath)
//                            val completedVideo = video.copy(localPath = localPath)
//                            val entity = completedVideo.toEntity()
//
//                            serviceScope.launch {
//                                repo.insert(entity)
//                                Log.i(TAG, "=== Data Entity after download ===")
//                                Log.i(TAG, "Title: ${entity.title}")
//                                Log.i(TAG, "ID: ${entity.id}")
//                                Log.i(TAG, "VideoURL: ${entity.videoUrl}")
//                                Log.i(TAG, "SourceURL: ${entity.sourceUrl}")
//                                Log.i(TAG, "Active jobs: ${entity.fileSize}")
//                                Log.i(TAG, "localPath:${entity.filePath}")
//                                Log.i(TAG, "Saved to Room successfully!")
//                            }
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Download exception [${video.title}]: ${e.message}", e)
//                DownloadRepository.markFailed(video.id)
//            } finally {
//                jobs.remove(jobKey)
//                Log.d(TAG, "Job removed. Remaining jobs: ${jobs.size}")
//
//                nextJob()
//
//                if (jobs.isEmpty()) {
//                    Log.i(TAG, "All downloads completed, stopping service")
//                    stopSelf()
//                }
//            }
//        }
//
//        jobs[jobKey] = job
//
////        val task = DownloadTask(videoInfo) { progress ->
////            updateProgress(videoInfo.id, progress)
////        }
////        Log.i("VideoDownloadService_ttdat", "startDownload: ${videoInfo.sourceUrl}")
////        downloads[videoInfo.id] = task
////        task.start()
//    }

    fun startDownload(video: VideoInfo) {
        if (jobs.containsKey(video.id)) return

        if (jobs.size >= MAX_CONCURRENT_DOWNLOADS) {
            pendingDownloads.add(video)
            DownloadRepository.addDownloading(video)
            return
        }

        startJob(video)

//        val safeName = video.title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
//        val targetFile = File(outputFolder, "$safeName.mp4")
    }

    private fun startJob(video: VideoInfo) {
        DownloadRepository.addDownloading(video)
        val outputFolder = FileHelper.createVideoFile(this, video.videoUrl ?: "")

        val job = serviceScope.launch {
            try {
                downloader.downloadVideo(
                    url = video.videoUrl ?: video.sourceUrl,
                    destFile = outputFolder,
                    onProgress = { percent ->
                        DownloadRepository.updateProgress(video.id, percent.toFloat())
                    }
                )

                val completedVideo = video.copy(localPath = outputFolder.absolutePath, downloadedAt = System.currentTimeMillis())
                Log.i(TAG, "duration: ${completedVideo.duration}")
                repo.insert(completedVideo.toEntity())

                DownloadRepository.markCompleted(video.id, outputFolder.absolutePath)

            } catch (e: Exception) {
                Log.e(TAG, "Download error: ${e.message}", e)
                DownloadRepository.markFailed(video.id)
            } finally {
                jobs.remove(video.id)
                nextJob()
                if (jobs.isEmpty() && pendingDownloads.isEmpty()) stopSelf()
            }
        }

        jobs[video.id] = job

    }

    fun cancelDownload(videoId: String) {
        Log.i(TAG, "cancelDownload called for ID: $videoId")

        val video = DownloadRepository.downloadingVideos.value.find { it.id == videoId }

        val job = jobs[videoId]
        if (job != null) {
            job.cancel()
            jobs.remove(videoId)
            Log.i(TAG, "Cancelled active download. Remaining jobs: ${jobs.size}")

            video?.let {
                deletePartialFile(it.title)
            }
        }

        val removed = pendingDownloads.removeAll { it.id == videoId }
        if (removed) {
            Log.i(TAG, "Removed from pending queue")
        }

        DownloadRepository.removeDownloading(videoId)

        nextJob()

        if (jobs.isEmpty() && pendingDownloads.isEmpty()) {
            Log.i(TAG, "No more downloads, stopping service")
            stopSelf()
        }
    }

    private fun deletePartialFile(title: String?) {
        if (title.isNullOrBlank()) return

        try {
            val outputDir = FileHelper.getVideoFolder(this)
            val sanitized = title.replace(Regex("""[\\/:*?"<>|]"""), "_")

            outputDir.listFiles()?.forEach { file ->
                if (file.nameWithoutExtension == sanitized) {
                    val deleted = file.delete()
                    Log.d(TAG, "Delete partial file ${file.name}: $deleted")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting partial file: ${e.message}")
        }
    }

    private fun nextJob() {
        if (pendingDownloads.isNotEmpty() && jobs.size < MAX_CONCURRENT_DOWNLOADS) {
            val nextVideo = pendingDownloads.removeAt(0)
            startDownload(nextVideo)
        }
    }

    private fun updateProgress(videoId: String, progress: Int) {
        val intent = Intent("DOWNLOAD_PROGRESS")
        intent.putExtra("video_id", videoId)
        intent.putExtra("progress", progress)
        sendBroadcast(intent)
    }

    private fun findDownloadedFilePath(dir: File, title: String?): String? {
        if (title.isNullOrBlank()) return null
        val sanitized = title.replace(Regex("""[\\/:*?"<>|]"""), "_")
        return dir.listFiles()
            ?.firstOrDefault { it.nameWithoutExtension == sanitized }?.absolutePath
    }

    private fun <T> Array<T>.firstOrDefault(predicate: (T) -> Boolean): T? {
        for (e in this) if (predicate(e)) return e
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        currentCall?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}

class DownloadTask(
    private val videoInfo: VideoInfo,
    private val onProgress: (Int) -> Unit
) {
    fun start() {
        Thread {
            try {
                downloadFile(videoInfo.sourceUrl)
                Log.i("VideoDownloadService_ttdat", "start: ${videoInfo.sourceUrl}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun downloadFile(url: String) {
        val cline = OkHttpClient()
        val request = Request.Builder().url(url).build()

        cline.newCall(request).execute().use { response ->
            val body = response.body ?: return
            val contentLength = body.contentLength()
            val inputStream = body.byteStream()

            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                videoInfo.title
            )

            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(8192)
            var downloaded = 0L
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloaded += bytesRead

                val progress = ((downloaded * 100) / contentLength).toInt()
                onProgress(progress)

            }

            outputStream.close()
            inputStream.close()
        }
    }
}