package com.example.video_downloader_xxx.ui.fragment.browser.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.DataExt
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.BottomSheetDownloadLayoutBinding
import com.example.video_downloader_xxx.ui.activity.PlayerActivity
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.home.adapter.DownloadUrlVideoAdapter
import com.example.video_downloader_xxx.util.TextHelper.extractExtension
import com.example.video_downloader_xxx.util.TextHelper.sanitizeFileName
import com.example.video_downloader_xxx.util.TextHelper.validateName
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class DownloadUrlVideoBottomSheet() : BottomSheetDialogFragment() {
    private var _binding: BottomSheetDownloadLayoutBinding? = null
    private val binding get() = _binding!!

    private val adapter: DownloadUrlVideoAdapter by lazy { DownloadUrlVideoAdapter() }
    private val downloadViewModel: SharedViewModel by activityViewModel()

    private var onDownload: ((VideoInfo) -> Unit)? = null
    private var onClose: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setCanceledOnTouchOutside(false)
        isCancelable = false

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
        (dialog as? BottomSheetDialog)?.let { d ->
            d.setCanceledOnTouchOutside(false)
            d.behavior.apply {
                isDraggable = false
                skipCollapsed = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetDownloadLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Current video list size: ${downloadViewModel.videoList.value.size}")
        setupRecyclerView()
        setupDownloadButton()
    }

    private fun setupRecyclerView() {
        binding.recycleViewListVideoDownload.adapter = adapter
        binding.recycleViewListVideoDownload.itemAnimator = null

        val currentVideos = downloadViewModel.videoList.value
        if (currentVideos.isNotEmpty()) {
            Log.d(TAG, "Setting initial data: ${currentVideos.size} videos")
            adapter.addData(currentVideos)
        }

        downloadViewModel.videoList
            .filterNotNull()
            .onEach { it ->
                Log.i(TAG, "setupRecyclerView: called")
                for (i in it) {
                    Log.i(TAG, "setupRecyclerView: ${i.videoUrl}")
                }
                adapter.addData(it)
            }.launchIn(lifecycleScope)

        adapter.onToggleSelect = { downloadViewModel.toggleSelect(it) }
        adapter.onRenameClick = {
            showRenameDialog(it)
        }
        adapter.onClick = { video ->
            Log.i(TAG, "adapter.onClick video: $video")
            video.videoUrl?.let { path ->
                Log.i(TAG, "adapter.onClick path: $path")
                DataExt.pathVideoUrl = path
                startActivity(Intent(requireContext(), PlayerActivity::class.java))
            }
        }
    }

    private fun setupDownloadButton() {
        binding.btnDownload.setOnClickListener {
            val listVideoSelected = downloadViewModel.getSelectedVideos()
            if (listVideoSelected.isEmpty()) {
                //Toast.makeText(requireContext(), "Chưa chọn video nào", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                listVideoSelected.forEach {
                    Log.i(TAG, "setupDownloadButton: ${it.videoUrl}")
                    onDownload?.invoke(it)
                }
            }
            dismissAllowingStateLoss()
        }
        binding.btnClose.setOnClickListener {
            Log.d("BottomSheet", "Close clicked")
            onClose?.invoke()
            dismissAllowingStateLoss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showRenameDialog(video: VideoInfo) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rename, null)

        val edtName = dialogView.findViewById<EditText>(R.id.edtName)
        val btnCancel = dialogView.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnRename = dialogView.findViewById<AppCompatButton>(R.id.btnRename)

        edtName.setText(video.title)
        edtName.setSelection(video.title.length)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnRename.setOnClickListener {
            val newName = edtName.text.toString().trim()
            val ok = validateName(newName, edtName)
            if (!ok) return@setOnClickListener

            val ext = extractExtension(video.videoUrl ?: video.sourceUrl)
            val clean = sanitizeFileName(newName.removeSuffix(".$ext"))
            val finalName = if (ext.isNotEmpty()) "$clean.$ext" else clean

            downloadViewModel.rename(video, finalName)

            dialog.dismiss()
        }
        dialog.show()
    }

    companion object {
        const val TAG = "DownloadUrlVideoBottomSheet"

        fun newInstance(
            onDownload: (VideoInfo) -> Unit,
            onClose: (() -> Unit)? = null
        ): DownloadUrlVideoBottomSheet {
            return DownloadUrlVideoBottomSheet().apply {
                this.onDownload = onDownload
                this.onClose = onClose
            }
        }

        fun newInstance(
            videos: MutableList<VideoInfo>,
            onDownload: (VideoInfo) -> Unit,
            onClose: (() -> Unit)? = null
        ): DownloadUrlVideoBottomSheet {
            return DownloadUrlVideoBottomSheet().apply {
                this.onDownload = onDownload
                this.onClose = onClose
            }
        }
    }

}