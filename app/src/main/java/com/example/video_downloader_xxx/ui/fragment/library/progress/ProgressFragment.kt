package com.example.video_downloader_xxx.ui.fragment.library.progress

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.data.repository.library.DownloadRepository
import com.example.video_downloader_xxx.databinding.FragmentProgressBinding
import com.example.video_downloader_xxx.service.VideoDownloadService
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.library.LibraryViewModel
import com.example.video_downloader_xxx.ui.fragment.library.complete.CompleteFragment
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ProgressFragment : BaseFragment<FragmentProgressBinding>() {

    private val library: LibraryViewModel by activityViewModel()
    private val adapter: VideoLoadingAdapter by lazy { VideoLoadingAdapter() }

    private var downloadService: VideoDownloadService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as VideoDownloadService.DownloadBinder
            downloadService = binder.getService()
            serviceBound = true
            Log.d(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            downloadService = null
            serviceBound = false
            Log.d(TAG, "Service disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindDownloadService()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            requireContext().unbindService(serviceConnection)
            serviceBound = false
        }
    }

    private fun bindDownloadService() {
        val intent = Intent(requireContext(), VideoDownloadService::class.java)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun initView() {
    }

    override fun initData() {
        binding?.recycleViewListVideoProgress?.adapter = adapter
        library.downloadingVideos
            .filterNotNull().onEach {
                for (i in it) {
                    Log.i(TAG, "=== Info download ===")
                    Log.i(TAG, "Title: ${i.title}")
                    Log.i(TAG, "ID: ${i.id}")
                    Log.i(TAG, "VideoURL: ${i.videoUrl}")
                    Log.i(TAG, "Duration: ${i.duration}")
                    Log.i(TAG, "SourceURL: ${i.sourceUrl}")
                    Log.i(TAG, "Active jobs: ${i.fileSize}")
                    Log.i(TAG, "localPath:${i.localPath}")
                }
                adapter.addData(it)
            }.launchIn(lifecycleScope)
    }

    override fun initListener() {
        adapter.onCloseClick = {
            Log.i(TAG, "Close clicked for: ${it.title} (ID: ${it.id})")
            showCancelConfirmDialog(it)
        }
    }

    private fun showCancelConfirmDialog(video: VideoInfo) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hủy tải xuống?")
            .setMessage("Bạn có chắc muốn hủy tải \"${video.title}\"?")
            .setPositiveButton("Hủy tải") { dialog, _ ->
                cancelDownload(video)
                dialog.dismiss()
            }
            .setNegativeButton("Tiếp tục tải", null)
            .show()
    }

    private fun cancelDownload(video: VideoInfo) {
        if (serviceBound && downloadService != null) {
            downloadService?.cancelDownload(video.id)
            Log.i(TAG, "Cancel request sent to service")

            Toast.makeText(
                requireContext(),
                "Đã hủy: ${video.title}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Log.w(TAG, "Service not bound, cannot cancel download")
            DownloadRepository.removeDownloading(video.id)
        }
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentProgressBinding =
        FragmentProgressBinding.inflate(layoutInflater)

    companion object {
        const val TAG = "ProgressFragment"
    }
}