package com.example.video_downloader_xxx.ui.fragment.browser.web

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.video_downloader_xxx.util.AdFilter
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebViewClient(
    private val callbacks: WebCallbacks
) : WebViewClient() {

    private val detectedUrls = mutableSetOf<String>()
    private val adBlocker = AdFilter()
    private val videoUrls = mutableSetOf<String>()

    private val normalizedVideoUrls = mutableSetOf<String>()
    private val hlsStreamIds = mutableSetOf<String>()

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

        if (adBlocker.isAd(url)) {
            Log.i("WebViewClient", "Blocked ad: $url")
            return adBlocker.createBlockedResponse()
        }
        if (adBlocker.isHLSAdSegment(url)) {
            Log.i("WebViewClient", "Blocked HLS ad segment: $url")
            return adBlocker.createBlockedResponse()
        }

        // Handle HLS segments with duplicate prevention
        if (adBlocker.isHLSVideoSegment(url)) {
            val normalizedUrl = adBlocker.normalizeUrl(url)

            view?.post {
                callbacks.onHLSSegmentDetected(url)
            }

            return super.shouldInterceptRequest(view, request)
        }

        val contentType = request.requestHeaders["Content-Type"]
        val contentLength = request.requestHeaders["Content-Length"]?.toLongOrNull()

        if (adBlocker.isVideoCandidate(url, contentType, contentLength)) {

            if (!videoUrls.contains(url)) {
                videoUrls.add(url)
                Log.i("WebViewClient", "NEW Video candidate detected: $url")
                view?.post {
                    callbacks.onVideoUrlDetected(url, contentType, contentLength)
                }
            } else {
                Log.d("WebViewClient", "DUPLICATE Video candidate ignored: $url")
            }
        }

        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        videoUrls.clear()
        normalizedVideoUrls.clear()
        hlsStreamIds.clear()

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
            callbacks.onUrlLoaded(it)
        }
    }
}

data class WebCallbacks(
    val onUrlLoaded: (String) -> Unit,
    val onPageStartedCallback: (String?) -> Unit,
    val onPageFinishedCallback: (String?) -> Unit,
    val onVideoUrlDetected: (String, String?, Long?) -> Unit,
    val onHLSSegmentDetected: (String) -> Unit,
)
