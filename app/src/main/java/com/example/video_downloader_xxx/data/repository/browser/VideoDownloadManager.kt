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

    suspend fun getVideoInfo(url: String): List<VideoInfo> = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(url).apply {
                addOption("--dump-json")
                addOption("--no-playlist")
                addOption("--print-json")
            }

            Log.i(TAG, "getVideoInfo: $request")

            val response: YoutubeDLResponse =
                YoutubeDL.getInstance().execute(request, "fetch_info", null)

            Log.i(TAG, "getVideoInfo: ${response.out}")
            logFull("VideoDownload", response.out)

            parseVideoInfoList(url, response.out)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "getVideoInfo: " + e.message)
            emptyList()
        }
    }

    fun logFull(tag: String, message: String) {
        if (message.length <= 4000) {
            Log.i(tag, message)
            return
        }

        var start = 0
        val maxLength = 4000

        while (start < message.length) {
            val end = (start + maxLength).coerceAtMost(message.length)
            Log.i(tag, message.substring(start, end))
            start = end
        }
    }

    private suspend fun parseVideoInfoList(sourceUrl: String, jsonString: String): List<VideoInfo> {
        val videoList = mutableListOf<VideoInfo>()

        try {
            val lines = jsonString.trim().split("\n")

            for (line in lines) {
                if (line.isBlank()) continue
                val json = JSONObject(line)

                val bestFormat = pickBestFormatObject(json)

                val chosenVideoUrl = bestFormat?.optString("url", null)
                    ?: json.optString("url", null)

                val chosenFormatId = bestFormat?.optString("format_id", null)

                val httpHeaders = extractHttpHeaders(bestFormat ?: json)
                val cookies = extractCookies(bestFormat ?: json)

                Log.i(TAG, "Extracted Headers: $httpHeaders")
                Log.i(TAG, "Extracted Cookies: $cookies")

                Log.i(
                    TAG,
                    "Selected format for title='${json.optString("title")}' -> " +
                            "formatId=$chosenFormatId, url=$chosenVideoUrl"
                )

                var durationText = formatDuration(json.optInt("duration", -1))

                if (durationText == "00:00:00") {
                    val url = json.optString("url", null)
                    if (!url.isNullOrEmpty()) {
                        durationText = fetchVideoDuration(url, httpHeaders, cookies)
                    }
                }

                val fileSizeText = getFileSizeOrFetch(json, httpHeaders, cookies) ?: "Unknown"

                var thumbnailUrl = json.optString("thumbnail", null)

                Log.i(TAG, "parseVideoInfoList: thumbnailUrl $thumbnailUrl")

                if (thumbnailUrl.isNullOrEmpty()) {
                    val videoUrl = json.optString("url", null)
                    if (!videoUrl.isNullOrEmpty()) {
                        thumbnailUrl = fetchVideoThumbnail(videoUrl, httpHeaders, cookies)
                    }
                }

                Log.i(TAG, "=== Video Info ===")
                Log.i(TAG, "Title: ${json.optString("title", "Unknown")}")
                Log.i(TAG, "Format ID: ${json.optString("format_id")}")
                Log.i(TAG, "Duration: $durationText")
                Log.i(TAG, "Headers: $httpHeaders")
                Log.i(TAG, "Cookies: $cookies")
                Log.i(TAG, "FileSize: $fileSizeText")
                Log.i(TAG, "Thumbnail: $thumbnailUrl")
                Log.i(TAG, "==================")

                videoList.add(
                    VideoInfo(
                        sourceUrl = sourceUrl,
                        videoUrl = chosenVideoUrl,
                        formatId = chosenFormatId,
                        title = json.optString("title"),
                        thumbnail = thumbnailUrl,
                        duration = durationText,
                        fileSize = fileSizeText,
                        downloadStatus = DownloadStatus.PENDING,
                        httpHeaders = httpHeaders,
                        cookies = cookies
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseVideoInfoList failed: ${e.message}")
        }
        return videoList
    }

    private fun extractHttpHeaders(json: JSONObject): Map<String, String> {
        val headers = mutableMapOf<String, String>()

        try {
            val httpHeadersJson = json.optJSONObject("http_headers")
            if (httpHeadersJson != null) {
                val keys = httpHeadersJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = httpHeadersJson.optString(key)
                    if (value.isNotEmpty()) {
                        headers[key] = value
                    }
                }
            }

            if (headers.isEmpty()) {
                headers["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                headers["Accept"] = "*/*"
                headers["Accept-Language"] = "en-US,en;q=0.9"
            }

            Log.i(TAG, "📋 Extracted ${headers.size} headers")
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting headers: ${e.message}")
        }

        return headers
    }

    private fun extractCookies(json: JSONObject): String? {
        try {
            val cookiesString = json.optString("cookies", null)
            if (!cookiesString.isNullOrEmpty()) {
                Log.i(TAG, "🍪 Found cookies: ${cookiesString.take(100)}...")
                return cookiesString
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting cookies: ${e.message}")
        }
        return null
    }

    private fun pickBestFormatObject(json: JSONObject): JSONObject? {
        val formats = json.optJSONArray("formats") ?: return null
        var videoOnlyFallback: JSONObject? = null

        Log.i(TAG, "-------- PICKING BEST FORMAT (parseVideoInfoList) --------")

        for (i in 0 until formats.length()) {
            val f = formats.optJSONObject(i) ?: continue

            val id = f.optString("format_id")
            val vExt = f.optString("video_ext", null)
            val aExt = f.optString("audio_ext", null)
            val vCodec = f.optString("vcodec", null)
            val aCodec = f.optString("acodec", null)

            val hasVideo = (vExt != null && vExt != "none") || (vCodec != null && vCodec != "none")
            val hasAudio = (aExt != null && aExt != "none") || (aCodec != null && aCodec != "none")

            Log.i(TAG, "Format $id -> video=$hasVideo, audio=$hasAudio, vExt=$vExt, aExt=$aExt")

            if (hasVideo && hasAudio) {
                Log.i(TAG, "✔ PICKED FULL FORMAT: $id (video + audio)")
                return f
            }

            if (hasVideo && !hasAudio && videoOnlyFallback == null) {
                Log.i(TAG, "⚠ Candidate VIDEO-ONLY fallback: $id")
                videoOnlyFallback = f
            }
        }

        if (videoOnlyFallback != null) {
            Log.i(TAG, "✔ PICKED FALLBACK VIDEO-ONLY FORMAT: ${videoOnlyFallback.optString("format_id")}")
        } else {
            Log.w(TAG, "❌ NO SUITABLE FORMAT FOUND (video or video+audio)")
        }

        return videoOnlyFallback
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

    private suspend fun fetchVideoThumbnail(
        videoUrl: String,
        headers: Map<String, String>,
        cookies: String?
    ): String? = withContext(Dispatchers.IO) {
        try {
            val retriever = android.media.MediaMetadataRetriever()

            val headerMap = HashMap<String, String>()
            headers.forEach { (key, value) ->
                headerMap[key] = value
            }

            if (!cookies.isNullOrEmpty()) {
                headerMap["Cookie"] = cookies
            }

            retriever.setDataSource(videoUrl, headerMap)
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

    private suspend fun fetchVideoDuration(
        videoUrl: String,
        headers: Map<String, String>,
        cookies: String?
    ): String = withContext(Dispatchers.IO) {
        try {
            val retriever = android.media.MediaMetadataRetriever()

            val headerMap = HashMap<String, String>()
            headers.forEach { (key, value) ->
                headerMap[key] = value
            }

            if (!cookies.isNullOrEmpty()) {
                headerMap["Cookie"] = cookies
            }

            retriever.setDataSource(videoUrl, headerMap)
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

    private suspend fun getFileSizeOrFetch(
        json: JSONObject,
        headers: Map<String, String>,
        cookies: String?
    ): String? {
        getSizeFromFormats(json)?.let { size ->
            return formatSize(size)
        }

        val approx = json.optLong("filesize_approx", -1)
        if (approx > 0) {
            return formatSize(approx)
        }

        val url = json.optString("url", null) ?: return null
        if (url.endsWith(".m3u8")) {
            val hlsSize = estimateHlsSize(url, headers, cookies)
            if (hlsSize != null && hlsSize > 0) {
                return formatSize(hlsSize)
            }
        }

        val headSize = fetchFileSizeFromUrl(url, headers, cookies)
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

    private suspend fun estimateHlsSize(
        m3u8Url: String,
        headers: Map<String, String>,
        cookies: String?
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(m3u8Url).openConnection() as HttpURLConnection

            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            if (!cookies.isNullOrEmpty()) {
                connection.setRequestProperty("Cookie", cookies)
            }

            val text = connection.inputStream.bufferedReader().readText()
            val base = m3u8Url.substringBeforeLast("/")
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
                val size = fetchFileSizeFromUrl(segUrl, headers, cookies) ?: continue
                total += size
            }

            total
        } catch (e: Exception) {
            Log.e(TAG, "estimateHlsSize error: ${e.message}")
            null
        }
    }

    private suspend fun fetchFileSizeFromUrl(
        url: String,
        headers: Map<String, String>,
        cookies: String?
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"

            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            if (!cookies.isNullOrEmpty()) {
                connection.setRequestProperty("Cookie", cookies)
            }

            connection.connect()
            val size = connection.contentLengthLong
            connection.disconnect()

            if (size > 0) size else null
        } catch (e: Exception) {
            Log.e(TAG, "fetchFileSizeFromUrl error: ${e.message}")
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

            video.httpHeaders?.forEach { (key, value) ->
                addOption("--add-header", "$key:$value")
            }

            if (!video.cookies.isNullOrEmpty()) {
                val cookieFile = File.createTempFile("cookies", ".txt")
                cookieFile.writeText(video.cookies)
                addOption("--cookies", cookieFile.absolutePath)
            }
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