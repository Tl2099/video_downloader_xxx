package com.example.video_downloader_xxx.ui.fragment.browser.home

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.video_downloader_xxx.databinding.FragmentBrowserBinding
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.util.DownloadState
import com.example.video_downloader_xxx.util.FileHelper
import com.example.video_downloader_xxx.util.FileHelper.isValidUrl
import com.example.video_downloader_xxx.util.hideKeyboard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class BrowserHomeFragment : BaseFragment<FragmentBrowserBinding>() {

    private val downloadViewModel: BrowserViewModel by viewModel()
    private val sharedVM: SharedViewModel by viewModel()
    private var pendingUrl: String? = null

    private val socialAdapter by lazy {
        SocialAdapter(emptyList()) { social ->
            downloadViaSearch(social.websiteUrl)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            pendingUrl?.let { url ->
                startDownload(url)
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Permission denied. Cannot download videos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun initView() {
        binding?.apply {
            progressBar.isIndeterminate = false
            progressBar.max = 100
        }

    }

    override fun initData() {
    }

    override fun initListener() {
        binding?.recycleViewListSocialMedia?.adapter = socialAdapter
        binding?.recycleViewListRecentlyWeb?.adapter = socialAdapter
        observeSocialData()
        observeDownloadState()

        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        binding?.icPaste?.setOnClickListener {
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString()
                if (!text.isNullOrEmpty()) {
                    binding?.edtUrl?.setText(text)
                    binding?.edtUrl?.setSelection(text.length)
                }
            } else {
                Toast.makeText(requireContext(), "Không có dữ liệu trong clipboard", Toast.LENGTH_SHORT).show()
            }
        }


        binding?.btnSearch?.setOnClickListener {
            hideKeyboard()
            val text = binding?.edtUrl?.text.toString().trim()

            if (text.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a URL", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (text.isValidUrl()) {
                downloadViaUrl(text)
            } else {
                downloadViaSearch(text)
            }

            binding?.edtUrl?.text?.clear()
        }
    }

    private fun observeSocialData() {
        viewLifecycleOwner.lifecycleScope.launch {
            downloadViewModel.social.collectLatest { socialList ->
                socialAdapter.updateData(socialList.take(7))
            }
        }
    }

    private fun observeDownloadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            downloadViewModel.downloadVideoState.collect { st ->
                when (st) {
                    is DownloadState.Idle -> {
                        Log.i("BrowserHomeFragment_ttdat", "DownloadState Idle ")
                        binding?.apply {
                            txtStatus.text = "Idle"
                            progressBar.progress = 0
                        }
                    }

                    is DownloadState.Downloading -> {
                        Log.i("BrowserHomeFragment_ttdat", "DownloadState Downloading ")
                        binding?.apply {
                            txtStatus.text = "Downloading: ${st.progress}%"
                            progressBar.progress = st.progress
                        }
                    }

                    is DownloadState.Success -> {
                        Log.i("BrowserHomeFragment_ttdat", "DownloadState Success ")
                        binding?.txtStatus?.text = "Saved: ${st.file.absolutePath}"
                    }

                    is DownloadState.Error -> {
                        Log.i("BrowserHomeFragment_ttdat", "DownloadState Error ")
                        binding?.txtStatus?.text = "Error: ${st.message}"
                    }
                }
            }

        }
    }

    private fun downloadViaUrl(url: String) {
        Log.i("BrowserHomeFragment_ttdat", "Url mode: ")
        if (hasPermissions()) {
            startDownload(url)
        } else {
            pendingUrl = url
            requestPermissions()
        }
    }

    private fun downloadViaSearch(url: String) {
        Log.i("BrowserHomeFragment_ttdat", "Web mode: ")
        if (url.isNotEmpty()) {
            val action = BrowserHomeFragmentDirections.actionBrowserFragmentToWebFragment(url)
            findNavController().navigate(action)
        }
    }

    override fun reloadAds() {
    }

    private fun startDownload(url: String) {
        val outFile = FileHelper.createVideoFile(requireContext())

        Toast.makeText(requireContext(), "Saving to: ${outFile.absolutePath}", Toast.LENGTH_LONG)
            .show()

        //downloadViewModel.start(url, outFile)

        lifecycleScope.launch {
            downloadViewModel.fetchVideoInfo(url)
            downloadViewModel.videoInfo.collect { video ->
                if (video != null) {
                    val sheet = DownloadUrlVideoBottomSheet.newInstance(video) {
                        Log.i("BrowserHomeFragment_ttdat", "startDownload: $it")
                        downloadViewModel.downloadVideo(it, outFile)
                    }
                    Log.i("BrowserHomeFragment_ttdat", "Showing BottomSheet for: ${video.videoUrl}")
                    sheet.show(parentFragmentManager, "DownloadSheet")
                    return@collect
                }
            }
        }
    }

    override fun getViewBinding(): FragmentBrowserBinding =
        FragmentBrowserBinding.inflate(layoutInflater)

    private fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        requestPermissionLauncher.launch(permissions)
    }

}