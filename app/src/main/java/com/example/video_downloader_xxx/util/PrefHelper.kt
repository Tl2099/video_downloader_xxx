package com.example.video_downloader_xxx.util

import android.content.Context
import androidx.core.content.edit

object PrefHelper {
    private const val PREF_NAME = "app_pref"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_FIRST_OPEN = "first_open"

    fun isFirstOpen(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FIRST_OPEN, true)
    }

    fun setFirstOpenFalse(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_FIRST_OPEN, false) }
    }

    fun isOnboardingDone(context: Context): Boolean {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingDone(context: Context) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit { putBoolean(KEY_ONBOARDING_DONE, true) }
    }

     fun getLastAnalyzedUrl(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("videoDownloader", Context.MODE_PRIVATE)
        return sharedPreferences.getString("lastAnalyzedUrl", null)
    }

     fun setLastAnalyzedUrl(context: Context, url: String) {
        val sharedPreferences = context.getSharedPreferences("videoDownloader", Context.MODE_PRIVATE)
        sharedPreferences.edit { putString("lastAnalyzedUrl", url) }
    }
}