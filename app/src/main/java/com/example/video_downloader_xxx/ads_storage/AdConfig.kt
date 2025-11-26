package com.example.video_downloader_xxx.ads_storage

import com.example.video_downloader_xxx.BuildConfig
import com.example.video_downloader_xxx.ui.activity.SplashActivity
import com.teh.software.tehads.format.AppOpenConfig
import com.teh.software.tehads.format.AppOpenType
import com.teh.software.tehads.format.BannerType
import com.teh.software.tehads.format.InterConfig
import com.teh.software.tehads.format.InterType
import com.teh.software.tehads.format.NativeBannerConfig
import com.teh.software.tehads.format.NativeBannerType
import com.teh.software.tehads.format.NativeType
import com.teh.software.tehads.format.NativeViewType
import com.teh.software.tehads.format.SplashConfig
import com.teh.software.tehads.format.SplashType
import com.teh.software.tehads.format.provider.NovaProvider

object AdConfig {

    val appOpenConfig by lazy {
        AppOpenConfig(
            idAdmob = BuildConfig.AppOpen_Admob,
            idMax = BuildConfig.AppOpen_Max,
            classDisable = listOf(SplashActivity::class.java),
            type = AppOpenType.APP_OPEN_NORMAL.value,
            orderProvider = listOf(NovaProvider.ADMOB.value, NovaProvider.MAX.value)
        ).fromRemote(RemoteKey.APP_OPEN)
    }

    //================= Splash ========== =======//
    val splashConfig by lazy {
        SplashConfig(
            idAdmob = BuildConfig.Inter_Splash_Admob,
            idMax = BuildConfig.Inter_Splash_Max,
            idAppOpenAdmob = BuildConfig.AppOpen_Admob,
            idAppOpenMax = BuildConfig.AppOpen_Max,
            idNativeAdmob = BuildConfig.Native_Full_Admob,
            idNativeMax = BuildConfig.Native_Full_Max,
            nativeViewType = NativeViewType.FULL_SIZE_2,
            type = SplashType.INTERSTITIAL.value,
            orderProvider = listOf(
                NovaProvider.ADMOB.value,
                NovaProvider.MAX.value
            ),
            orderProviderNative = listOf(
                NovaProvider.ADMOB.value,
                NovaProvider.MAX.value
            )
        ).fromRemote(RemoteKey.SPLASH)
    }

    val nativeBannerLanguageConfig by lazy {
        NativeBannerConfig(
            idAdmob = BuildConfig.Native_Admob,
            idMax = BuildConfig.Native_Max,
            nativeType = NativeType.NATIVE_NORMAL.value,
            nativeViewType = NativeViewType.MEDIUM,
            idBannerAdmob = BuildConfig.Banner_Admob,
            idBannerMax = BuildConfig.Banner_Max,
            bannerType = BannerType.BANNER_LARGE.value,
            type = NativeBannerType.NATIVE.value,
            orderProvider = listOf(
                NovaProvider.ADMOB.value, NovaProvider.MAX.value
            )
        ).fromRemote(RemoteKey.NATIVE_BANNER_LANGUAGE_CONFIG)
    }

    val nativeBannerIntroConfig by lazy {
        NativeBannerConfig(
            idAdmob = BuildConfig.Native_Admob,
            idMax = BuildConfig.Native_Max,
            nativeType = NativeType.NATIVE_NORMAL.value,
            nativeViewType = NativeViewType.MEDIUM,
            idBannerAdmob = BuildConfig.Banner_Admob,
            idBannerMax = BuildConfig.Banner_Max,
            bannerType = BannerType.BANNER_LARGE.value,
            type = NativeBannerType.NATIVE.value,
            orderProvider = listOf(NovaProvider.ADMOB.value, NovaProvider.MAX.value)
        ).fromRemote(RemoteKey.SPLASH_BANNER_CONFIG)
    }

    val nativeBannerSlideIntroConfig by lazy {
        NativeBannerConfig(
            idAdmob = BuildConfig.Native_Admob,
            idMax = BuildConfig.Native_Max,
            nativeType = NativeType.NATIVE_NORMAL.value,
            nativeViewType = NativeViewType.SLIDE,
            idBannerAdmob = BuildConfig.Banner_Admob,
            idBannerMax = BuildConfig.Banner_Max,
            bannerType = BannerType.BANNER_LARGE.value,
            type = NativeBannerType.NATIVE.value,
            orderProvider = listOf(NovaProvider.ADMOB.value, NovaProvider.MAX.value)
        ).fromRemote(RemoteKey.SPLASH_BANNER_CONFIG)
    }

    val interNativeCallsBackConfig by lazy {
        InterConfig(
            idAdmob = BuildConfig.Inter_Admob,
            idMax = BuildConfig.Inter_Max,
            idNativeAdmob = BuildConfig.Native_Full_Admob,
            idNativeMax = BuildConfig.Native_Full_Max,
            nativeViewType = NativeViewType.FULL_SIZE_2,
            nativeTimeClose = 3,
            type = InterType.NATIVE_FULL.value,
            orderProvider = listOf(NovaProvider.ADMOB.value, NovaProvider.MAX.value),
            orderProviderNative = listOf(NovaProvider.ADMOB.value, NovaProvider.MAX.value)
        ).fromRemote(RemoteKey.NATIVE_FULL_SCREEN_ONBOARDING_CONFIG)
    }

}