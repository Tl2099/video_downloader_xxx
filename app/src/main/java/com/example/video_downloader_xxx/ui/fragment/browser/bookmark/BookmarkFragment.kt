package com.example.video_downloader_xxx.ui.fragment.browser.bookmark

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.databinding.FragmentBookmarkBinding
import com.example.video_downloader_xxx.ui.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.getValue

class BookmarkFragment : BaseFragment<FragmentBookmarkBinding>() {

    private val bookmarkVM: BookmarkViewModel by activityViewModel()
    private val adapter: BookmarkAdapter by lazy { BookmarkAdapter() }

    override fun initView() {
    }

    override fun initData() {
        bookmarkVM.loadBookmarks()
        binding?.recycleViewBookmark?.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            bookmarkVM.bookmarks.collectLatest {
                adapter.addData(it)
            }
        }
        adapter.onItemClick = {
            val action = BookmarkFragmentDirections.actionBookmarkFragmentToWebFragment(it.url)
            findNavController().navigate(action)
        }
    }

    override fun initListener() {
        binding?.btnDelete?.setOnClickListener {
            showDeleteDialog()
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().popBackStack()
            //findNavController().navigate(R.id.action_bookmarkFragment_to_browserFragment)
        }

        adapter.onDeleteClick = {
            bookmarkVM.delete(it)
        }

        adapter.onShareClick = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, it.url)
            }
            startActivity(Intent.createChooser(intent, "Share link"))
        }

        adapter.onCopyLinkClick = {
            val cm = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("link", it.url))
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_clear_history, null)

        val btnNo = dialogView.findViewById<AppCompatButton>(R.id.btnNo)
        val btnYes = dialogView.findViewById<AppCompatButton>(R.id.btnYes)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvContent = dialogView.findViewById<TextView>(R.id.tvMessage)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        tvTitle.text = getString(R.string.txt_title_clear_bookmark)
        tvContent.text = getString(R.string.txt_content_clear_bookmark)

        btnNo.setOnClickListener { dialog.dismiss() }

        btnYes.setOnClickListener {
            bookmarkVM.clearAll()
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentBookmarkBinding =
        FragmentBookmarkBinding.inflate(layoutInflater)

    companion object {
        const val TAG = "BookmarkFragment"
    }
}