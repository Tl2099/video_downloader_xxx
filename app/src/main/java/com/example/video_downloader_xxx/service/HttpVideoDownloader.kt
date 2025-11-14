package com.example.video_downloader_xxx.service

import com.example.video_downloader_xxx.data.model.VideoInfo
import com.example.video_downloader_xxx.data.repository.browser.DownloadProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class HttpVideoDownloader {

    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    fun download(
        video: VideoInfo,
        outputFile: File
    ): Flow<DownloadProgress> = channelFlow {

        try {
            var downloaded = if (outputFile.exists()) outputFile.length() else 0L

            val request = Request.Builder()
                .url(video.videoUrl!!)
                .apply {
                    if (downloaded > 0) {
                        addHeader("Range", "bytes=$downloaded-")
                    }
                }
                .build()

            val response = client.newCall(request).execute()
            val body = response.body ?: throw Exception("Empty body")

            val total = (body.contentLength() + downloaded)

            val input = body.byteStream()
            val output = FileOutputStream(outputFile, true)

            val buffer = ByteArray(8192)
            var read: Int

            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
                downloaded += read

                val percent = (downloaded * 100f) / total
                trySend(DownloadProgress(percent, 0, null))
            }

            output.close()
            input.close()

            trySend(DownloadProgress(100f, 0, "Completed"))

        } catch (e: Exception) {
            trySend(DownloadProgress(-1f, 0, "Error: ${e.message}"))
        }

        awaitClose { }
    }.flowOn(Dispatchers.IO)
}
