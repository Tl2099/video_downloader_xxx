package com.example.video_downloader_xxx.data.repository.browser

import android.graphics.Bitmap
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
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

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

            Log.i(TAG, "getVideoInfo: ${response.out}")

            parseVideoInfo(url, response.out)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "getVideoInfo: " + e.message)
            null
        }
    }

    private suspend fun parseVideoInfo(sourceUrl: String, jsonString: String): VideoInfo? {
        return try {
            val json = JSONObject(jsonString)

            var durationText = json.optInt("duration", -1).takeIf { it > 0 }?.let { sec ->
                val min = sec / 60
                val s = sec % 60
                "%02d:%02d".format(min, s)
            }
            if (durationText == null) {
                val url = json.optString("url", null)
                if (!url.isNullOrEmpty()) {
                    durationText = fetchVideoDuration(url)
                }
            }

            val fileSizeText = getFileSizeOrFetch(json)

            var thumbnailUrl = json.optString("thumbnail", null)
            if (thumbnailUrl.isNullOrEmpty()) {
                val thumbsArray = json.optJSONArray("thumbnails")
                if (thumbsArray != null && thumbsArray.length() > 0) {
                    thumbnailUrl = thumbsArray.optJSONObject(thumbsArray.length() - 1)
                        ?.optString("url", null)
                }
            }
            if (thumbnailUrl.isNullOrEmpty()) {
                val videoUrl = json.optString("url", null)
                if (!videoUrl.isNullOrEmpty()) {
                    thumbnailUrl = fetchVideoThumbnail(videoUrl)
                }
            }

            VideoInfo(
                sourceUrl = sourceUrl,
                videoUrl = json.optString("url", null),
                title = json.optString("title"),
                thumbnailUrl = thumbnailUrl,
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

    private suspend fun fetchVideoThumbnail(videoUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(videoUrl, HashMap())
            val bitmap = retriever.getFrameAtTime(0)
            retriever.release()

            if (bitmap != null) {
                val file = File.createTempFile("thumb_", ".jpg")
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                out.close()
                return@withContext file.absolutePath
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "fetchVideoThumbnail failed: ${e.message}")
            null
        }
    }


    private suspend fun fetchVideoDuration(videoUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(videoUrl, HashMap())
            val durationMs = retriever.extractMetadata(
                android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: return@withContext null
            retriever.release()

            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            "%02d:%02d".format(minutes, seconds)
        } catch (e: Exception) {
            Log.w(TAG, "fetchVideoDuration failed: ${e.message}")
            null
        }
    }


    private suspend fun getFileSizeOrFetch(json: JSONObject): String? {
        val fileSizeBytes = json.optLong("filesize_approx", -1)
        if (fileSizeBytes > 0) {
            return String.format("%.2f MB", fileSizeBytes / 1024f / 1024f)
        }

        val url = json.optString("url", null) ?: return null
        return fetchFileSizeFromUrl(url)?.let {
            String.format("%.2f MB", it / 1024f / 1024f)
        }
    }

    private suspend fun fetchFileSizeFromUrl(url: String): Long? = withContext(Dispatchers.IO) {
        try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connect()
            val size = connection.contentLengthLong
            connection.disconnect()
            if (size > 0) size else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

data class DownloadProgress(
    val percent: Float,
    val etaSeconds: Long,
    val logLine: String? = null
)