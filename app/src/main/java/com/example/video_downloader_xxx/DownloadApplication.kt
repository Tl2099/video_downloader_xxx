package com.example.video_downloader_xxx

import com.example.video_downloader_xxx.di.appModules
import com.example.video_downloader_xxx.di.databaseModule
import com.example.video_downloader_xxx.di.repositoryModule
import com.example.video_downloader_xxx.di.viewModelModule
import com.teh.software.tehads.NovaApplication
import com.teh.software.tehads.main.NovaAds
import com.teh.software.tehads.main.config.AdjustConfig
import com.teh.software.tehads.main.config.NovaConfig
import com.teh.software.tehads.solar.SolarConfig
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DownloadApplication : NovaApplication() {

    override fun onInitialize() {
        instance = this

        initKoin()
        initYoutubeDL()
        initAds()
    }

    private fun initYoutubeDL() {
        try {
            YoutubeDL.getInstance().init(this);
            FFmpeg.getInstance().init(this);
            Aria2c.getInstance().init(this);
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@DownloadApplication)
            modules(
                appModules,
                databaseModule,
                viewModelModule,
                repositoryModule,
            )
        }
    }

    companion object {
        lateinit var instance: DownloadApplication
            private set
    }

    private fun initAds() {
        val adjustConfig = AdjustConfig.Builder(adjustToken = BuildConfig.adjust_app_token).build()
        val solarConfig = SolarConfig.Builder(appKey = BuildConfig.solar_app_token).build()

        val novaConfig =
            NovaConfig.Builder(adjustConfig = adjustConfig, solarConfig = solarConfig)
                .variantProduct(BuildConfig.isVariantProduce)
                .keyMax(BuildConfig.SDKKey_Max)
                .build()

        NovaAds.getInstance().initialize(application = this, novaConfig = novaConfig)
    }
}