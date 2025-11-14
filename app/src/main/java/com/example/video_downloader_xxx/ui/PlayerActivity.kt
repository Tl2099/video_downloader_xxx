package com.example.video_downloader_xxx.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.video_downloader_xxx.data.DataExt
import com.example.video_downloader_xxx.databinding.ActivityPlayerBinding
import com.example.video_downloader_xxx.ui.base.BaseActivity
import java.io.File

class PlayerActivity : BaseActivity<ActivityPlayerBinding>() {
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uriVideo = Uri.parse("android.resource://${packageName}/${DataExt.path}")

        Log.d("LamnhNguvl", ""+uriVideo )
        player = ExoPlayer.Builder(this).build()

        binding.playerView.player = player

        player.setMediaItem(MediaItem.fromUri(DataExt.path))
        player.prepare()
        player.play()
    }

    override fun initView() {
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun viewBinding(): ActivityPlayerBinding = ActivityPlayerBinding.inflate(layoutInflater)

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}