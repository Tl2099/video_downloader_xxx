package com.example.video_downloader_xxx.ui.fragment.library.complete

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.BottomSheetCompleteLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CompleteBottomSheet() : BottomSheetDialogFragment() {
    private var _binding: BottomSheetCompleteLayoutBinding? = null
    private val binding get() = _binding!!

    private var onShare: ((VideoInfo) -> Unit)? = null
    private var onRename: ((VideoInfo) -> Unit)? = null
    private var onDelete: ((VideoInfo) -> Unit)? = null

    private var currentVideo: VideoInfo? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setCanceledOnTouchOutside(true)
        isCancelable = true

        dialog.setOnShowListener {
            val behavior = dialog.behavior
            behavior.isDraggable = false
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window ?: return

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        WindowInsetsControllerCompat(window, window.decorView)
            .hide(WindowInsetsCompat.Type.navigationBars())
    }

    fun setVideo(video: VideoInfo) {
        currentVideo = video
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetCompleteLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupDownloadButton()
    }

    private fun setupRecyclerView() {

    }

    private fun setupDownloadButton() {
        binding.tvTitle.text = currentVideo?.title

        binding.btnShare.setOnClickListener {
            currentVideo?.let { video ->
                onShare?.invoke(video)
                dismissAllowingStateLoss()
            }
        }

        binding.btnRename.setOnClickListener {
            currentVideo?.let { video ->
                onRename?.invoke(video)
                dismissAllowingStateLoss()
            }
        }

        binding.btnDelete.setOnClickListener {
            currentVideo?.let { video ->
                onDelete?.invoke(video)
                dismissAllowingStateLoss()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DownloadUrlVideoBottomSheet"

        fun newInstance(
            onShare: (VideoInfo) -> Unit,
            onRename: (VideoInfo) -> Unit,
            onDelete: (VideoInfo) -> Unit,
        ): CompleteBottomSheet {
            return CompleteBottomSheet().apply {
                this.onShare = onShare
                this.onRename = onRename
                this.onDelete = onDelete
            }
        }
    }

}