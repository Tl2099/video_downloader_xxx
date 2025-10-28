package com.example.video_downloader_xxx.ui.fragment.browser.web

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.video_downloader_xxx.MainActivity
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.FragmentWebTabBinding
import com.example.video_downloader_xxx.service.VideoDownloadService
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.browser.DownloadViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class WebFragment : BaseFragment<FragmentWebTabBinding>() {
    private val sharedVM: SharedViewModel by activityViewModels()
    private lateinit var downloadViewModel: DownloadViewModel
    private lateinit var downloadService: VideoDownloadService
    private var serviceBound = false
    private lateinit var webView: WebView
    private var url: String? = null
    private val detectedVideos = mutableListOf<VideoInfo>()

    private val args by navArgs<WebFragmentArgs>()

    private val urlLink by lazy {
        args.url
    }

    override fun initView() {
        webView = binding?.webViewContainer ?: return

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        sharedVM.searchQuery.observe(viewLifecycleOwner) {
            Log.i("webFragment_ttdat", "initView: $it")
            val url = if (it.startsWith("http")) it
            else "https://www.google.com/search?q=${it.replace(" ", "+")}"
            webView.loadUrl(url)
        }

        binding?.ivGoBack?.setOnClickListener{
            (requireActivity() as MainActivity).binding.viewPager2.currentItem = 0
        }

//        setupWebView()
//        observeDownloadEvents()
    }

    private fun setupWebView() {
//        webView = binding?.webViewContainer ?: return
//        webView.settings.apply {
//            javaScriptEnabled = true
//            domStorageEnabled = true
//            loadWithOverviewMode = true
//            useWideViewPort = true
//        }
//
//        webView.webViewClient = WebViewClient { onVideoDetected(it) }
//
//        webView.webChromeClient = object : WebChromeClient() {
//            override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                binding?.progressBar?.apply {
//                    progress = newProgress
//                    visibility = if (newProgress < 100) View.VISIBLE else View.GONE
//                }
//            }
//        }
    }

//    private fun onVideoDetected(videoInfo: VideoInfo){
//        detectedVideos.add(videoInfo)
//        showVideoDetectedDialog(videoInfo)
//    }

//    private fun showVideoDetectedDialog(videoInfo: VideoInfo){
//        AlertDialog.Builder(requireContext())
//            .setTitle("Video Detected")
//            .setMessage("Found: ${videoInfo.title}")
//            .setPositiveButton("Download") { dialog, _ ->
//                startDownload(videoInfo)
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }

//    private fun startDownload(videoInfo: VideoInfo){
//        if(serviceBound){
//            downloadService.startDownload(videoInfo)
//        }
//        Toast.makeText(requireContext(), "Download started!", Toast.LENGTH_SHORT).show()
//
//    }

//    private fun observeDownloadEvents(){
//        downloadViewModel.downloadVideoEvent.observe(viewLifecycleOwner) { videoInfo ->
//            if(serviceBound){
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