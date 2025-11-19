package com.example.video_downloader_xxx.data.repository.web

import android.annotation.SuppressLint
import android.util.Log
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.util.DownloadStatus
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject


class DownloadVideosOnWebRepositoryImpl : DownloadVideosOnWebRepository {

    companion object {
        private const val TAG = "DownloadVideosOnWebRepositoryImpl"
    }

    override suspend fun getVideoInfo(url: String): VideoInfo? = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(url).apply {
                addOption("--no-warnings")
                addOption("--dump-json")
                addOption("--no-playlist")
                addOption("--no-download")
                addOption("--format", "bestvideo+bestaudio/best")
            }

            Log.i("BrowserWebViewClient", "getVideoInfo: Executing yt-dlp request for $url")
            val processId = "fetch_info_${System.currentTimeMillis()}"
            val response: YoutubeDLResponse =
                YoutubeDL.getInstance().execute(request, processId, null)

            if (response.out.isEmpty()) {
                Log.e("BrowserWebViewClient", "No video information returned for URL: $url")
                return@withContext null
            }

            Log.i("DownloadVideosOnWebRepositoryImpl", "getVideoInfo: ${response.out}")

            return@withContext parseVideoInfo(url, response.out)

        } catch (e: Exception) {
            Log.e("BrowserWebViewClient", "yt-dlp error: ${e.message}", e)
            null
        }
    }

    // ===================== PARSE JSON ========================

    private suspend fun parseVideoInfo(sourceUrl: String, jsonString: String): VideoInfo? {
        return try {

            val json = JSONObject(jsonString)

            // ---------------- TITLE ----------------
            val title = json.optString("title", "Unknown")

            // ---------------- DURATION (HH:mm:ss) ----------------
            var durationText = formatDuration(json.optInt("duration", -1))

            if (durationText == "00:00:00") {
                val url = json.optString("url", null)
                if (!url.isNullOrEmpty()) {
                    durationText = fetchVideoDuration(url)
                }
            }

            // ---------------- THUMBNAIL ----------------
            var thumb = json.optString("thumbnail", null)

            // Fallback lấy từ thumbnails array
            if (thumb.isNullOrEmpty()) {
                val thumbs = json.optJSONArray("thumbnails")
                if (thumbs != null && thumbs.length() > 0) {
                    thumb = thumbs.optJSONObject(thumbs.length() - 1)?.optString("url", null)
                }
            }

            // ---------------- FILE SIZE ----------------
            val sizeText = getFileSizeOrFetch(json) ?: "Unknown"

            // ---------------- VIDEO URL ----------------
            val videoUrl = json.optString("url", null)

            VideoInfo(
                sourceUrl = sourceUrl,
                videoUrl = videoUrl,
                title = title,
                thumbnailUrl = thumb,
                duration = durationText,
                fileSize = sizeText,
                downloadStatus = DownloadStatus.PENDING
            )

        } catch (e: Exception) {
            Log.e("BrowserWebViewClient", "parseVideoInfo failed: ${e.message}")
            null
        }
    }

    private fun getFileSizeOrFetch(json: JSONObject): String {

        getSizeFromFormats(json)?.let { size ->
            return formatSize(size)
        }

        val approx = json.optLong("filesize_approx", -1)
        if (approx > 0) {
            return formatSize(approx)
        }

        val url = json.optString("url", null) ?: return "Unknown"

        if (url.endsWith(".m3u8")) {
            return "Streaming"
        }

        if (url.endsWith(".mp4")) {
            return runBlockingFileSize(url)
        }

        return "Unknown"
    }

    private fun runBlockingFileSize(url: String): String {
        return try {
            val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connect()
            val size = conn.contentLengthLong
            conn.disconnect()

            if (size > 0) formatSize(size) else "Unknown"

        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getSizeFromFormats(json: JSONObject): Long? {
        val formats = json.optJSONArray("formats") ?: return null

        for (i in 0 until formats.length()) {
            val f = formats.optJSONObject(i) ?: continue
            val size = f.optLong("filesize", -1).takeIf { it > 0 }
                ?: f.optLong("filesize_approx", -1).takeIf { it > 0 }

            if (size != null) return size
        }
        return null
    }



    // ===================== FORMAT DURATION ========================

    @SuppressLint("DefaultLocale")
    private fun formatDuration(seconds: Int): String {
        if (seconds <= 0) return "00:00:00"

        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60

        return String.format("%02d:%02d:%02d", h, m, s)
    }

    // ===================== FORMAT SIZE (MB / GB) ========================

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "Unknown"

        val mb = bytes / 1024f / 1024f

        return if (mb < 1024) {
            String.format("%.2f MB", mb)
        } else {
            val gb = mb / 1024f
            String.format("%.2f GB", gb)
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
}
