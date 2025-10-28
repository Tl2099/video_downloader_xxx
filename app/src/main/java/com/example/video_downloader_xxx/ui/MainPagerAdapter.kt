package com.example.video_downloader_xxx.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.video_downloader_xxx.ui.fragment.browser.home.BrowserHomeFragment
import com.example.video_downloader_xxx.ui.fragment.browser.web.WebFragment
import com.example.video_downloader_xxx.ui.fragment.list_video.ListVideoFragment
import com.example.video_downloader_xxx.ui.fragment.progress.ProgressFragment

class MainPagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> BrowserHomeFragment()
            1 -> WebFragment()
            2 -> ProgressFragment()
            3 -> ListVideoFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

    override fun getItemCount(): Int = 4
}