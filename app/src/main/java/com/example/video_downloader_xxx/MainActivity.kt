package com.example.video_downloader_xxx

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.example.video_downloader_xxx.databinding.ActivityMainBinding
import com.example.video_downloader_xxx.ui.MainPagerAdapter
import com.example.video_downloader_xxx.ui.base.BaseActivity
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val sharedVM: SharedViewModel by viewModel()
    private lateinit var pagerAdapter: MainPagerAdapter

    override fun initView() {
        setupViewPager()
        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.browserFragment -> binding.viewPager2.currentItem = 0
                R.id.webFragment -> binding.viewPager2.currentItem = 1
                R.id.progressFragment -> binding.viewPager2.currentItem = 2
                R.id.listVideoFragment -> binding.viewPager2.currentItem = 3
            }
            true
        }

        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bottomNav.menu[position].isChecked = true
            }
        })
    }

    private fun setupViewPager() {
        pagerAdapter = MainPagerAdapter(this)
        binding.viewPager2.adapter = pagerAdapter
        //binding.viewPager2.isUserInputEnabled = false
    }

    fun switchToBrowserTab(query: String){
        sharedVM.setSearchQuery(query)
        binding.viewPager2.currentItem = 1
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun viewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

}