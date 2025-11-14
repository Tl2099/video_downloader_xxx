package com.example.video_downloader_xxx.ui.fragment.browser.home

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.video_downloader_xxx.BuildConfig
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.databinding.FragmentBrowserBinding
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.browser.home.DownloadUrlVideoBottomSheet
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.home.adapter.EndMarginDecoration
import com.example.video_downloader_xxx.ui.fragment.browser.home.adapter.SocialAdapter
import com.example.video_downloader_xxx.util.DownloadState
import com.example.video_downloader_xxx.util.FileHelper
import com.example.video_downloader_xxx.util.FileHelper.isValidUrl
import com.example.video_downloader_xxx.util.hideKeyboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.getValue

class BrowserHomeFragment : BaseFragment<FragmentBrowserBinding>() {

    companion object {
        const val TAG = "BrowserHomeFragment"
    }
    private val downloadViewModel: SharedViewModel by activityViewModel()
    private var pendingUrl: String? = null
    private lateinit var clipboardManager: ClipboardManager

    private val socialAdapter by lazy {
        SocialAdapter(emptyList()) { social ->
            downloadViaSearch(social.websiteUrl)
        }
    }

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        checkDataInClipBoard()
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
        Log.d("SonLN", "onCreateDialog: $downloadViewModel")

        binding?.apply {
            progressBar.isIndeterminate = false
            progressBar.max = 100
        }

    }

    override fun initData() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(100)
            downloadViewModel.onFindVideoDone
                .onEach {
                    val sheet = DownloadUrlVideoBottomSheet.newInstance(
                        onDownload = {
                            val outFile = FileHelper.createVideoFile(requireContext())
                            downloadViewModel.downloadVideo(it, outFile)
                        },
                        onClose = {
                            //downloadViewModel.clearDetectedVideos()
                        }
                    )
                    sheet.show(parentFragmentManager, "DownloadSheet")
                }
                .launchIn(lifecycleScope)
        }
    }

    override fun initListener() {
        val spacing = resources.getDimensionPixelSize(R.dimen.dp_12)
        binding?.apply {
            recycleViewListRecentlyWeb.addItemDecoration(EndMarginDecoration(spacing))
            recycleViewListSocialMedia.addItemDecoration(EndMarginDecoration(spacing))
            recycleViewListSocialMedia.adapter = socialAdapter
            recycleViewListRecentlyWeb.adapter = socialAdapter
        }
        observeSocialData()
        observeDownloadState()
        setupUrlWatcher()

        clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(clipListener)
        checkDataInClipBoard()

        val clipData = clipboardManager.primaryClip
        Log.i(TAG, "initListener1: $clipData")

        binding?.icPaste?.setOnClickListener {
            val clipData = clipboardManager.primaryClip
            Log.i(TAG, "initListener2: $clipData")
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString()
                if (!text.isNullOrEmpty()) {
                    binding?.edtUrl?.setText(text)
                    binding?.edtUrl?.setSelection(text.length)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Không có dữ liệu trong clipboard",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding?.run {

            if (BuildConfig.DEBUG) {
                edtUrl.setText("xem phim")
                btnSearch.isSelected = true
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
            downloadViewModel.clearDetectedVideos()
        }
    }

    private fun observeSocialData() {
        viewLifecycleOwner.lifecycleScope.launch {
            downloadViewModel.social.collectLatest { socialList ->
                socialAdapter.updateData(socialList.take(7))
            }
        }
    }

    private fun setupUrlWatcher() {
        binding?.apply {
            edtUrl.addTextChangedListener { text ->
                val hasText = !text.isNullOrEmpty()
                btnSearch.isSelected = hasText
            }
        }
    }

    private fun checkDataInClipBoard() {
        val clip = clipboardManager.primaryClip
        val hasData = if (clip != null && clip.itemCount > 0) {
            val item = clip.getItemAt(0)
            val text = item.text?.toString()
            val uri = item.uri?.toString()
            val intent = item.intent?.toUri(Intent.URI_INTENT_SCHEME)
            !text.isNullOrEmpty() || !uri.isNullOrEmpty() || !intent.isNullOrEmpty()
        } else {
            false
        }

        Log.d(
            "ClipboardCheck",
            "hasData=$hasData, clip=${clipboardManager.primaryClip?.getItemAt(0)}"
        )
        binding?.icPaste?.isSelected = hasData
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

    override fun onResume() {
        super.onResume()
        binding?.root?.post {
            checkDataInClipBoard()
        }
        //checkDataInClipBoard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clipboardManager.removePrimaryClipChangedListener(clipListener)
    }

    override fun reloadAds() {
    }

    private fun startDownload(url: String) {
        downloadViewModel.fetchVideoInfo(url)
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