package com.example.video_downloader_xxx.ui.activity

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.video_downloader_xxx.data.DataExt
import com.example.video_downloader_xxx.databinding.ActivityPlayerBinding
import com.example.video_downloader_xxx.util.isNetworkAvailable
import java.io.File
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import com.example.video_downloader_xxx.ui.base.BaseActivity

class PlayerActivity : BaseActivity<ActivityPlayerBinding>() {
    private lateinit var player: ExoPlayer

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("PlayerActivity", "onCreate: ")

        val pathVideoUrl = DataExt.pathVideoUrl
        val pathLocalVideo = DataExt.pathLocalVideo

        val isOnline = isNetworkAvailable()

        val uri = if (isOnline) {
            Log.i("PlayerActivity", "Online")
            pathVideoUrl.toUri()
        } else {
            Log.i("PlayerActivity", "Offline")
            Uri.fromFile(File(pathLocalVideo))
        }
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        binding.playerView.controllerAutoShow = true
        binding.playerView.controllerHideOnTouch = true
        binding.playerView.showController()
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
    }



    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
        binding.btnBack.setOnClickListener {
            Log.i("PlayerActivity", "btnBack: Called")
            finish()
        }
    }

    override fun viewBinding(): ActivityPlayerBinding = ActivityPlayerBinding.inflate(layoutInflater)

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
        }
    }
}