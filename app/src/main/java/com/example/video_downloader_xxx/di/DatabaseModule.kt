package com.example.video_downloader_xxx.di

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import com.example.video_downloader_xxx.data.local.database.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single { provideDatabase(get()) }
    single { provideDownloadedVideoDAO(get()) }
    single { provideWebsiteHistoryDao(get()) }
}

private fun provideDatabase(application: Application): AppDatabase {
    return Room.databaseBuilder(application, AppDatabase::class.java, "app_database.db")
        .fallbackToDestructiveMigration().build()
}

private fun provideDownloadedVideoDAO(db: AppDatabase) = db.downloadedVideoDao()

private fun provideWebsiteHistoryDao(db: AppDatabase) = db.websiteHistoryDao()