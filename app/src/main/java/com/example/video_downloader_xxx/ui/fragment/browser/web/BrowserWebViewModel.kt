package com.example.video_downloader_xxx.ui.fragment.browser.web

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BrowserWebViewModel: ViewModel() {

    private val _videoDetected = MutableSharedFlow<String>()
    val videoDetected = _videoDetected.asSharedFlow()

    suspend fun emitVideoDetected(url: String){
        _videoDetected.emit(url)
    }
}