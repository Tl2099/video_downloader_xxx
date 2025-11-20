package com.example.video_downloader_xxx.ui.fragment.browser.home

import android.Manifest
import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.FragmentBrowserBinding
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
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.home.adapter.EndMarginDecoration
import com.example.video_downloader_xxx.ui.fragment.browser.home.adapter.SocialAdapter
import com.example.video_downloader_xxx.ui.fragment.browser.web.WebFragment
import com.example.video_downloader_xxx.util.DownloadState
import com.example.video_downloader_xxx.util.FileHelper.isValidUrl
import com.example.video_downloader_xxx.util.TextHelper.isYouTubeUrl
import com.example.video_downloader_xxx.util.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class BrowserHomeFragment : BaseFragment<FragmentBrowserBinding>() {
    private val downloadViewModel: SharedViewModel by activityViewModel()
    private var pendingUrl: String? = null
    private lateinit var clipboardManager: ClipboardManager
    private var analyzeTimeoutJob: Job? = null
    private val analyzeTimeoutMs = 100_000L
    private var hasShownAddedDialog = false

    private val socialAdapter by lazy {
        SocialAdapter(emptyList()) { social ->
            downloadViaSearch(social.websiteUrl)
        }
    }

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        checkDataInClipBoard()
    }

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
            Log.w(TAG, "Service was not bound, cannot unbind: ${e.message}")
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
//            Toast.makeText(
//                requireContext(),
//                "Permission denied. Cannot download videos.",
//                Toast.LENGTH_LONG
//            ).show()
        }
    }

    override fun initView() {
        Log.d("SonLN", "onCreateDialog: $downloadViewModel")

//        binding?.apply {
//            progressBar.isIndeterminate = false
//            progressBar.max = 100
//        }
    }

    override fun initData() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(100)
            downloadViewModel.onFindVideoDone
                .onEach {
                    analyzeTimeoutJob?.cancel()
                    binding?.apply {
                        btnSearch.text = getString(R.string.txt_convert)
                        edtUrl.text.clear()
                        txtStatus.isVisible = false
                        btnClose.isVisible = false
                        loadingAnim.isVisible = false
                        loadingAnim.cancelAnimation()
                    }
                    val sheet = DownloadUrlVideoBottomSheet.newInstance(
                        onDownload = {
                            Log.i(TAG, "onDownload: ${it.videoUrl}")
                            showTaskAddedDialog(it.sourceUrl)
                            startDownload(it)
                        },
                        onClose = {

                        }
                    )
                    sheet.show(parentFragmentManager, "DownloadSheet")
                }
                .launchIn(lifecycleScope)
        }
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
//                Toast.makeText(
//                    requireContext(),
//                    "Không có dữ liệu trong clipboard",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
        }

