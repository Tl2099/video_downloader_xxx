package com.example.video_downloader_xxx.di

import com.example.video_downloader_xxx.data.repository.VideoRepository
import com.example.video_downloader_xxx.domain.usecase.DownloadVideoUseCase
import com.example.video_downloader_xxx.service.VideoDownloadService
import com.example.video_downloader_xxx.ui.fragment.browser.DownloadViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.VideoDownloadManager
import com.example.video_downloader_xxx.ui.fragment.browser.web.BrowserWebViewModel
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModules = module{

    single { OkHttpClient() }
    single { VideoDownloadManager() }
    single { VideoRepository(get()) }
    single { VideoDownloadService() }
    factory { DownloadVideoUseCase(get()) }
    viewModel { DownloadViewModel(get()) }
    viewModel { BrowserWebViewModel() }
    viewModel { SharedViewModel() }
}