package com.example.video_downloader_xxx.ui.fragment.browser.web

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.video_downloader_xxx.util.AdFilter
import java.util.Collections

class WebViewClient(
    private val callbacks: WebCallbacks
) : WebViewClient() {

    private val detectedUrls = mutableSetOf<String>()
    private val adBlocker = AdFilter()
    //private val videoUrls = mutableSetOf<String>()

    private val normalizedVideoUrls = mutableSetOf<String>()
    private val hlsStreamIds = mutableSetOf<String>()

    private val videoUrls = Collections.synchronizedSet(mutableSetOf<String>())

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val newUrl = request?.url?.toString()
        Log.d("BrowserWebViewClient_ttdat", "➡️ Navigating to: $newUrl")
        return false
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

        if (url.endsWith(".mp4")) {
            Log.w("WebViewClient", "MP4 seen by WebView: $url")
        }

        if (adBlocker.isAd(url) ) {
            Log.i("WebViewClient", "Blocked by isAd: $url")
            return adBlocker.createBlockedResponse()
        }

        if ( adBlocker.isHLSAdSegment(url)) {
            Log.i("WebViewClient", "Blocked by isHLSAdSegment: $url")
            return adBlocker.createBlockedResponse()
        }

        // Handle HLS segments with duplicate prevention
        if (adBlocker.isHLSVideoSegment(url)) {
            Log.d("WebViewDebug", "✅ HLS segment detected: $url")
            view?.post {
                callbacks.onHLSSegmentDetected(url)
            }

            return super.shouldInterceptRequest(view, request)
        }

        val contentType = request.requestHeaders["Content-Type"]
        val contentLength = request.requestHeaders["Content-Length"]?.toLongOrNull()

        Log.d("WebViewDebug", "➡️ Checking candidate: $url ct=$contentType cl=$contentLength")

        val isVideo = adBlocker.isVideoCandidate(url, contentType, contentLength)
        Log.d("WebViewDebug", "📊 isVideoCandidate($url) => $isVideo")


        if (isVideo) {
            val normalizedUrl = adBlocker.normalizeUrl(url)
            val added = videoUrls.add(normalizedUrl)
            Log.d("WebViewDebug", "🧩 normalized=$normalizedUrl | added=$added")

            if (added) {
                val mimeTypeFromUrl = adBlocker.getVideoMimeType(normalizedUrl)
                val finalMimeType = mimeTypeFromUrl ?: contentType
                Log.i("WebViewDebug", "🎬 NEW Video detected - old : $normalizedUrl")
                Log.i(
                    "WebViewDebug",
                    "🎬 NEW Video detected - new: $normalizedUrl (mime=$finalMimeType, cl=$contentLength)"
                )
                view?.post {
                    callbacks.onVideoUrlDetected(normalizedUrl, finalMimeType, contentLength)
                }
            } else {
                Log.d("WebViewDebug", "🔁 DUPLICATE ignored: $normalizedUrl")
            }
        }

        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        videoUrls.clear()
        normalizedVideoUrls.clear()
        hlsStreamIds.clear()
        videoUrls.clear()

        Log.i("BrowserWebViewClient", "onPageStarted: $url")
        url?.let {
            callbacks.onPageStartedCallback(it)
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.d("BrowserWebViewClient_ttdat", "🌐 Page loaded: $url")
        if (url.isNullOrBlank() || detectedUrls.contains(url)) return
        detectedUrls.add(url)
        Log.d("BrowserWebViewClient", "🌐 Page loaded: $url → checking video...")
        url.let {
            callbacks.onPageFinishedCallback(it)
        }
    }
}

data class WebCallbacks(
    val onPageStartedCallback: (String?) -> Unit,
    val onPageFinishedCallback: (String?) -> Unit,
    val onVideoUrlDetected: (String, String?, Long?) -> Unit,
    val onHLSSegmentDetected: (String) -> Unit,
)
