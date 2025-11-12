package com.example.video_downloader_xxx.ui.fragment.browser.web

import android.app.AlertDialog
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.FragmentWebTabBinding
import com.example.video_downloader_xxx.service.VideoDownloadService
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.browser.DownloadUrlVideoBottomSheet
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.util.DownloadStatus
import com.example.video_downloader_xxx.util.FileHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier._q
import java.io.File

class WebFragment : BaseFragment<FragmentWebTabBinding>() {
    private val TAG = this::class.java.simpleName
    private val downloadViewModel: SharedViewModel by activityViewModel()
    private val webViewModel: WebViewModel by viewModel()
    private val downloadService: VideoDownloadService by inject()
    private var serviceBound = false
    private lateinit var webView: WebView
    private var url: String? = null
    private val detectedVideos = mutableListOf<VideoInfo>()
    private val detectedVideoUrls = mutableSetOf<String>()
    private val videoIdTracker = mutableSetOf<String>()
    private val dashStreams = mutableMapOf<String, MutableList<String>>()
    private val hlsSegments = mutableSetOf<String>()
    private var hlsPlaylistUrl: String? = null
    private var currentVideo: VideoInfo? = null

    override fun initView() {
        setupWebView()

        val text = arguments?.getString("url") ?: return
        val url = if (text.startsWith("http")) text
        else "https://www.google.com/search?q=${text.replace(" ", "+")}"
        webView.loadUrl(url)

        binding?.apply {
            ivCloseTab.setOnClickListener {
                findNavController().popBackStack()
            }

            ivGoBack.setOnClickListener {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    findNavController().popBackStack()
                }
            }

            ivRefresh.setOnClickListener {
                webView.reload()
            }

            ivGoForward.setOnClickListener {
                if (webView.canGoForward()) {
                    webView.goForward()
                }
            }


        }
        //observeDownloadEvents()
    }

    private fun setupWebView() {
        webView = binding?.webViewContainer ?: return
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            mediaPlaybackRequiresUserGesture = false
        }

        webView.webViewClient = WebViewClient(
            WebCallbacks(
//                onUrlLoaded = {
//                    Log.i(TAG, "onUrlLoaded: $it")
//                    downloadViewModel.fetchVideoInfo(it)
//                },
                onPageStartedCallback = {
                    Log.i(TAG, "onPageStarted: $it")

                    downloadViewModel.clearDetectedVideos()
                    detectedVideos.clear()
                    detectedVideoUrls.clear()
                    hlsSegments.clear()
                    hlsPlaylistUrl = null

                    resetFabState()
                    binding?.edtSearch?.setText(it)
                },
                onPageFinishedCallback = {
                    Log.i(TAG, "onPageFinished: $it")
                    binding?.edtSearch?.setText(it)
                },
                onVideoUrlDetected = { url, contentType, contentLength ->
                    Log.i("ttdat", "setupWebView: $url")
                    onVideoUrlDetected(url, contentType, contentLength)
                    webViewModel.onVideoCandidate(url, contentType, contentLength)
                },
                onHLSSegmentDetected = { segmentUrl ->
                    onHLSSegmentDetected(segmentUrl)
                }
            )
        )

//        downloadViewModel.videoDetected
//            .filterNotNull()
//            .onEach {
//                Log.i(TAG, "videoDetected - test: ${it.title} ${it.fileSize} ")
//            }.launchIn(lifecycleScope)

//        webViewModel.videoDetected
//            .filterNotNull()
//            .onEach {
//                Log.i(TAG, "videoDetected - test: ${it.title} ${it.fileSize} ")
//            }.launchIn(lifecycleScope)

//        downloadViewModel.videoList
//            .filterNotNull()
//            .onEach {
//                for (video in it) {
//                    Log.i(TAG, "videoDetected: ${video.title} ${video.fileSize} ")
//                }
//                //onVideoDetected(it)
//            }.launchIn(lifecycleScope)
////        lifecycleScope.launch {
////            webViewModel.videoDetected.collectLatest { info ->
////                info?.let { onVideoDetected(it) }
////            }
////        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding?.progressBar?.apply {
                    progress = newProgress
                    visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                }
            }

        }

