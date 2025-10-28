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
import com.example.video_downloader_xxx.databinding.FragmentBrowserBinding
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.browser.DownloadViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.util.DownloadState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class BrowserHomeFragment : BaseFragment<FragmentBrowserBinding>() {

    private val downloadViewModel: DownloadViewModel by viewModel()
    private val sharedVM: SharedViewModel by viewModel()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            startDownload()
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

        binding?.btnSearch?.setOnClickListener {
            val isChecked = binding?.switchMode?.isChecked ?: false
            if (isChecked) {
                Log.i("BrowserHomeFragment_ttdat", "Url mode: ")
                val url = binding?.edtUrl?.text.toString().trim()
                if (url.isBlank()) {
                    Toast.makeText(requireContext(), "Please enter a URL", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                if (hasPermissions()) {
                    startDownload()
                } else {
                    requestPermissions()
                }
            } else {
                Log.i("BrowserHomeFragment_ttdat", "Web mode: ")
                val text = binding?.edtUrl?.text.toString()
                if (text.isNotEmpty()) {
                    (activity as MainActivity).switchToBrowserTab(text)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            downloadViewModel.downloadVideoState.collect { st ->
                when (st) {
                    is DownloadState.Idle -> {
                        binding?.apply {
                            txtStatus.text = "Idle"
                            progressBar.progress = 0
                        }
                    }

                    is DownloadState.Downloading -> {
                        binding?.apply {
                            txtStatus.text = "Downloading: ${st.progress}%"
                            progressBar.progress = st.progress
                        }
                    }

                    is DownloadState.Success -> {
                        binding?.txtStatus?.text = "Saved: ${st.file.absolutePath}"
                    }

                    is DownloadState.Error -> {
                        binding?.txtStatus?.text = "Error: ${st.message}"
                    }
                }
            }
        }
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun reloadAds() {
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

    private fun startDownload() {
        val url = binding?.edtUrl?.text.toString().trim()

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

        Toast.makeText(requireContext(), "Saving to: ${outFile.absolutePath}", Toast.LENGTH_LONG)
            .show()

        downloadViewModel.start(url, outFile)
    }

}