package com.example.video_downloader_xxx.data.repository.browser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class OkHttpVideoDownloader(
    private val client: OkHttpClient = OkHttpClient(),
    private val maxRetry: Int = 3,
    private val retryDelay: Long = 1500
) {

    suspend fun downloadVideo(
        url: String,
        destFile: File,
        onProgress: (Int) -> Unit
    ) = withContext(Dispatchers.IO) {

        var attempt = 0

        while (attempt < maxRetry) {
            try {
                return@withContext downloadWithResume(url, destFile, onProgress)
            } catch (e: Exception) {
                attempt++
                if (attempt >= maxRetry) throw e
                delay(retryDelay)
            }
        }
    }

    private fun downloadWithResume(
        url: String,
        destFile: File,
        onProgress: (Int) -> Unit
    ) {

        val downloaded = if (destFile.exists()) destFile.length() else 0L

        val request = Request.Builder()
            .url(url)
            .addHeader("Range", "bytes=$downloaded-")
            .build()

        val response = client.newCall(request).execute()
        response.use { resp ->

            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")

            val body = resp.body ?: throw Exception("Null body")
            val newBytes = body.contentLength()
            val totalBytes = downloaded + newBytes

            val input = body.byteStream()
            val output = FileOutputStream(destFile, true)

            output.use { out ->
                val buffer = ByteArray(8192)
                var read: Int
                var total = downloaded

                while (input.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                    total += read

                    val progress = ((total * 100) / totalBytes).toInt()
                    onProgress(progress.coerceIn(0, 100))
                }
            }
        }
    }
}
