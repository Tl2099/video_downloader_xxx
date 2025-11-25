package com.example.video_downloader_xxx.ui.fragment.browser.web

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Service.START_NOT_STICKY
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.DataExt
import com.example.video_downloader_xxx.data.local.entities.WebsiteHistoryEntity
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.FragmentWebTabBinding
import com.example.video_downloader_xxx.service.VideoDownloadService
import com.example.video_downloader_xxx.service.VideoDownloadService.Companion.EXTRA_DURATION
import com.example.video_downloader_xxx.service.VideoDownloadService.Companion.EXTRA_FILE_SIZE
import com.example.video_downloader_xxx.service.VideoDownloadService.Companion.EXTRA_ID
import com.example.video_downloader_xxx.service.VideoDownloadService.Companion.EXTRA_SOURCE_URL
import com.example.video_downloader_xxx.service.VideoDownloadService.Companion.EXTRA_THUMB
import com.example.video_downloader_xxx.service.VideoDownloadService.Companion.EXTRA_TITLE
import com.example.video_downloader_xxx.service.VideoDownloadService.Companion.EXTRA_VIDEO_URL
import com.example.video_downloader_xxx.ui.activity.MainActivity
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.browser.home.DownloadUrlVideoBottomSheet
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.history.WebsiteHistoryViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.home.BrowserHomeFragment
import com.example.video_downloader_xxx.util.DownloadStatus
import com.example.video_downloader_xxx.util.FileHelper
import com.example.video_downloader_xxx.util.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier._q
import java.io.ByteArrayOutputStream
import java.io.File

class WebFragment : BaseFragment<FragmentWebTabBinding>() {
    private val downloadViewModel: SharedViewModel by activityViewModel()
    private val history: WebsiteHistoryViewModel by activityViewModel()
    private lateinit var webView: WebView
    private val hlsSegments = mutableSetOf<String>()
    private var hlsPlaylistUrl: String? = null
    private var hasShownAddedDialog = false

    private var currentTitle: String? = null
    private var currentFavicon: Bitmap? = null

    private var lastScrollY = 0
    private var isToolbarShown = true

    private val args: WebFragmentArgs by navArgs()

