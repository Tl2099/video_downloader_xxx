package com.example.video_downloader_xxx.data.repository

import com.example.video_downloader_xxx.util.DownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

class VideoRepository(private val client: OkHttpClient = OkHttpClient()) {

    fun downloadVideo(url: String, outputFile: File): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0))

        val request = Request.Builder().url(url).build()
        val response = try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            emit(DownloadState.Error("Network error: ${e.message}"))
            return@flow
        }

        if (!response.isSuccessful) {
            emit(DownloadState.Error("HTTP ${response.code}"))
            return@flow
        }

        val body = response.body ?: run {
            emit(DownloadState.Error("Empty body"))
            return@flow
        }

        val total = body.contentLength().takeIf { it > 0 } ?: -1L
        var copied = 0L

        //Ghi file
        outputFile.parentFile?.mkdirs()
        outputFile.sink().buffer().use { sink ->
            body.source().use { source ->
                var read: Long
                val chunk = 8_192L
                while (source.read(sink.buffer, chunk).also { read = it } != -1L) {
                    copied += read
                    if (total > 0) {
                        val percent = (copied * 100 / total).toInt().coerceIn(0, 100)
                        emit(DownloadState.Downloading(percent))
                    }
                }
                sink.flush()
            }
        }
        emit(DownloadState.Success(outputFile))
    }.flowOn(Dispatchers.IO)
}