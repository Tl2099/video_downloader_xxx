package com.example.video_downloader_xxx.di

import com.example.video_downloader_xxx.ui.fragment.browser.bookmark.BookmarkViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.SharedViewModel
import com.example.video_downloader_xxx.ui.fragment.browser.history.WebsiteHistoryViewModel
import com.example.video_downloader_xxx.ui.fragment.library.LibraryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module{
    viewModel { SharedViewModel(get(), get(), get ()) }
    viewModel { LibraryViewModel(get()) }
    viewModel { WebsiteHistoryViewModel(get()) }
    viewModel { BookmarkViewModel(get()) }
}