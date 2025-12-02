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
        val pathSourceUrl = DataExt.pathSourceUrl

        Log.i("PlayerActivity", "onCreate: Video Url = $pathVideoUrl")
        Log.i("PlayerActivity", "onCreate: Local Video = $pathLocalVideo")
        Log.i("PlayerActivity", "onCreate: Source Url = $pathSourceUrl")

        val uri = chooseVideoUri(pathVideoUrl, pathLocalVideo, pathSourceUrl)

        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        binding.playerView.controllerAutoShow = true
        binding.playerView.controllerHideOnTouch = true
        binding.playerView.showController()

        if (uri == Uri.EMPTY) {
            Toast.makeText(this, "Không tìm thấy video để phát", Toast.LENGTH_SHORT).show()
            return
        }

        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
    }

    private fun isTikTokUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.contains("tiktok.com", ignoreCase = true)
    }

    private fun chooseVideoUri(
        pathVideoUrl: String?,
        pathLocalVideo: String?,
        pathSourceUrl: String?
    ): Uri {
        val hasLocal = !pathLocalVideo.isNullOrBlank()
        val isTikTok = isTikTokUrl(pathSourceUrl ?: pathVideoUrl)
        val online = isNetworkAvailable()

        Log.i(
            "PlayerActivity",
            "chooseVideoUri: isTikTok=$isTikTok, hasLocal=$hasLocal, online=$online"
        )

        return when {
            isTikTok && hasLocal -> {
                Log.i("PlayerActivity", "Play LOCAL (TikTok)")
                Uri.fromFile(File(pathLocalVideo))
            }

            !isTikTok && online && !pathVideoUrl.isNullOrBlank() -> {
                Log.i("PlayerActivity", "Play ONLINE (non-TikTok)")
                pathVideoUrl.toUri()
            }

            !isTikTok && hasLocal -> {
                Log.i("PlayerActivity", "Play LOCAL (non-TikTok, offline)")
                Uri.fromFile(File(pathLocalVideo))
            }

            hasLocal -> {
                Log.i("PlayerActivity", "Play LOCAL (fallback)")
                Uri.fromFile(File(pathLocalVideo))
            }

            else -> {
                Log.e("PlayerActivity", "No valid URI to play")
                Uri.EMPTY
            }
        }
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