//        binding.ivCloseTab.setOnClickListener { f }
    }
    private fun onHLSSegmentDetected(segmentUrl: String) {
        hlsSegments.add(segmentUrl)

        // Tìm playlist URL từ segment URL
        val playlistUrl = extractPlaylistFromSegment(segmentUrl)

        if (playlistUrl != null && playlistUrl != hlsPlaylistUrl) {
            hlsPlaylistUrl = playlistUrl

            // Tạo VideoInfo từ HLS playlist
            val videoInfo = createVideoInfoFromHLS(playlistUrl, segmentUrl)
            downloadViewModel.addDetectedVideo(videoInfo)

            Log.i(TAG, "HLS stream detected: $playlistUrl")
        }
    }

    private fun extractPlaylistFromSegment(segmentUrl: String): String? {
        val baseUrl = segmentUrl.substringBeforeLast("/")
        return "$baseUrl/index.m3u8"
    }

    private fun createVideoInfoFromHLS(playlistUrl: String, sampleSegmentUrl: String): VideoInfo {
        // Extract quality from URL (3000k)
        val quality = Regex("/(\\d+k)/").find(sampleSegmentUrl)?.groupValues?.get(1) ?: "unknown"

        // Extract video ID
        val videoId = Regex("/\\d{8}/(\\w+)/").find(sampleSegmentUrl)?.groupValues?.get(1) ?: "video"

        return VideoInfo(
            sourceUrl = webView.url ?: playlistUrl,
            videoUrl = playlistUrl,
            title = "HLS Video $videoId ($quality)",
            thumbnailUrl = null,
            duration = null,
            fileSize = null,
            downloadStatus = DownloadStatus.PENDING
        )
    }

    private fun onVideoUrlDetected(url: String, contentType: String?, contentLength: Long?) {
        Log.i(TAG, "Video URL detected: $url")
        Log.i(TAG, "Content-Type: $contentType, Size: $contentLength")

        detectedVideoUrls.add(url)

        // Tạo VideoInfo từ thông tin cơ bản
        val videoInfo = createVideoInfoFromUrl(url, contentType, contentLength)

        // Thêm vào list để hiển thị
        downloadViewModel.addDetectedVideo(videoInfo)

        // Update FAB state
        binding?.fabDownload?.animate()?.setDuration(200)?.withStartAction {
            binding?.fabDownload?.isSelected = true
        }?.start()
    }

    private fun createVideoInfoFromUrl(url: String, contentType: String?, contentLength: Long?): VideoInfo {
        val title = extractTitleFromUrl(url) ?: "Video ${System.currentTimeMillis()}"

        val fileSize = contentLength?.let { size ->
            String.format("%.2f MB", size / 1024f / 1024f)
        }

        return VideoInfo(
            sourceUrl = webView.url ?: url,
            videoUrl = url,
            title = title,
            thumbnailUrl = null,
            duration = null,
            fileSize = fileSize,
            downloadStatus = DownloadStatus.PENDING
        )
    }

    private fun extractTitleFromUrl(url: String): String? {
        // Logic để extract title từ URL
        // Ví dụ: lấy filename hoặc segment cuối của URL
        val segments = url.split("/")
        val filename = segments.lastOrNull()?.split("?")?.firstOrNull()
        return filename?.takeIf { it.isNotEmpty() }
    }


    private fun resetFabState() {
        detectedVideos.clear()
        binding?.fabDownload?.isSelected = false
    }

//    private fun onVideoDetected(listVideo: List<VideoInfo>) {
//        //detectedVideos.addAll(listVideo)
//
//        downloadViewModel.onFindVideoDone.onEach {
//            binding?.fabDownload?.animate()?.setDuration(200)?.withStartAction {
//                binding?.fabDownload?.isSelected = true
//            }?.start()
//        }.launchIn(lifecycleScope)
//
//
//        //Log.i("WebFragment_ttdat", "onVideoDetected: ${videoInfo.sourceUrl}")
//
//        //showVideoDetectedDialog(videoInfo)
//    }

    private fun showVideoDetectedDialog(videoInfo: VideoInfo) {
        val downloadsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        } else {
            @Suppress("DEPRECATION")
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        }

        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val outFile = File(downloadsDir, "video_${System.currentTimeMillis()}.mp4")
        AlertDialog.Builder(requireContext())
            .setTitle("Video Detected")
            .setMessage("Found: ${videoInfo.title} \n ${videoInfo.sourceUrl}")
            .setPositiveButton("Download") { dialog, _ ->
                Log.i("WebFragment_ttdat", "showVideoDetectedDialog: Called")
                //downloadViewModel.start(videoInfo.sourceUrl, outFile)
                // startDownload(videoInfo)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startDownload(videoInfo: VideoInfo) {
        Log.i("WebFragment_ttdat", "startDownload: Called")
        downloadService.startDownload(videoInfo)
        if (serviceBound) {
            Log.i("WebFragment_ttdat", "startDownload: ${videoInfo.title}")
            downloadService.startDownload(videoInfo)
        }
        Toast.makeText(requireContext(), "Download started!", Toast.LENGTH_SHORT).show()

    }

//    private fun observeDownloadEvents() {
//        downloadViewModel.downloadVideoEvent.observe(viewLifecycleOwner) { videoInfo ->
//            Log.i("WebFragment_ttdat", "Received download event: ${videoInfo.title}")
//            if (serviceBound) {
//                downloadService.startDownload(videoInfo)
//            }
//        }
//    }

    override fun initData() {
//        downloadViewModel.onFindVideoDone
//            .onEach { it ->
//                val sheet = DownloadUrlVideoBottomSheet.newInstance(
//                    onDownload = {
//                        val outFile = FileHelper.createVideoFile(requireContext())
//                        downloadViewModel.downloadVideo(it, outFile)
//                    },
//                    onClose = {
//                    }
//                )
//                sheet.show(parentFragmentManager, "DownloadSheet")
//            }
//            .launchIn(lifecycleScope)
    }

    override fun initListener() {
        binding?.apply {
            fabDownload.setOnClickListener {
                if (fabDownload.isSelected) {
                    showBottomSheet()
                } else {
                    return@setOnClickListener
                }
            }
        }
    }

    private fun showBottomSheet() {
        val sheet = DownloadUrlVideoBottomSheet.newInstance(
            onDownload = {
                Log.i(TAG, "onDownload: ${it.sourceUrl}")
                val outFile = FileHelper.createVideoFile(requireContext())
                downloadViewModel.downloadVideo(it, outFile)
            },
            onClose = {
            }
        )
        sheet.show(parentFragmentManager, "DownloadSheet")
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentWebTabBinding =
        FragmentWebTabBinding.inflate(layoutInflater)
}