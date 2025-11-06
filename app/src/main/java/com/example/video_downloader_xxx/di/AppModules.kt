package com.example.video_downloader_xxx.di

import com.example.video_downloader_xxx.data.repository.browser.SocialRepository
import com.example.video_downloader_xxx.data.repository.browser.VideoDownloadManager
import com.example.video_downloader_xxx.data.repository.VideoRepository
import com.example.video_downloader_xxx.data.repository.web.DownloadVideosOnWebRepository
import com.example.video_downloader_xxx.data.repository.web.DownloadVideosOnWebRepositoryImpl
import com.example.video_downloader_xxx.domain.usecase.DownloadVideoUseCase
import com.example.video_downloader_xxx.service.VideoDownloadService
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.home.BrowserViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.web.WebViewModel
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModules = module{

    single { OkHttpClient() }
    single { VideoDownloadManager() }
    single { VideoRepository(get()) }
    single { SocialRepository() } //
    single<DownloadVideosOnWebRepository> { DownloadVideosOnWebRepositoryImpl() }
//    single<VideoRepository> { VideoRepositoryImpl() }
    single { VideoDownloadService() }
    factory { DownloadVideoUseCase(get()) }
    viewModel { BrowserViewModel(get(), get()) }
    viewModel { WebViewModel(get()) }
    viewModel { SharedViewModel() }
}