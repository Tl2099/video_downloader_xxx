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

    override suspend fun getVideoInfo(url: String): VideoInfo? = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(url).apply {
                addOption("--no-warnings")
                addOption("--dump-json")
                addOption("--no-playlist")
                addOption("--no-download")
                addOption("--format", "bestvideo+bestaudio/best")
                // addOption("--extractor-args", "generic:impersonate=chrome101")
            }

            Log.i("BrowserWebViewClient", "getVideoInfo: Executing yt-dlp request for $url")
            val processId = "fetch_info_${System.currentTimeMillis()}"
            val response: YoutubeDLResponse =
                YoutubeDL.getInstance().execute(request, processId, null)

            if (response.out.isEmpty()) {
                Log.e("BrowserWebViewClient", "No video information returned for URL: $url")
                return@withContext null
            }
            return@withContext parseVideoInfo(url, response.out)

//            videoInfo?.let {
//                view?.post { onVideoDetected(it) }
//            }

        } catch (e: Exception) {
            Log.e("BrowserWebViewClient", "yt-dlp error: ${e.message}", e)
            null
        }
    }

    @SuppressLint("DefaultLocale")
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
            Log.e("BrowserWebViewClient", "parseVideoInfo failed: ${e.message}")
            null
        }
    }
}