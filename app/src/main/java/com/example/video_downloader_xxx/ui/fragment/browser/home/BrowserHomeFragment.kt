package com.example.video_downloader_xxx.ui.fragment.browser.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.video_downloader_xxx.MainActivity
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.databinding.FragmentBrowserBinding
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.browser.home.DownloadViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.util.DownloadState
import com.example.video_downloader_xxx.util.FileHelper
import com.example.video_downloader_xxx.util.FileHelper.isValidUrl
import com.example.video_downloader_xxx.util.hideKeyboard
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import kotlin.math.log

class BrowserHomeFragment : BaseFragment<FragmentBrowserBinding>() {

    private val downloadViewModel: DownloadViewModel by viewModel()
    private val sharedVM: SharedViewModel by viewModel()
    private var pendingUrl: String? = null

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