package com.example.video_downloader_xxx.ui.fragment.browser.history

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.databinding.FragmentHistoryBinding
import com.teh.software.tehads.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class WebHistoryFragment : BaseFragment<FragmentHistoryBinding>() {
    private val history: WebsiteHistoryViewModel by activityViewModel()
    private val adapter: WebsiteHistoryAdapter by lazy { WebsiteHistoryAdapter() }

    override fun initView() {
    }

    override fun initData() {
        binding?.recycleViewHistory?.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            history.history.collectLatest {
                adapter.addData(it)
            }
        }
        adapter.onItemClick = {
            val action = WebHistoryFragmentDirections.actionHistoryFragmentToWebFragment(it.url)
            findNavController().navigate(action)
        }
    }

    override fun initListener() {
        binding?.btnDelete?.setOnClickListener {
            showDeleteDialog()
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().popBackStack()
            //findNavController().navigate(R.id.action_historyFragment_to_browserFragment)
        }

        adapter.onDeleteClick = {
            history.delete(it.url)
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

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        btnNo.setOnClickListener { dialog.dismiss() }

        btnYes.setOnClickListener {
            history.clearAll()
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentHistoryBinding =
        FragmentHistoryBinding.inflate(layoutInflater)

    companion object {
        const val TAG = "WebHistoryFragment"
    }
}