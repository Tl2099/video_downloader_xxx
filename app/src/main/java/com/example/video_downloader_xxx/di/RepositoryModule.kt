package com.example.video_downloader_xxx.di

import com.example.video_downloader_xxx.data.local.reposities.video.VideoInfoRepository
import com.example.video_downloader_xxx.data.local.reposities.video.VideoInfoRepositoryImpl
import com.example.video_downloader_xxx.data.repository.VideoRepository
import com.example.video_downloader_xxx.data.repository.browser.SocialRepository
import com.example.video_downloader_xxx.data.repository.web.DownloadVideosOnWebRepository
import com.example.video_downloader_xxx.data.repository.web.DownloadVideosOnWebRepositoryImpl
import com.example.video_downloader_xxx.data.repository.webHistory.WebsiteHistoryRepository
import com.example.video_downloader_xxx.data.repository.webHistory.WebsiteHistoryRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module{
    single { VideoRepository(get()) }
    single { SocialRepository() }
    single<DownloadVideosOnWebRepository> { DownloadVideosOnWebRepositoryImpl() }
    single<VideoInfoRepository> { VideoInfoRepositoryImpl(get()) }
    single<WebsiteHistoryRepository> { WebsiteHistoryRepositoryImpl(get()) }
}