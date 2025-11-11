package com.example.video_downloader_xxx.ui.fragment.library

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.video_downloader_xxx.ui.fragment.library.complete.CompleteFragment
import com.example.video_downloader_xxx.ui.fragment.library.progress.ProgressFragment

class ViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> ProgressFragment()
            else -> CompleteFragment()
        }
    }

    override fun getItemCount(): Int = 2
}