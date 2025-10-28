plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
}

val splitApks = !project.hasProperty("noSplits")
val abiFilterList = (properties["ABI_FILTERS"] as String).split(';')

android {
    namespace = "com.example.video_downloader_xxx"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.video_downloader_xxx"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }
}

dependencies {
    // Core yt-dlp wrapper
    implementation("io.github.junkfood02.youtubedl-android:library:0.18.0")

    // FFmpeg để xử lý audio/video
    implementation("io.github.junkfood02.youtubedl-android:ffmpeg:0.18.0")

    // Aria2c để tăng tốc download (optional)
    implementation("io.github.junkfood02.youtubedl-android:aria2c:0.18.0")

    //Koin
    implementation("io.insert-koin:koin-android:4.1.0")

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