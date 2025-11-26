package com.example.video_downloader_xxx.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment

fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
    val view = requireActivity().currentFocus ?: View(requireContext())
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun MenuItem.resizeIconBitmap(context: Context, sizeDp: Int) {
    val drawable = icon ?: return

    val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt()

    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, sizePx, sizePx)
    drawable.draw(canvas)

    this.icon = BitmapDrawable(context.resources, bitmap)
}


