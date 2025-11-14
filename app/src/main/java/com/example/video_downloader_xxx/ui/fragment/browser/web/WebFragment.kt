package com.example.video_downloader_xxx.ui.fragment.browser.web

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.video_downloader_xxx.data.DataExt
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.FragmentWebTabBinding
import com.example.video_downloader_xxx.service.VideoDownloadService
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.browser.home.DownloadUrlVideoBottomSheet
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
    private val downloadViewModel: SharedViewModel by activityViewModel()
    private lateinit var webView: WebView
    private val hlsSegments = mutableSetOf<String>()
    private var hlsPlaylistUrl: String? = null

    private var downloadService: VideoDownloadService? = null
    private var serviceBound = false
    private val serviceConnection = object : ServiceConnection{
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            val binder = service as VideoDownloadService.DownloadBinder
            downloadService = binder.getService()
            serviceBound = true
            Log.i(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            downloadService = null
            serviceBound = false
            Log.i(TAG, "Service disconnected")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindDownloadService()
    }

    private fun bindDownloadService() {
        val intent = Intent(requireContext(), VideoDownloadService::class.java)
        requireContext().bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            requireContext().unbindService(serviceConnection)
            serviceBound = false
        }
    }

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

    @SuppressLint("SetJavaScriptEnabled")
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

                    downloadViewModel.clearDetectedWebVideos()
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
    }

    private fun onHLSSegmentDetected(segmentUrl: String) {
        hlsSegments.add(segmentUrl)
        val playlistUrl = extractPlaylistFromSegment(segmentUrl)

        if (playlistUrl != null && playlistUrl != hlsPlaylistUrl) {
            hlsPlaylistUrl = playlistUrl

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
        val videoId =
            Regex("/\\d{8}/(\\w+)/").find(sampleSegmentUrl)?.groupValues?.get(1) ?: "video"

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

        downloadViewModel.addVideo(url)
//        val videoInfo = createVideoInfoFromUrl(url, contentType, contentLength)
//
//        // Thêm vào list để hiển thị
//        downloadViewModel.addDetectedVideo(videoInfo)

        // Update FAB state
        binding?.fabDownload?.animate()?.setDuration(200)?.withStartAction {
            binding?.fabDownload?.isSelected = true
        }?.start()
    }

    private fun createVideoInfoFromUrl(
        url: String,
        contentType: String?,
        contentLength: Long?
    ): VideoInfo {
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
        //detectedVideos.clear()
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

//    private fun startDownload(videoInfo: VideoInfo) {
//        Log.i("WebFragment_ttdat", "startDownload: Called")
//        downloadService.startDownload(videoInfo)
//        if (serviceBound) {
//            Log.i("WebFragment_ttdat", "startDownload: ${videoInfo.title}")
//            downloadService.startDownload(videoInfo)
//        }
//        Toast.makeText(requireContext(), "Download started!", Toast.LENGTH_SHORT).show()
//
//    }

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
        val sheet = DownloadVideoWebBottomSheet.newInstance(
            onDownload = {
                Log.i(TAG, "onDownload: ${it.videoUrl}")

                //val outFile = FileHelper.createVideoFile(requireContext())
                //downloadViewModel.downloadVideo(it, outFile)

                startDownload(it)
            },
            onClose = {
            }
        )
        sheet.show(parentFragmentManager, "DownloadSheet")
    }

    private fun startDownload(videoInfo: VideoInfo) {
        Log.i(TAG, "startDownload called for: ${videoInfo.title}")
        val context = requireContext().applicationContext
        val intent = Intent(context, VideoDownloadService::class.java).apply {
            putExtra(VideoDownloadService.EXTRA_ID, videoInfo.id)
            putExtra(VideoDownloadService.EXTRA_SOURCE_URL, videoInfo.sourceUrl)
            putExtra(VideoDownloadService.EXTRA_VIDEO_URL, videoInfo.videoUrl)
            putExtra(VideoDownloadService.EXTRA_TITLE, videoInfo.title)
            putExtra(VideoDownloadService.EXTRA_THUMB, videoInfo.thumbnailUrl)
            putExtra(VideoDownloadService.EXTRA_DURATION, videoInfo.duration)
            putExtra(VideoDownloadService.EXTRA_FILE_SIZE, videoInfo.fileSize)
        }

        context.startService(intent)

        Toast.makeText(context, "Download started: ${videoInfo.title}", Toast.LENGTH_SHORT).show()
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentWebTabBinding =
        FragmentWebTabBinding.inflate(layoutInflater)

    companion object {
        const val TAG = "WebFragment"
    }
}