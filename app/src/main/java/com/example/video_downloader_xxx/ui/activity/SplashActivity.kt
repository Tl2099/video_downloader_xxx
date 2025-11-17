package com.example.video_downloader_xxx.ui.activity

import android.animation.Animator
import android.animation.ValueAnimator
import com.example.video_downloader_xxx.databinding.ActivitySplashBinding
import com.example.video_downloader_xxx.ui.base.BaseActivity

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun initView() {
        startProgressAnimation()
    }

    private fun startProgressAnimation() {
        val seekBar = binding.customProgressBar

        val animator = ValueAnimator.ofInt(0, seekBar.max).apply {
            ValueAnimator.setDuration = 2000L
            //repeatCount = 0
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            addUpdateListener { valueAnimator ->
                val progress = valueAnimator.animatedValue as Int
                seekBar.progress = progress
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    //startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    //finish()
                }

                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }

        animator.start()
    }

    override fun viewBinding(): ActivitySplashBinding =
        ActivitySplashBinding.inflate(layoutInflater)

    override fun initData() {}
    override fun initListener() {}
}