//        binding?.run {
//            if (BuildConfig.DEBUG) {
//                edtUrl.setText("mixkit")
//                btnSearch.isSelected = true
//            }
//        }

        binding?.btnSearch?.setOnClickListener {
            hideKeyboard()
            val text = binding?.edtUrl?.text.toString().trim()

            if (text.isBlank()) {
//                Toast.makeText(requireContext(), "Please enter a URL", Toast.LENGTH_SHORT)
//                    .show()
                return@setOnClickListener
            }

            if (text.isYouTubeUrl()) {
                showInvalidLinkDialog(
                    "Cannot download YouTube video because\n" +
                            "it violates Google policy.",
                    onCancel = {}
                )
                return@setOnClickListener
            }

//            if (!text.isValidUrl()) {
//                showInvalidLinkDialog(
//                    "Invalid link, please try again!",
//                    onCancel = {}
//                )
//                return@setOnClickListener
//            }

            if (text.isValidUrl()) {
                Log.i(TAG, "btn url: $text")
                downloadViaUrl(text)
            } else {
                Log.i(TAG, "btn search: $text")
                downloadViaSearch(text)
            }

            binding?.txtStatus?.text = getString(R.string.txt_status_analyzing)
            downloadViewModel.clearDetectedVideos()
        }

        binding?.btnClose?.setOnClickListener {
            analyzeTimeoutJob?.cancel()
            analyzeTimeoutJob = viewLifecycleOwner.lifecycleScope.launch {
                downloadViewModel.cancelFetch()
                binding?.apply {
                    edtUrl.text.clear()
                    btnSearch.text = getString(R.string.txt_convert)
                    btnClose.isVisible = false
                    txtStatus.isVisible = false
                    loadingAnim.cancelAnimation()
                    loadingAnim.isVisible = false
                }
            }
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

    private fun showTaskAddedDialog(sourceUrl: String) {
        Log.i(WebFragment.Companion.TAG, "showTaskAddedDialog: called")
        if (hasShownAddedDialog) return
        hasShownAddedDialog = true

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_task_add_layout, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val btnView = view.findViewById<AppCompatTextView>(R.id.btnShowDownload)
        val btnClose = view.findViewById<AppCompatImageView>(R.id.btnClose)

        tvTitle.text = sourceUrl

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

    private fun observeDownloadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            downloadViewModel.downloadVideoState.collect { st ->
                when (st) {
                    is DownloadState.Idle -> {
                        Log.i("BrowserHomeFragment_ttdat", "DownloadState Idle ")
                        binding?.apply {
                            txtStatus.isVisible = false
                            //txtStatus.text = "Idle"
                            //progressBar.progress = 0
                        }
                    }

                    is DownloadState.Downloading -> {
                        Log.i("BrowserHomeFragment_ttdat", "DownloadState Downloading ")
                        binding?.apply {
                            txtStatus.isVisible = true
                            txtStatus.text = getString(R.string.txt_status_analyzing)

                            //txtStatus.text = "Downloading: ${st.progress}%"
                            //progressBar.progress = st.progress
                        }
                    }

                    is DownloadState.Success -> {
                        Log.i("BrowserHomeFragment_ttdat", "DownloadState Success ")
                        binding?.txtStatus?.isVisible = false
                        //binding?.txtStatus?.text = "Saved: ${st.file.absolutePath}"
                    }

                    is DownloadState.Error -> {
                        Log.i("BrowserHomeFragment_ttdat", "DownloadState Error ")
                        analyzeTimeoutJob?.cancel()
                        binding?.apply {
                            btnSearch.text = getString(R.string.txt_convert)
                            btnClose.isVisible = false
                            loadingAnim.cancelAnimation()
                            loadingAnim.isVisible = false
                        }
                        showInvalidLinkDialog(getString(R.string.txt_fetch_fail), onCancel = {})
                        Log.i(TAG, "DownloadState.Error: ${st.message}")
                        binding?.txtStatus?.isVisible = false
                        //binding?.txtStatus?.text = "Error: ${st.message}"
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
            binding?.edtUrl?.text?.clear()
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
        binding?.loadingAnim?.cancelAnimation()
    }

    override fun reloadAds() {
    }

    private fun startDownload(url: String) {
        binding?.apply {
            btnSearch.text = getString(R.string.txt_analyzing)
            btnClose.isVisible = true
            loadingAnim.playAnimation()
            loadingAnim.isVisible = true
            txtStatus.isVisible = true
        }
        downloadViewModel.fetchVideoInfo(url)
        analyzeTimeoutJob?.cancel()
        analyzeTimeoutJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(analyzeTimeoutMs)
            downloadViewModel.cancelFetch()
            showAnalyzeTimeout()
            showInvalidLinkDialog(getString(R.string.txt_so_long_time), onCancel = {})
        }
    }

    private fun showAnalyzeTimeout() {
        binding?.apply {
            btnSearch.text = getString(R.string.txt_convert)
            txtStatus.isVisible = false
            loadingAnim.cancelAnimation()
            loadingAnim.isVisible = false
            txtStatus.text = getString(R.string.txt_analyzing_timeout)
        }
//        Toast.makeText(requireContext(), "Phân tích quá lâu, vui lòng thử lại", Toast.LENGTH_SHORT)
//            .show()
    }

    private fun showInvalidLinkDialog(
        message: String,
        onCancel: (() -> Unit)? = null,
    ) {
        binding?.edtUrl?.text?.clear()
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_invalid_link, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        dialogView.findViewById<TextView>(R.id.tvMessage).text = message
        val btnCancel = dialogView.findViewById<AppCompatImageView>(R.id.btnClose)

        btnCancel.setOnClickListener {
            onCancel?.invoke()
            dialog.dismiss()
        }

        dialog.show()
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

    override fun onPause() {
        super.onPause()
        binding?.loadingAnim?.cancelAnimation()
    }

    companion object {
        const val TAG = "BrowserHomeFragment"
    }

}