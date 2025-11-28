package com.example.video_downloader_xxx.ui.fragment.browser.setting

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.databinding.FragmentSettingBinding
import com.example.video_downloader_xxx.util.openFeedbackEmail
import com.teh.software.tehads.base.BaseFragment
import com.teh.software.tehads.format.AppOpenHelper
import com.teh.software.tehads.utils.shareApp

class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
        binding?.btnHowToDownload?.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_howToDownloadFragment)
        }
        binding?.btnBack?.setOnClickListener {
            findNavController().popBackStack()
        }
        binding?.btnShare?.setOnClickListener {
            requireActivity().shareApp(getString(R.string.app_name))
        }
        binding?.btnPolicy?.setOnClickListener {
            AppOpenHelper.instance.disableOneTime()
            val intent = Intent(Intent.ACTION_VIEW, getString(R.string.link_policy).toUri())
            startActivity(intent)
        }
        binding?.btnFeedback?.setOnClickListener {
            requireContext().openFeedbackEmail()
        }
    }

    override fun reloadAds() {
    }

    override fun getViewBinding(): FragmentSettingBinding =
        FragmentSettingBinding.inflate(layoutInflater)
}