package com.example.video_downloader_xxx.data.repository.browser

import android.annotation.SuppressLint
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
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class VideoDownloadManager {

    companion object {
        private const val TAG = "VideoDownloadManager"

    }

    suspend fun getVideoInfo(url: String): List<VideoInfo> = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(url).apply {
                addOption("--dump-json")
                addOption("--no-playlist")
            }

            Log.i(TAG, "getVideoInfo: $request")

            val response: YoutubeDLResponse =
                YoutubeDL.getInstance().execute(request, "fetch_info", null)

            Log.i(TAG, "getVideoInfo: ${response.out}")

            parseVideoInfoList(url, response.out)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "getVideoInfo: " + e.message)
            emptyList()
        }
    }

    private suspend fun parseVideoInfoList(sourceUrl: String, jsonString: String): List<VideoInfo> {
        val videoList = mutableListOf<VideoInfo>()

        try {
            val lines = jsonString.trim().split("\n")

            for (line in lines) {
                if (line.isBlank()) continue
                val json = JSONObject(line)

                var durationText = formatDuration(json.optInt("duration", -1))

                if (durationText == "00:00:00") {
                    val url = json.optString("url", null)
                    if (!url.isNullOrEmpty()) {
                        durationText = fetchVideoDuration(url)
                    }
                }

                val fileSizeText = getFileSizeOrFetch(json) ?: "Unknown"

                var thumbnailUrl = json.optString("thumbnail", null)

                Log.i(TAG, "parseVideoInfoList: thumbnailUrl $thumbnailUrl")
//                if (thumbnailUrl.isNullOrEmpty()) {
//                    val thumbsArray = json.optJSONArray("thumbnails")
//                    if (thumbsArray != null && thumbsArray.length() > 0) {
//                        thumbnailUrl = thumbsArray.optJSONObject(thumbsArray.length() - 1)
//                            ?.optString("url", null)
//                    }
//                }
                if (thumbnailUrl.isNullOrEmpty()) {
                    val videoUrl = json.optString("url", null)
                    if (!videoUrl.isNullOrEmpty()) {
                        thumbnailUrl = fetchVideoThumbnail(videoUrl)
                    }
                }

                videoList.add(
                    VideoInfo(
                        sourceUrl = sourceUrl,
                        videoUrl = json.optString("url", null),
                        title = json.optString("title"),
                        thumbnailUrl = thumbnailUrl,
                        duration = durationText,
                        fileSize = fileSizeText,
                        downloadStatus = DownloadStatus.PENDING
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseVideoInfoList failed: ${e.message}")
        }

        return videoList
    }


    @SuppressLint("DefaultLocale")
    private fun formatDuration(seconds: Int): String {
        if (seconds <= 0) return "00:00:00"

        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60

        return String.format("%02d:%02d:%02d", h, m, s)
    }

    private fun ensureUniqueName(dir: File, baseName: String): String {
        var name = baseName
        var idx = 1
        while (dir.listFiles()?.any { it.nameWithoutExtension == name } == true) {
            name = "$baseName($idx)"
            idx++
        }
        return name
    }

    private suspend fun fetchVideoThumbnail(videoUrl: String): String? =
        withContext(Dispatchers.IO) {
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


    private suspend fun fetchVideoDuration(videoUrl: String): String =
        withContext(Dispatchers.IO) {
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(videoUrl, HashMap())
                val durationMs = retriever
                    .extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()

                retriever.release()

                if (durationMs != null && durationMs > 0) {
                    val totalSeconds = (durationMs / 1000).toInt()
                    return@withContext formatDuration(totalSeconds)
                }

                "00:00:00"
            } catch (e: Exception) {
                Log.w(TAG, "fetchVideoDuration failed: ${e.message}")
                "00:00:00"
            }
        }

    private suspend fun getFileSizeOrFetch(json: JSONObject): String? {

        getSizeFromFormats(json)?.let { size ->
            return formatSize(size)
        }

        val approx = json.optLong("filesize_approx", -1)
        if (approx > 0) {
            return formatSize(approx)
        }

        val url = json.optString("url", null) ?: return null
        if (url.endsWith(".m3u8")) {
            val hlsSize = estimateHlsSize(url)
            if (hlsSize != null && hlsSize > 0) {
                return formatSize(hlsSize)
            }
        }

        val headSize = fetchFileSizeFromUrl(url)
        if (headSize != null && headSize > 0) {
            return formatSize(headSize)
        }

        return "Unknown"
    }


    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 MB"

        val mb = bytes / 1024f / 1024f

        return if (mb < 1024) {
            String.format("%.2f MB", mb)
        } else {
            val gb = mb / 1024f
            String.format("%.2f GB", gb)
        }
    }


    private fun getSizeFromFormats(json: JSONObject): Long? {
        val formats = json.optJSONArray("formats") ?: return null

        var bestSize: Long? = null

        for (i in 0 until formats.length()) {
            val f = formats.optJSONObject(i) ?: continue
            val size = f.optLong("filesize", -1).takeIf { it > 0 }
                ?: f.optLong("filesize_approx", -1).takeIf { it > 0 }

            if (size != null) {
                bestSize = size
                break
            }
        }

        return bestSize
    }

    private suspend fun estimateHlsSize(m3u8Url: String): Long? = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL(m3u8Url)
            val base = m3u8Url.substringBeforeLast("/")

            val text = url.readText()
            val lines = text.lines()

            val segments = mutableListOf<String>()

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                if (trimmed.startsWith("#")) continue

                segments.add(trimmed)
            }

            if (segments.isEmpty()) {
                Log.w(TAG, "estimateHlsSize: no segments found in $m3u8Url")
                return@withContext null
            }

            var total: Long = 0

            for (seg in segments) {
                val segUrl = if (seg.startsWith("http")) seg else "$base/$seg"
                val size = fetchFileSizeFromUrl(segUrl) ?: continue
                total += size
            }

            total
        } catch (e: Exception) {
            Log.e(TAG, "estimateHlsSize error: ${e.message}")
            null
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

    fun downloadVideo(
        video: VideoInfo,
        outputPath: File,
        formatId: String? = null,
    ): Flow<DownloadProgress> = channelFlow {

        val baseTitle = (video.title.trim().takeUnless { it.isEmpty() } ?: "video")
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
        val uniqueTitle = ensureUniqueName(outputPath, baseTitle)

        val request = YoutubeDLRequest(video.sourceUrl).apply {
            addOption("--extractor-args", "generic:impersonate=chrome101")
            addOption("-o", File(outputPath, "$uniqueTitle.%(ext)s").absolutePath)
            addOption("-f", formatId ?: "best")
            addOption("--add-metadata")
            addOption("--embed-thumbnail")
            addOption("--continue")
            addOption("--no-part")
            addOption("--newline")
            addOption("--no-playlist")
        }

        try {
            YoutubeDL.getInstance().execute(request, "download") { progress, eta, line ->
                trySend(DownloadProgress(progress, eta, line))
            }

            trySend(DownloadProgress(100f, 0, "Completed"))
        } catch (e: Exception) {
            trySend(DownloadProgress(-1f, 0, "Error: ${e.message}"))
        }

    }.flowOn(Dispatchers.IO)

}

data class DownloadProgress(
    val percent: Float,
    val etaSeconds: Long,
    val logLine: String? = null
)