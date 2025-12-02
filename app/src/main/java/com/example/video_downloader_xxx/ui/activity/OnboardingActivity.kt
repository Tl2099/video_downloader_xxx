package com.example.video_downloader_xxx.ui.activity

import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.databinding.ActivityOnboardingBinding
import com.example.video_downloader_xxx.ui.base.BaseActivity

class OnboardingActivity: BaseActivity<ActivityOnboardingBinding>() {

    private val onboardingImages = listOf(
        R.drawable.img_onboarding_1,
        R.drawable.img_onboarding_2,
        R.drawable.img_onboarding_3,
    )

    private val onProgressImages = listOf(
        R.drawable.onboarding_ic_progress_1,
        R.drawable.onboarding_ic_progress_2,
        R.drawable.onboarding_ic_progress_3,
    )

    private var currentPage = 0

    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    private fun loadImage(index: Int) {
        binding.imgMain.setImageResource(onboardingImages[index])

        binding.btnNext.text = if (index == onboardingImages.lastIndex) "Start" else "Next"
    }


    override fun viewBinding(): ActivityOnboardingBinding = ActivityOnboardingBinding.inflate(layoutInflater)
}