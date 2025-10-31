package com.example.video_downloader_xxx.util

import android.view.View
import androidx.fragment.app.Fragment

fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
    val view = requireActivity().currentFocus ?: View(requireContext())
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
