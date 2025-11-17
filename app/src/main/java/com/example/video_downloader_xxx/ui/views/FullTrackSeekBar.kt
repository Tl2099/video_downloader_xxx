package com.example.video_downloader_xxx.ui.views

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatSeekBar

@Keep
class FullTrackSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.seekBarStyle
) : AppCompatSeekBar(context, attrs, defStyleAttr){

    override fun onDraw(canvas: Canvas) {
        val progressDrawable = progressDrawable ?: return
        val saveCount = canvas.save()

        val padding = (16 * resources.displayMetrics.density).toInt()
        val trackWidth = width - padding * 2

        val left = (width - trackWidth) / 2
        val right = left + trackWidth


        progressDrawable.setBounds(left, 0, right, height)
        progressDrawable.draw(canvas)

        canvas.restoreToCount(saveCount)
    }

}