    private var downloadService: VideoDownloadService? = null
    private var serviceBound = false
    private val serviceConnection = object : ServiceConnection {
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

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (serviceBound) {
                requireContext().unbindService(serviceConnection)
                serviceBound = false
            }
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Service was not bound, cannot unbind.")
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

            edtSearch.setOnEditorActionListener { v, actionId, event ->

                val text = v.text.toString().trim()

                if (text.isNotEmpty()) {
                    loadInput(text)
                }
                true

            }
        }
    }

    private fun loadInput(input: String) {
        hideKeyboard()
        val url = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") -> "https://$input"
            else -> "https://www.google.com/search?q=${input.replace(" ", "+")}"
        }
        webView.loadUrl(url)
        binding?.edtSearch?.clearFocus()
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
                onPageStartedCallback = {
                    Log.i(TAG, "onPageStarted: $it")

                    downloadViewModel.clearDetectedWebVideos()
                    hlsSegments.clear()
                    hlsPlaylistUrl = null

                    fabState(false)
                    binding?.edtSearch?.setText(it)
                },
                onPageFinishedCallback = { url ->
                    Log.i(TAG, "onPageFinished: $url")
                    binding?.edtSearch?.setText(url)

                    if (!isAdded) return@WebCallbacks

                    if (!url.isNullOrBlank()) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            if (!isAdded) return@launch
                            history.addVisit(
                                WebsiteHistoryEntity(
                                    url = url,
                                    title = currentTitle ?: "UnKnown",
                                    faviconUrl = currentFavicon?.let { bitmapToBase64(it) },
                                    lastVisited = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                },
                onVideoUrlDetected =
                    { url, contentType, contentLength ->
                        Log.i("ttdat", "setupWebView: $url")
                        onVideoUrlDetected(url, contentType, contentLength)
                    },
                onHLSSegmentDetected =
                    { segmentUrl ->
                        //onHLSSegmentDetected(segmentUrl)
                    }
            )
        )

        webView.webChromeClient =
            object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding?.progressBar?.apply {
                        progress = newProgress
                        visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                    }
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    currentTitle = title
                }

                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    currentFavicon = icon
                }
            }

        webView.viewTreeObserver.addOnScrollChangedListener {
            val currentY = webView.scrollY
            if (currentY > lastScrollY + 10 && isToolbarShown) {
                binding?.appBarLayout?.setExpanded(false, true)
                isToolbarShown = false
            }
            if (currentY < lastScrollY - 10 && !isToolbarShown) {
                binding?.appBarLayout?.setExpanded(true, true)
                isToolbarShown = true
            }

            lastScrollY = currentY
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
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
        val quality = Regex("/(\\d+k)/").find(sampleSegmentUrl)?.groupValues?.get(1) ?: "unknown"

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

        // Update FAB state
        //fabState(true)

    }

    private fun fabState(isCheck: Boolean) {
        binding?.fabDownload?.animate()?.setDuration(200)?.withStartAction {
            binding?.fabDownload?.isSelected = isCheck
        }?.start()
    }

    private fun openWebFromHistory(){
        val url = args.url
        binding?.webViewContainer?.loadUrl(url)
    }

    override fun initData() {
    }

    override fun initListener() {
        openWebFromHistory()
        binding?.apply {
            fabDownload.setOnClickListener {
                if (fabDownload.isSelected) {
                    showBottomSheet()
                } else {
                    return@setOnClickListener
                }
            }
        }
        downloadViewModel.videoWebList.onEach {
            fabState(true)
        }.launchIn(lifecycleScope)
    }

    private fun showBottomSheet() {
        val sheet = DownloadVideoWebBottomSheet.newInstance(
            onDownload = {
                Log.i(TAG, "onDownload: ${it.videoUrl}")
                startDownload(it)
                showTaskAddedDialog(it.sourceUrl)
            },
            onClose = {
            }
        )
        sheet.show(parentFragmentManager, "DownloadSheet")
    }

    private fun startDownload(videoInfo: VideoInfo) {
        Log.i(TAG, "startDownload called for: ${videoInfo.title}")
        val context = requireContext().applicationContext

        if (downloadService?.isAlreadyQueuedOrDownloading(videoInfo.id) == true) {
            //Toast.makeText(context, "Video is already in download queue", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(context, VideoDownloadService::class.java).apply {
            putExtra(EXTRA_ID, videoInfo.id)
            putExtra(EXTRA_SOURCE_URL, videoInfo.sourceUrl)
            putExtra(EXTRA_VIDEO_URL, videoInfo.videoUrl)
            putExtra(EXTRA_TITLE, videoInfo.title)
            putExtra(EXTRA_THUMB, videoInfo.thumbnailUrl)
            putExtra(EXTRA_DURATION, videoInfo.duration)
            putExtra(EXTRA_FILE_SIZE, videoInfo.fileSize)
        }

        context.startService(intent)

        if (!serviceBound) {
            context.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
        //Toast.makeText(context, "Download started: ${videoInfo.title}", Toast.LENGTH_SHORT).show()
    }

    private fun showTaskAddedDialog(sourceUrl: String) {
        Log.i(TAG, "showTaskAddedDialog: called")
        if (hasShownAddedDialog) return
        hasShownAddedDialog = true

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_task_add_layout, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val btnView = view.findViewById<AppCompatTextView>(R.id.btnShowDownload)
        val btnClose = view.findViewById<AppCompatImageView>(R.id.btnClose)

        tvTitle.text = sourceUrl

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnView.setOnClickListener {
            dialog.dismiss()
            (requireActivity() as MainActivity).openProgressScreen()
        }
        dialog.setContentView(view)
        dialog.behavior.isDraggable = false
        dialog.setOnDismissListener {
            hasShownAddedDialog = false
        }
        dialog.show()
        view.postDelayed({
            if (dialog.isShowing) dialog.dismiss()
        }, 20000)
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentWebTabBinding =
        FragmentWebTabBinding.inflate(layoutInflater)

    companion object {
        const val TAG = "WebFragment"
    }
}