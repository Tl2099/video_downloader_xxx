package com.example.video_downloader_xxx.data.repository

import android.util.Log
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.util.DownloadStatus
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class VideoDownloadManager {

    companion object {
        private const val TAG = "VideoDownloadManager"
    }

    suspend fun getVideoInfo(url: String): VideoInfo? = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(url).apply {
                addOption("--dump-json")
                addOption("--no-playlist")
            }

            Log.i(TAG, "getVideoInfo: $request")

            val response: YoutubeDLResponse =
                YoutubeDL.getInstance().execute(request, "fetch_info", null)

            parseVideoInfo(url, response.out)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "getVideoInfo: " + e.message)
            null
        }
    }

    private fun parseVideoInfo(sourceUrl: String, jsonString: String): VideoInfo? {
        return try {
            val json = JSONObject(jsonString)
            val durationSec = json.optInt("duration", -1)
            val durationText = if (durationSec > 0) {
                val minutes = durationSec / 60
                val seconds = durationSec % 60
                "%02d:%02d".format(minutes, seconds)
            } else null

            val fileSizeBytes = json.optLong("filesize_approx", -1)
            val fileSizeText = if (fileSizeBytes > 0)
                String.format("%.2f MB", fileSizeBytes / 1024f / 1024f)
            else null

            VideoInfo(
                sourceUrl = sourceUrl,
                videoUrl = json.optString("url", null),
                title = json.optString("title"),
                thumbnailUrl = json.optString("thumbnail", null),
                duration = durationText,
                fileSize = fileSizeText,
                downloadStatus = DownloadStatus.PENDING
            )
        } catch (e: Exception) {
            Log.e(TAG, "parseVideoInfo failed: ${e.message}")
            null
        }
    }

    fun downloadVideo(
        video: VideoInfo,
        outputPath: File,
        formatId: String? = null,
    ): Flow<DownloadProgress> = channelFlow {

        val request = YoutubeDLRequest(video.sourceUrl).apply {
            addOption("--extractor-args", "generic:impersonate=chrome101")
            addOption("-o", "${outputPath.absolutePath}/%(title)s.%(ext)s")
            addOption("-f", formatId ?: "best")
            addOption("--add-metadata")
            addOption("--embed-thumbnail")
        }

        try {
            YoutubeDL.getInstance().execute(request, "download") { progress, eta, line ->
                trySend(DownloadProgress(progress, eta, line))
            }

            trySend(DownloadProgress(100f, 0, "Completed"))
        } catch (e: Exception) {
            trySend(DownloadProgress(-1f, 0, "Error: ${e.message}"))
        }

        awaitClose { }

    }.flowOn(Dispatchers.IO)
}

data class DownloadProgress(
    val percent: Float,
    val etaSeconds: Long,
    val logLine: String? = null
)