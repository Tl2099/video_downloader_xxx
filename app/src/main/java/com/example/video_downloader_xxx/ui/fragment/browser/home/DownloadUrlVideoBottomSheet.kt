package com.example.video_downloader_xxx.ui.fragment.browser.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DownloadUrlVideoBottomSheet() : BottomSheetDialogFragment() {
    private var _binding: BottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    private var videoList: MutableList<VideoInfo> = mutableListOf()
    private var onDownload: ((VideoInfo) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupDownloadButton()
    }

    private fun setupRecyclerView() {
        val adapter = DownloadUrlVideoAdapter(videoList) { videoInfo ->
            Log.i("DownloadUrlVideoBottomSheet", "setupRecyclerView: Called")
            onDownload?.invoke(videoInfo)
            dismiss()
        }
        binding.recycleViewListVideoDownload.adapter = adapter
    }

    private fun setupDownloadButton() {
        binding.btnDownload.setOnClickListener {
            val firstVideo = videoList.firstOrNull() ?: return@setOnClickListener
            onDownload?.invoke(firstVideo)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        fun newInstance(
            video: VideoInfo,
            onDownload: (VideoInfo) -> Unit
        ): DownloadUrlVideoBottomSheet {
            return DownloadUrlVideoBottomSheet().apply {
                this.videoList = mutableListOf(video)
                this.onDownload = onDownload
            }
        }

        fun newInstance(
            videos: MutableList<VideoInfo>,
            onDownload: (VideoInfo) -> Unit
        ): DownloadUrlVideoBottomSheet {
            return DownloadUrlVideoBottomSheet().apply {
                this.videoList = videos
                this.onDownload = onDownload
            }
        }
    }

}