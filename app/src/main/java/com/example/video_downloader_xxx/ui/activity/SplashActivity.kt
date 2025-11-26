package com.example.video_downloader_xxx.ui.activity

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import com.example.video_downloader_xxx.R
import com.example.video_downloader_xxx.ads_storage.AdConfig
import com.example.video_downloader_xxx.ads_storage.RemoteKey.SHOW_INTRO_AGAIN_CONFIG
import com.example.video_downloader_xxx.ads_storage.RemoteKey.SHOW_INTRO_CONFIG
import com.example.video_downloader_xxx.ads_storage.RemoteKey.SHOW_LANGUAGE_AGAIN_CONFIG
import com.example.video_downloader_xxx.ads_storage.RemoteKey.SHOW_LANGUAGE_CONFIG
import com.example.video_downloader_xxx.databinding.ActivitySplashBinding
import com.teh.software.tehads.remote.RemoteConfig
import com.teh.software.tehads.view.BaseSplashActivity
import com.teh.software.tehfoa.helper.main.FOAHelper
import com.teh.software.tehfoa.helper.main.IntroConfig
import com.teh.software.tehfoa.helper.main.IntroModel
import com.teh.software.tehfoa.helper.main.LanguageConfig

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseSplashActivity<ActivitySplashBinding>(R.xml.remote_config) {
    val showLanguage by lazy { RemoteConfig.instance.getBoolean(SHOW_LANGUAGE_CONFIG, true) }
    val showLanguageAgain by lazy {
        RemoteConfig.instance.getBoolean(
            SHOW_LANGUAGE_AGAIN_CONFIG,
            true
        )
    }
    val showIntro by lazy { RemoteConfig.instance.getBoolean(SHOW_INTRO_CONFIG, true) }
    val showIntroAgain by lazy { RemoteConfig.instance.getBoolean(SHOW_INTRO_AGAIN_CONFIG, true) }

    override fun onInitAds() {
        Log.d("SonLN", "onInitAds: $showLanguage $showLanguageAgain $showIntro $showIntroAgain")

        FOAHelper.getInstance().start(
            activity = this@SplashActivity,
            lifecycleOwner = this,
            splashConfig = AdConfig.splashConfig,
            languageConfig = LanguageConfig(
                firstLanguageConfigAds = AdConfig.nativeBannerLanguageConfig,
                showAgain = true
            ),
            introConfig = IntroConfig(
                introConfigs = listOf(
                    IntroModel(
                        id = 0,
                        image = R.drawable.img_onboarding_1,
                        title = "Fast Video Download",
                        description = "Just paste the link and you can download the video quickly."
                    ),
                    IntroModel(
                        id = 1,
                        image = R.drawable.img_onboarding_2,
                        title = "Missile Loading Speed",
                        description = "Unlimited bandwidth for lightning-fast download speeds"
                    ),
                    IntroModel(
                        id = 2,
                        image = R.drawable.img_onboarding_3,
                        title = "Easy To Download",
                        description = "Save videos instantly with just one tap"
                    ),
                ),
                interval = 2,
                showAgain = true,
                nativeBannerConfig = AdConfig.nativeBannerIntroConfig,
                slideNativeBannerConfig = AdConfig.nativeBannerSlideIntroConfig,
                interConfig = AdConfig.interNativeCallsBackConfig
            ),
            targetActivity = MainActivity::class.java
        )
    }

    override fun onInitViews() {
        startProgressAnimation()
    }

    private fun startProgressAnimation() {
        val seekBar = binding.customProgressBar

        val animator = ValueAnimator.ofInt(0, seekBar.max).apply {
            //ValueAnimator.setDuration = 2000L
            repeatCount = 0
            duration = 2000L
//            repeatCount = ValueAnimator.INFINITE
//            repeatMode = ValueAnimator.REVERSE

            addUpdateListener { valueAnimator ->
                val progress = valueAnimator.animatedValue as Int
                seekBar.progress = progress
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
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

}