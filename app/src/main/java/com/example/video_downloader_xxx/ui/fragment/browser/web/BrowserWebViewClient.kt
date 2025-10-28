package com.example.video_downloader_xxx.ui.fragment.browser.web

//class BrowserWebViewClient(
//    private val onVideoDetected: (VideoInfo) -> Unit
//) : WebViewClient() {
//
//    override fun shouldInterceptRequest(
//        view: WebView?,
//        request: WebResourceRequest?
//    ): WebResourceResponse? {
//        val url = request?.url.toString() ?: return super.shouldInterceptRequest(view, request)
//
//        if (isVideoUrl(url)) {
//            val videoInfo = extractVideoInfo(url, view)
//            if (videoInfo != null) {
//                onVideoDetected(videoInfo)
//            }
//        }
//
//        return super.shouldInterceptRequest(view, request)
//    }
//
//    private fun isVideoUrl(url: String): Boolean {
//        val videoExtensions = listOf(".mp4", ".webm", ".avi", ".mov", ".mkv", ".flv")
//        val videoKeywords = listOf("video", "stream", "playlist")
//
//        return videoExtensions.any { url.contains(it, ignoreCase = true) }
//                || videoKeywords.any { url.contains(it, ignoreCase = true) }
//    }
//
//    private fun extractVideoInfo(videoInfo: VideoInfo, webView: WebView?): VideoInfo? {
//        val title = webView?.title ?: "Unknown Video"
//        return VideoInfo(title = title, url = videoInfo.sourceUrl)
//    }
//}