package com.example.video_downloader_xxx.ui.fragment.browser.web

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.util.DownloadStatus
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class BrowserWebViewClient(
    private val onVideoDetected: (VideoInfo) -> Unit
) : WebViewClient() {

    private val detectedUrls = mutableSetOf<String>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)


    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val newUrl = request?.url?.toString()
        Log.d("BrowserWebViewClient_ttdat", "➡️ Navigating to: $newUrl")
        // Let the WebView handle loading the URL. The video check will be performed in onPageFinished.
        return false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.d("BrowserWebViewClient_ttdat", "🌐 Page loaded: $url")
        if (url.isNullOrBlank()) return
        if (detectedUrls.contains(url)) return

        detectedUrls.add(url)
        Log.d("BrowserWebViewClient", "🌐 Page loaded: $url → checking video...")

        coroutineScope.launch {
            checkVideoWithYtDlp(url, view)
        }
    }


    private fun checkVideoWithYtDlp(pageUrl: String, view: WebView?) {
        try {
            val request = YoutubeDLRequest(pageUrl).apply {
                addOption("--no-warnings")
                addOption("--dump-json")
                addOption("--no-playlist")
                addOption("--no-download")
                addOption("--format", "bestvideo+bestaudio/best")
                // addOption("--extractor-args", "generic:impersonate=chrome101")
            }

            Log.i("BrowserWebViewClient", "getVideoInfo: Executing yt-dlp request for $pageUrl")
            val processId = "fetch_info_${System.currentTimeMillis()}"
            val response: YoutubeDLResponse =
                YoutubeDL.getInstance().execute(request, processId, null)

            if (response.out.isNullOrEmpty()) {
                Log.e("BrowserWebViewClient", "No video information returned for URL: $pageUrl")
                return
            }
            val videoInfo = parseVideoInfo(pageUrl, response.out)

            videoInfo?.let {
                view?.post { onVideoDetected(it) }
            }

            Log.i("BrowserWebViewClient", "yt-dlp response output: ${response.out}")

        } catch (e: Exception) {
            Log.e("BrowserWebViewClient", "yt-dlp error: ${e.message}", e)
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
            Log.e("BrowserWebViewClient", "parseVideoInfo failed: ${e.message}")
            null
        }
    }

    /** (Giữ nguyên): dùng nếu muốn lọc các URL rác khi duyệt */
//    override fun shouldInterceptRequest(
//        view: WebView?,
//        request: WebResourceRequest?
//    ) = super.shouldInterceptRequest(view, request)
}
