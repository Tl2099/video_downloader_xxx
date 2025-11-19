package com.example.video_downloader_xxx.ui.fragment.library.complete

import android.content.Intent
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.video_downloader_xxx.data.DataExt
import com.example.video_downloader_xxx.data.DataExt.indexPos
import com.example.video_downloader_xxx.data.DataExt.listUrl
import com.example.video_downloader_xxx.databinding.FragmentCompleteBinding
import com.example.video_downloader_xxx.service.VideoDownloadService
import com.example.video_downloader_xxx.ui.activity.PlayerActivity
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.example.video_downloader_xxx.ui.fragment.browser.home.BrowserHomeFragment
import com.example.video_downloader_xxx.ui.fragment.browser.home.DownloadUrlVideoBottomSheet
import com.example.video_downloader_xxx.ui.fragment.library.LibraryViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CompleteFragment : BaseFragment<FragmentCompleteBinding>() {
    private val library: LibraryViewModel by activityViewModel()
    private val adapter: VideoCompleteAdapter by lazy { VideoCompleteAdapter() }

    override fun initView() {
    }

    override fun initData() {
        binding?.recycleViewListVideoProgress?.adapter = adapter
        library.videos
            .filterNotNull()
            .onEach {
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
        adapter.onItemClick = { video ->
            Log.i(TAG, "initListener: ${video.localPath} ${DataExt.pathVideoUrl}")
            video.videoUrl?.let { path ->
                DataExt.pathVideoUrl = path
                Log.i(TAG, "initListener: $path ${DataExt.pathVideoUrl}")
            }
            video.localPath?.let { path ->
                DataExt.pathLocalVideo = path
                Log.i(TAG, "initListener: $path ${DataExt.pathLocalVideo}")
            }
            startActivity(Intent(requireContext(), PlayerActivity::class.java))
        }

        adapter.onMoreClick = { video ->
            val sheet = CompleteBottomSheet.newInstance(
                onShare = {

                },
                onRename = {

                },
                onDelete = {

                }
            )
            sheet.setVideo(video)
            sheet.show(parentFragmentManager, "DownloadSheet")
        }

        binding?.btnDeleteAll?.setOnClickListener {
            library.deleteAll()
        }
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentCompleteBinding =
        FragmentCompleteBinding.inflate(layoutInflater)

    companion object {
        const val TAG = "CompleteFragment"
    }
}