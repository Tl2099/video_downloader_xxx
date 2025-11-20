package com.example.video_downloader_xxx.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.databinding.DialogRenameBinding
import com.example.video_downloader_xxx.util.TextHelper

class RenameDialog(
    context: Context,
    private val currentName: String,
    private val video: VideoInfo?,
    private val onRename: (String) -> Unit
) : AlertDialog(context) {
    private lateinit var binding: DialogRenameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogRenameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        binding.edtName.setText(currentName)
        binding.edtName.setSelection(currentName.length)

        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnRename.setOnClickListener {
            val newName = binding.edtName.text.toString().trim()

            if (!TextHelper.validateName(newName, binding.edtName))
                return@setOnClickListener

            val ext = TextHelper.extractExtension(video?.videoUrl ?: video?.sourceUrl)
            val clean = TextHelper.sanitizeFileName(newName.removeSuffix(".$ext"))

            val finalName = if (ext.isNotEmpty()) "$clean.$ext" else clean

            onRename(finalName)
            dismiss()
        }
        binding.edtName.requestFocus()
    }

    override fun show() {
        super.show()
        binding.edtName.post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.edtName, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}