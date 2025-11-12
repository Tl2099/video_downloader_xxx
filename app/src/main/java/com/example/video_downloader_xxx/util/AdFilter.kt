package com.example.video_downloader_xxx.util

import android.util.Log
import android.webkit.WebResourceResponse
import com.example.video_downloader_xxx.data.model.VideoInfo
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.regex.Pattern

class AdFilter {
    companion object {
        private val AD_DOMAINS = setOf(
            "googleads.com", "doubleclick.net", "googletag", "adsystem.com",
            "facebook.com/ad", "ads.yahoo.com", "amazon-adsystem.com",
            "adsystem.amazon.com", "googlesyndication.com", "2mdn.net",
            "ads.twitter.com", "analytics.twitter.com", "scorecardresearch.com",
            "quantserve.com", "outbrain.com", "taboola.com", "adsystem",
            "advertising.com", "adskeeper.co.uk", "criteo.com", "adsystem.com"
        )

        private val AD_PATTERNS = listOf(
            Pattern.compile(
                ".*\\/(ads?|advertisement|advert|adserv|adservice|adserver|adnxs|adsystem)\\/.*",
                Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile(
                ".*[?&](ad|ads|adv|advert|advertisement)([=&].*)?$",
                Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile(
                ".*\\/(tracking|analytics|metrics|telemetry|beacon)\\/.*",
                Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile(".*\\/(pixel|track|ping)\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*banner.*\\.(gif|jpg|jpeg|png).*", Pattern.CASE_INSENSITIVE)
        )

        private val NON_VIDEO_EXTENSIONS = setOf(
            "js", "css", "html", "xml", "ico", "png", "gif", "jpg", "jpeg",
            "svg", "woff", "woff2", "ttf", "otf", "eot", "pdf", "txt", "json",
            "php", "jsp", "asp", "cur", "webp", "bmp", "tif", "tiff",
            "vtt", "srt", "ass", "ssa", "sub",
            "xml", "ttml",
            "m3u", "pls",
            "torrent"
        )

        private val NON_VIDEO_DOMAINS = setOf(
            "youtube.com", "youtu.be",
            "google.com", "googlevideo.com", "play.google.com",
            "facebook.com", "fbcdn.net",
            "twitter.com", "t.co",
            "instagram.com", "cdninstagram.com",
            "tiktok.com", "pinterest.com",
            "vimeo.com", "dailymotion.com"
        )

        private val TRACKING_PATTERNS = listOf(
            Pattern.compile(".*\\/collect\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/track\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/pixel\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/beacon\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*utm_.*", Pattern.CASE_INSENSITIVE)
        )

        private val API_PATTERNS = listOf(
            Pattern.compile(".*\\/api\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/backend\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/config\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*get-config\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/v\\d+\\/.*config.*", Pattern.CASE_INSENSITIVE)
        )

        private val SUBTITLE_PATTERNS = listOf(
            Pattern.compile(".*\\/subtitle\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.(vtt|srt|ass|ssa|sub|ttml).*", Pattern.CASE_INSENSITIVE)
        )


    }

    fun isAd(url: String): Boolean {
        val cleanUrl = url.lowercase().trim()
        return isAdDomain(cleanUrl) || isAdPattern(cleanUrl) || isTrackingUrl(cleanUrl)
    }

    /**
     * Check if URL is a video file that should be analyzed
     */

    fun isHLSVideoSegment(url: String): Boolean {
        val cleanUrl = url.lowercase().trim()

        // Check if it's a .ts segment
        if (!cleanUrl.contains(".ts")) return false

        // Additional checks for legitimate video segments
        return cleanUrl.contains("/hls/") ||
                cleanUrl.contains("/stream/") ||
                cleanUrl.matches(Regex(".*\\d{8,}_[a-f0-9]+/\\d+k/hls/.*\\.ts.*"))
    }

    fun isHLSAdSegment(url: String): Boolean {
        val cleanUrl = url.lowercase().trim()

        if (!cleanUrl.contains(".ts")) return false

        // Patterns that often indicate ad segments
        val adPatterns = listOf(
            "ad-", "ads-", "preroll", "midroll", "postroll",
            "sponsor", "promo", "commercial",
            "/ad/", "/ads/", "/advertising/"
        )

        return adPatterns.any { pattern ->
            cleanUrl.contains(pattern, ignoreCase = true)
        }
    }

    private fun hostOf(url: String): String? = try {
        URI(url).host?.lowercase()
    } catch (_: Exception) {
        null
    }

    private fun isNonVideoDomain(url: String): Boolean {
        val host = hostOf(url) ?: return false
        return NON_VIDEO_DOMAINS.any { d ->
            host == d || host.endsWith(".$d")
        }
    }

//    fun isVideoCandidate(url: String, contentType: String?, contentLength: Long?): Boolean {
//        val cleanUrl = url.lowercase().trim()
//
//        // Skip if it's an ad
//        if (isAd(url)) return false
//
//         //Skip non-video domains (e.g., youtube embed, google log)
//        if (isNonVideoDomain(cleanUrl)) return false
//
//         //Skip embed or logging URLs
//        if (cleanUrl.contains("/embed/") || cleanUrl.contains("/log")) {
//            return false
//        }
//
//        // Skip non-HTTP URLs
//        if (!cleanUrl.startsWith("http")) return false
//
//        // Skip API calls
//        if (isApiCall(cleanUrl)) return false
//
//        // Skip subtitle files
//        if (isSubtitleFile(cleanUrl)) return false
//
//        // Skip if file extension indicates non-video
//        if (hasNonVideoExtension(cleanUrl)) return false
//
//        // Check content type
//        contentType?.let { type ->
//            val lowerType = type.lowercase()
//
//            // Skip non-video content types explicitly
//            if (lowerType.contains("text/") ||
//                lowerType.contains("application/json") ||
//                lowerType.contains("application/xml") ||
//                lowerType.contains("image/") ||
//                lowerType.contains("font/") ||
//                lowerType.contains("text/vtt") ||  // VTT subtitles
//                lowerType.contains("application/ttml+xml")) { // TTML subtitles
//                return false
//            }
//
//            if (lowerType.contains("video") || lowerType.contains("audio") ||
//                lowerType.contains("mpegurl") || lowerType.contains("dash")
//            ) {
//                return true
//            }
//
//            // Skip non-media content types
//            if (lowerType.contains("text/") || lowerType.contains("application/json") ||
//                lowerType.contains("image/") || lowerType.contains("font/")
//            ) {
//                return false
//            }
//        }
//
//        // Check file size (skip very small files - likely tracking pixels)
//        contentLength?.let {
//            val mb = it / (1024.0 * 1024.0)
//            if (mb < 1) return false
//        }
//
//        // Check for video-like patterns in URL
//        if (hasVideoLikePattern(cleanUrl)) return true
//
//        return false
//    }

    fun isVideoCandidate(url: String, contentType: String?, contentLength: Long?): Boolean {
        val cleanUrl = url.lowercase().trim()
        Log.d("AdFilterDebug", "🔍 Checking: $cleanUrl (ct=$contentType, cl=$contentLength)")

        if (isAd(url)) {
            Log.d("AdFilterDebug", "❌ filtered by isAd()")
            return false
        }

        if (isNonVideoDomain(cleanUrl)) {
            Log.d("AdFilterDebug", "❌ filtered by isNonVideoDomain()")
            return false
        }

        if (cleanUrl.contains("/embed/") || cleanUrl.contains("/log")) {
            Log.d("AdFilterDebug", "❌ filtered by embed/log path")
            return false
        }

        if (!cleanUrl.startsWith("http")) {
            Log.d("AdFilterDebug", "❌ filtered by non-http scheme")
            return false
        }

        if (isApiCall(cleanUrl)) {
            Log.d("AdFilterDebug", "❌ filtered by isApiCall()")
            return false
        }

        if (isSubtitleFile(cleanUrl)) {
            Log.d("AdFilterDebug", "❌ filtered by subtitle pattern")
            return false
        }

        if (hasNonVideoExtension(cleanUrl)) {
            Log.d("AdFilterDebug", "❌ filtered by hasNonVideoExtension()")
            return false
        }

        // Content-Type checks
        if (contentType != null) {
            val lower = contentType.lowercase()
            Log.d("AdFilterDebug", "🧩 Content-Type: $lower")

            if (lower.contains("text/") ||
                lower.contains("application/json") ||
                lower.contains("application/xml") ||
                lower.contains("image/") ||
                lower.contains("font/") ||
                lower.contains("text/vtt") ||
                lower.contains("application/ttml+xml")
            ) {
                Log.d("AdFilterDebug", "❌ filtered by non-video content-type")
                return false
            }

            if (lower.contains("video") || lower.contains("audio") ||
                lower.contains("mpegurl") || lower.contains("dash")
            ) {
                Log.d("AdFilterDebug", "✅ accepted by content-type")
                return true
            }
        }

        // Content-Length check
        if (contentLength != null) {
            val mb = contentLength / (1024.0 * 1024.0)
            Log.d("AdFilterDebug", "🧩 File size ≈ %.2f MB".format(mb))
            if (mb < 1) {
                Log.d("AdFilterDebug", "❌ filtered by small file size (<1MB)")
                return false
            }
        } else {
            Log.d("AdFilterDebug", "ℹ️ Content-Length missing → skip size filter")
        }

        // Pattern match check
        val patternMatch = hasVideoLikePattern(cleanUrl)
        Log.d("AdFilterDebug", "🎯 hasVideoLikePattern = $patternMatch")

        if (patternMatch) {
            Log.d("AdFilterDebug", "✅ accepted by pattern match (.mp4 etc.)")
            return true
        }

        Log.d("AdFilterDebug", "❌ no video-like pattern found → rejected")
        return false
    }


    // Thêm helper methods
    private fun isApiCall(url: String): Boolean {
        return API_PATTERNS.any { pattern ->
            pattern.matcher(url).matches()
        }
    }

    private fun isSubtitleFile(url: String): Boolean {
        return SUBTITLE_PATTERNS.any { pattern ->
            pattern.matcher(url).matches()
        }
    }

    /**
     * Create empty response to block ads
     */
    fun createBlockedResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream("".toByteArray())
        )
    }

    /**
     * Normalize URL để so sánh duplicate
     */
    fun normalizeUrl(url: String): String {
        val cleanUrl = url.lowercase().trim()

        // Remove common varying parameters
        val urlWithoutParams = cleanUrl.split('?')[0]

        // For HLS segments, normalize to base pattern
        if (isHLSVideoSegment(url)) {
            return normalizeHLSUrl(urlWithoutParams)
        }

        return urlWithoutParams
    }

    /**
     * Normalize HLS URL để group segments
     */
    private fun normalizeHLSUrl(url: String): String {
        // Từ: https://stream.com/video/1080p/hls/segment001.ts
        // Thành: https://stream.com/video/1080p/hls/

        val hlsPattern = Regex("(.*hls/)[^/]*\\.ts$")
        val match = hlsPattern.find(url)

        return if (match != null) {
            match.groupValues[1] // Base HLS path
        } else {
            url
        }
    }

    // Private helper methods
    private fun isAdDomain(url: String): Boolean {
        return AD_DOMAINS.any { domain ->
            url.contains(domain, ignoreCase = true)
        }
    }

    private fun isAdPattern(url: String): Boolean {
        return AD_PATTERNS.any { pattern ->
            pattern.matcher(url).matches()
        }
    }

    private fun isTrackingUrl(url: String): Boolean {
        return TRACKING_PATTERNS.any { pattern ->
            pattern.matcher(url).matches()
        }
    }

    private fun hasNonVideoExtension(url: String): Boolean {
        val lower = url.lowercase().trim()
        val urlWithoutParams = lower.substringBefore("?").substringBefore("#")

        if (!urlWithoutParams.contains('.')) return false

        if (urlWithoutParams.endsWith("/css") ||
            urlWithoutParams.endsWith("/css2") ||
            urlWithoutParams.endsWith("/js") ||
            urlWithoutParams.endsWith("/html") ||
            urlWithoutParams.endsWith("/xml")) {
            return true
        }

        val extension = urlWithoutParams.substringAfterLast('.', "")
            .substringBefore("/")
            .trim()

        if (extension.isEmpty()) return false

        val isNonVideo = extension in NON_VIDEO_EXTENSIONS
        return isNonVideo
    }


    private fun hasVideoLikePattern(url: String): Boolean {
        val lower = url.lowercase()

        val videoExtensions = listOf(".mp4", ".mkv", ".mov", ".avi", ".webm", ".m3u8", ".mpd", ".ts")
        if (videoExtensions.any { lower.contains(it) }) return true

        val pathIndicators = listOf("/stream/", "/play/", "/hls/")
        if (pathIndicators.any { lower.contains(it) }) return true

        if (lower.contains("/video/") && !lower.contains("banner") && !lower.contains("frame") && !lower.contains("thumb")) {
            return true
        }

        return false
    }

}