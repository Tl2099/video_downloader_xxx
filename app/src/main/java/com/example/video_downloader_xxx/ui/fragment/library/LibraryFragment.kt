package com.example.video_downloader_xxx.ui.fragment.library

import androidx.viewpager2.widget.ViewPager2
import com.example.video_downloader_xxx.databinding.FragmentLibraryBinding
import com.example.video_downloader_xxx.ui.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LibraryFragment: BaseFragment<FragmentLibraryBinding>() {
    private val adapter by lazy { ViewPagerAdapter(this) }
    private var mediator: TabLayoutMediator? = null

    override fun initView() {
        binding?.let { b ->
            b.viewPager.adapter = adapter
            mediator = TabLayoutMediator(b.tabLayout, b.viewPager) {tab, pos ->
                tab.text = if(pos == 0) "Progress" else "Download"
            }.apply { attach()}
        }

    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun reloadAds() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
    }

    override fun getViewBinding(): FragmentLibraryBinding = FragmentLibraryBinding.inflate(layoutInflater)
}