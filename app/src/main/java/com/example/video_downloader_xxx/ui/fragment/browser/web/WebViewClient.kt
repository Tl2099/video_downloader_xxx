package com.example.video_downloader_xxx.ui.fragment.browser.web

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebViewClient(
    private val callbacks: WebCallbacks
) : WebViewClient() {

    private val detectedUrls = mutableSetOf<String>()

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val newUrl = request?.url?.toString()
        Log.d("BrowserWebViewClient_ttdat", "➡️ Navigating to: $newUrl")
        return false
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
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
)
