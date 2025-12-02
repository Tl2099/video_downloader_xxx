package com.example.video_downloader_xxx.ui.fragment.library.complete

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.data.DataExt
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.FragmentCompleteBinding
import com.example.video_downloader_xxx.ui.activity.PlayerActivity
import com.example.video_downloader_xxx.ui.fragment.library.LibraryViewModel
import com.example.video_downloader_xxx.util.TextHelper.extractExtension
import com.example.video_downloader_xxx.util.TextHelper.sanitizeFileName
import com.example.video_downloader_xxx.util.TextHelper.validateName
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import androidx.core.graphics.drawable.toDrawable
import com.teh.software.tehads.base.BaseFragment

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
                binding?.imgEmpty?.isVisible = it.isEmpty()
            }.launchIn(lifecycleScope)
    }

    override fun initListener() {
        adapter.onItemClick = { video ->
            Log.i(TAG, "initListener: localPath=${video.localPath} videoUrl=${video.videoUrl}")

            val onlinePath = video.videoUrl ?: video.sourceUrl

            DataExt.pathVideoUrl = onlinePath
            DataExt.pathLocalVideo = video.localPath ?: ""

            video.sourceUrl.let { src ->
                DataExt.pathSourceUrl = src
                Log.i(TAG, "initListener: sourceUrl=$src")
            }

            startActivity(Intent(requireContext(), PlayerActivity::class.java))
        }


        adapter.onMoreClick = { video ->
            val sheet = CompleteBottomSheet.newInstance(
                onShare = {
                    video.localPath?.let { path ->
                        shareVideo(requireContext(), path)
                    }
                },
                onRename = {
                    showRenameDialog(video)
                },
                onDelete = {
                    showDeleteDialog(video)
                }
            )
            sheet.setVideo(video)
            sheet.show(parentFragmentManager, "DownloadSheet")
        }
    }

    private fun showDeleteDialog(video: VideoInfo){
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete, null)

        val btnCancel = dialogView.findViewById<AppCompatButton>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<AppCompatButton>(R.id.btnDelete)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            deleteFileAndRecord(video)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteFileAndRecord(video: VideoInfo) {
        video.localPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                val deleted = file.delete()
                Log.i("Delete", "File deleted: $deleted at $path")
            }
        }

        library.delete(video)
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

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnRename.setOnClickListener {
            val newName = edtName.text.toString().trim()
            val ok = validateName(newName, edtName)
            if (!ok) return@setOnClickListener

            val ext = extractExtension(video.videoUrl ?: video.sourceUrl)
            val clean = sanitizeFileName(newName.removeSuffix(".$ext"))
            val finalName = if (ext.isNotEmpty()) "$clean.$ext" else clean

            library.rename(video, finalName)

            dialog.dismiss()
        }
        dialog.show()
    }

    fun shareVideo(context: Context, videoPath: String) {
        val file = File(videoPath)

        if (!file.exists()) {
            //Toast.makeText(context, "File không tồn tại!", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "video/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(Intent.createChooser(intent, "Chia sẻ video qua..."))
    }


    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentCompleteBinding =
        FragmentCompleteBinding.inflate(layoutInflater)

    companion object {
        const val TAG = "CompleteFragment"
    }
}