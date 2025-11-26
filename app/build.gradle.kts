plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
}

val splitApks = !project.hasProperty("noSplits")
val abiFilterList = (properties["ABI_FILTERS"] as String).split(';')

android {
    namespace = "com.example.video_downloader_xxx"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.video_downloader_xxx"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("x86")
            abiFilters.add("x86_64")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }

        if (splitApks) {
            splits {
                abi {
                    isEnable = true
                    reset()
                    include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
                    isUniversalApk = true
                }
            }
        } else {
            ndk { abiFilters.addAll(abiFilterList) }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "isVariantProduce", "true")

            //Adjust
            buildConfigField("String", "adjust_app_token", "\"roymr4vsz8xs\"")
            buildConfigField("String", "solar_app_token", "\"ddf87c2da8875d84\"")

            buildConfigField("String", "SDKKey_Max", "\"ZAfVxNkdSk6cf7ZwKlno-4hm9rkqoJZqRtOQdltbEIQZEmpRKvnSPUMfWylvDoPqffwh6xUdDL2la8IlH_UGFD\"")
            buildConfigField("String", "Inter_Max", "\"ac0d1b8e7ce4cb8c\"")
            buildConfigField("String", "Inter_Splash_Max", "\"16b9ad5ea8b7a7d6\"")
            buildConfigField("String", "Native_Max", "\"3b9635a7e13978a7\"")
            buildConfigField("String", "Native_Full_Max", "\"0171b0c53db719c8\"")
            buildConfigField("String", "Banner_Max", "\"48e7c132a8b93ec7\"")
            buildConfigField("String", "AppOpen_Max", "\"eff27eb9c0b7c549\"")
            buildConfigField("String", "Rewarded_Max", "\"9ba62b1c70e342a4\"")

            resValue("string", "admob_app_id", "ca-app-pub-8936048775410604~6188501102")
            //Splash
            buildConfigField("String", "AppOpen_Admob", "\"ca-app-pub-8936048775410604/1295267070\"")
            buildConfigField("String", "Inter_Splash_Admob", "\"ca-app-pub-8936048775410604/3646098247\"")
            buildConfigField("String", "Native_Admob", "\"ca-app-pub-8936048775410604/2052717493\"")
            buildConfigField("String", "Native_Full_Admob", "\"ca-app-pub-8936048775410604/7669103736\"")
            buildConfigField("String", "Banner_Admob", "\"ca-app-pub-8936048775410604/6356022061\"")
            buildConfigField("String", "Inter_Admob", "\"ca-app-pub-8936048775410604/4966370031\"")
            buildConfigField("String", "Rewarded_Admob", "\"ca-app-pub-8936048775410604/2340206691\"")
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("Boolean", "isVariantProduce", "false")

            //Adjust
            buildConfigField("String", "adjust_app_token", "\"cc4jvudppczk\"")
            buildConfigField("String", "solar_app_token", "\"123\"")

            buildConfigField("String", "SDKKey_Max", "\"ZAfVxNkdSk6cf7ZwKlno-4hm9rkqoJZqRtOQdltbEIQZEmpRKvnSPUMfWylvDoPqffwh6xUdDL2la8IlH_UGFD\"")
            buildConfigField("String", "Inter_Max", "\"2c7fd911d9393057\"")
            buildConfigField("String", "Inter_Splash_Max", "\"2c7fd911d9393057\"")
            buildConfigField("String", "Native_Max", "\"645e4f104ae05cc3\"")
            buildConfigField("String", "Native_Full_Max", "\"645e4f104ae05cc3\"")
            buildConfigField("String", "Banner_Max", "\"afc329f93331abe0\"")
            buildConfigField("String", "AppOpen_Max", "\"0aa23fe22877ee4b\"")
            buildConfigField("String", "Rewarded_Max", "\"126abc7594c6134d\"")

            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")
            //Splash
            buildConfigField("String", "AppOpen_Admob", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "Inter_Splash_Admob", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "Native_Admob", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "Native_Full_Admob", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "Banner_Admob", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "Inter_Admob", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "Rewarded_Admob", "\"ca-app-pub-3940256099942544/5224354917\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    //Ads
    implementation("com.teh.software:ads-sdk:1.4.0.2")

    //Thư viện Google Mobile Ads SDK (AdMob)
    implementation("com.google.android.gms:play-services-ads:24.6.0")

    //FOA
    implementation("com.teh.software:foa-sdk:1.4.1.1")

    //multidex
    implementation("androidx.multidex:multidex:2.0.1")

    //lottie
    implementation("com.airbnb.android:lottie:6.6.7")

    //RoomDatabase
    implementation("androidx.room:room-runtime:2.8.3")
    kapt("androidx.room:room-compiler:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")

    //ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")

    //glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    //Android Material Components
    implementation("com.google.android.material:material:1.13.0")

    // Core yt-dlp wrapper
    implementation("io.github.junkfood02.youtubedl-android:library:0.18.0")

    // FFmpeg để xử lý audio/video
    implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.18.0")

    // Aria2c để tăng tốc download (optional)
    implementation("io.github.junkfood02.youtubedl-android:aria2c:0.18.0")

    //Koin
    implementation("io.insert-koin:koin-android:4.1.1")
    implementation("io.insert-koin:koin-androidx-navigation:4.1.1")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:5.2.1")

    //Worker
    implementation("androidx.work:work-runtime-ktx:2.11.0")

    // WebView
    implementation("androidx.webkit:webkit:1.6.1")

    // Download
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // --- Fragment & Navigation ---
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")

    // --- Compose (chỉ cho UI component) ---
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    debugImplementation("androidx.compose.ui:ui-tooling")

    // --- Coroutine ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    //Mobile Ads SDK
    //implementation("com.teh.software:ads-sdk:1.3.7")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}