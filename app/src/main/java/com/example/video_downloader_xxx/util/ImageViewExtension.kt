package com.example.video_downloader_xxx.util

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun ImageView.glideLoad(url: Any) {
    Glide.with(this)
        .load(url)
        .skipMemoryCache(true)
        .into(this)
        .waitForLayout()
        .clearOnDetach()
}

fun ImageView.glideLoadUseCache(url: Any) {
    Glide.with(this)
        .load(url)
        .into(this)
        .waitForLayout()
//        .clearOnDetach()
}

fun ImageView.glideLoad(url: Any, placeholder: Int) {
    Glide.with(this)
        .load(url)
        .placeholder(placeholder)
        .skipMemoryCache(true)
        .into(this)
        .waitForLayout()
}

fun ImageView.glideLoadCallback(url: Any, callback: () -> Unit) {
    Glide.with(this)
        .asBitmap()
        .load(url)
        .skipMemoryCache(true)
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap?>,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Bitmap,
                model: Any,
                target: Target<Bitmap?>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                callback.invoke()
                return true
            }
        })
        .into(this)
        .waitForLayout()

}