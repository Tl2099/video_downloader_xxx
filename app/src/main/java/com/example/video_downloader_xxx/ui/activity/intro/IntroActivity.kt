package com.example.video_downloader_xxx.ui.activity.intro

import android.content.Intent
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.databinding.ActivityIntroBinding
import com.example.video_downloader_xxx.ui.activity.MainActivity
import com.example.video_downloader_xxx.ui.base.BaseActivity
import com.example.video_downloader_xxx.ui.fragment.browser.home.adapter.SocialAdapter
import com.example.video_downloader_xxx.util.PrefHelper

class IntroActivity: BaseActivity<ActivityIntroBinding>() {
    private lateinit var pages: List<IntroPage>

    override fun initView() {
        pages =listOf(
            IntroPage(R.drawable.img_onboarding_1_1, R.drawable.intro_ic_progress_1,getString(R.string.btn_title_1), getString(R.string.btn_content_1)),
            IntroPage(R.drawable.img_onboarding_2_2, R.drawable.intro_ic_progress_2,getString(R.string.btn_title_2), getString(R.string.btn_content_2)),
            IntroPage(R.drawable.img_onboarding_3_3, R.drawable.intro_ic_progress_3,getString(R.string.btn_title_3), getString(R.string.btn_content_3))
        )
    }

    override fun initData() {
    }

    override fun initListener() {
        val adapter = IntroAdapter(pages) {page ->
            val current = pages.indexOf(page)
            if(current < pages.size - 1) {
                binding.viewPager.currentItem = current + 1
            }else{
                PrefHelper.setFirstOpenFalse(this)
                startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                finish()
            }
        }

        binding.viewPager.adapter = adapter
    }



    override fun viewBinding(): ActivityIntroBinding = ActivityIntroBinding.inflate(layoutInflater)
}