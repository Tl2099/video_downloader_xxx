package com.example.video_downloader_xxx

import android.app.Application
import com.example.video_downloader_xxx.di.appModules
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DownloadApplication : Application()  {

    override fun onCreate() {
        super.onCreate()

        //Khởi tạo Koin
        startKoin {
            androidContext(this@DownloadApplication)
            modules(appModules)
        }

        //Khởi tạo yt-dlp
        try {
            YoutubeDL.getInstance().init(this);
            FFmpeg.getInstance().init(this);
            Aria2c.getInstance().init(this);
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}