package com.example.video_downloader_xxx.ui.fragment.browser.web

import android.app.AlertDialog
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.video_downloader_xxx.MainActivity
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.FragmentWebTabBinding
import com.example.video_downloader_xxx.service.VideoDownloadService
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.browser.home.DownloadViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class WebFragment : BaseFragment<FragmentWebTabBinding>() {
    private val sharedVM: SharedViewModel by activityViewModels()
    private val downloadViewModel: DownloadViewModel by viewModel()
    private val downloadService: VideoDownloadService by inject()
    private var serviceBound = false
    private lateinit var webView: WebView
    private var url: String? = null
    private val detectedVideos = mutableListOf<VideoInfo>()
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
                if(webView.canGoForward()){
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
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = false
        }

        webView.webViewClient = BrowserWebViewClient { onVideoDetected(it) }

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

    private fun onVideoDetected(videoInfo: VideoInfo) {
        detectedVideos.add(videoInfo)
        currentVideo = videoInfo
        Log.i("WebFragment_ttdat", "onVideoDetected: ${videoInfo.sourceUrl}")
        showVideoDetectedDialog(videoInfo)
    }

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
                downloadViewModel.start(videoInfo.sourceUrl, outFile)
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
    }

    override fun initListener() {
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentWebTabBinding =
        FragmentWebTabBinding.inflate(layoutInflater)
}