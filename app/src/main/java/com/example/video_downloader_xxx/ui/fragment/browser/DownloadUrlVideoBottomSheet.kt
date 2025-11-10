package com.example.video_downloader_xxx.ui.fragment.browser

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.BottomSheetLayoutBinding
import com.example.video_downloader_xxx.util.TextHelper
import com.example.video_downloader_xxx.util.TextHelper.extractExtension
import com.example.video_downloader_xxx.util.TextHelper.sanitizeFileName
import com.example.video_downloader_xxx.util.TextHelper.validateName
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class DownloadUrlVideoBottomSheet() : BottomSheetDialogFragment() {
    private var _binding: BottomSheetLayoutBinding? = null
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
        _binding = BottomSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupDownloadButton()
    }

    private fun setupRecyclerView() {
        downloadViewModel.videoList
            .filterNotNull()
            .onEach { it ->
                Log.i(TAG, "setupRecyclerView: called")
                for(i in it){
                    Log.i(TAG, "setupRecyclerView: ${i.videoUrl}")
                }
                adapter.addData(it)
            }.launchIn(lifecycleScope)

        binding.recycleViewListVideoDownload.adapter = adapter
        adapter.onToggleSelect = { downloadViewModel.toggleSelect(it) }
        adapter.onRenameClick = {
            showRenameDialog(it)
        }
    }

    private fun setupDownloadButton() {
        binding.btnDownload.setOnClickListener {
            val listVideoSelected = downloadViewModel.getSelectedVideos()
            if (listVideoSelected.isEmpty()) {
                Toast.makeText(requireContext(), "Chưa chọn video nào", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                listVideoSelected.forEach { onDownload?.invoke(it) }
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
        val current =
            (video.title.ifBlank { TextHelper.guessNameFromUrl(video.sourceUrl) }).take(128)

        val inputLayout = TextInputLayout(requireContext()).apply {
            isHintEnabled = true
            hint = "New name"
            setPadding(24, 8, 24, 0)
        }
        val edit = TextInputEditText(requireContext()).apply {
            setText(current)
            setSelection(text?.length ?: 0)
            maxLines = 1
            isSingleLine = true
            setEms(24)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
        inputLayout.addView(edit)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.txt_rename)
            .setView(inputLayout)
            .setNegativeButton(R.string.txt_cancel_dialog, null)
            .setPositiveButton(R.string.txt_rename, null)
            .create()

        dialog.setOnShowListener {
            edit.post {
                edit.requestFocus()
                val imm =
                    requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                            as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(edit, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }

            val btn = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val raw = edit.text?.toString()?.trim().orEmpty()
                val ok = validateName(raw, inputLayout)
                if (!ok) return@setOnClickListener

                val ext = extractExtension(video.videoUrl ?: video.sourceUrl)
                val clean = sanitizeFileName(raw.removeSuffix(".$ext"))
                val finalName = if (ext.isNotEmpty()) "$clean.$ext" else clean

                downloadViewModel.rename(video, finalName)

                dialog.dismiss()
            }
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