package com.example.video_downloader_xxx.util

import android.webkit.WebResourceResponse
import com.example.video_downloader_xxx.data.model.VideoInfo
import java.io.ByteArrayInputStream
import java.util.regex.Pattern

class AdFilter {
    companion object {
        // Common ad domains
        private val AD_DOMAINS = setOf(
            "googleads.com", "doubleclick.net", "googletag", "adsystem.com",
            "facebook.com/ad", "ads.yahoo.com", "amazon-adsystem.com",
            "adsystem.amazon.com", "googlesyndication.com", "2mdn.net",
            "ads.twitter.com", "analytics.twitter.com", "scorecardresearch.com",
            "quantserve.com", "outbrain.com", "taboola.com", "adsystem",
            "advertising.com", "adskeeper.co.uk", "criteo.com", "adsystem.com"
        )

        // Ad-related URL patterns
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

        // Non-video file extensions
        private val NON_VIDEO_EXTENSIONS = setOf(
            // Existing extensions
            "js", "css", "html", "xml", "ico", "png", "gif", "jpg", "jpeg",
            "svg", "woff", "woff2", "ttf", "otf", "eot", "pdf", "txt", "json",
            "php", "jsp", "asp", "cur", "webp", "bmp", "tif", "tiff",

            "vtt", "srt", "ass", "ssa", "sub",  // Subtitle files
            "xml", "ttml",                       // Subtitle formats
            "m3u", "pls",                       // Playlist files (not video)
            "torrent"                           // Torrent files
        )


        // Tracking and analytics patterns
        private val TRACKING_PATTERNS = listOf(
            Pattern.compile(".*\\/collect\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/track\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/pixel\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/beacon\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*utm_.*", Pattern.CASE_INSENSITIVE)
        )

        // API and configuration patterns
        private val API_PATTERNS = listOf(
            Pattern.compile(".*\\/api\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/backend\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/config\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*get-config\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/v\\d+\\/.*config.*", Pattern.CASE_INSENSITIVE)
        )

        // Subtitle and support file patterns
        private val SUBTITLE_PATTERNS = listOf(
            Pattern.compile(".*\\/subtitle\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.(vtt|srt|ass|ssa|sub|ttml).*", Pattern.CASE_INSENSITIVE)
        )


    }

    /**
     * Check if URL is an advertisement
     */
    fun isAd(url: String): Boolean {
        val cleanUrl = url.lowercase().trim()

        // Check domain-based blocking
        if (isAdDomain(cleanUrl)) return true

        // Check pattern-based blocking
        if (isAdPattern(cleanUrl)) return true

        // Check tracking patterns
        if (isTrackingUrl(cleanUrl)) return true

        return false
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


    fun isVideoCandidate(url: String, contentType: String?, contentLength: Long?): Boolean {
        val cleanUrl = url.lowercase().trim()

        // Skip if it's an ad
        if (isAd(url)) return false

        // Skip non-HTTP URLs
        if (!cleanUrl.startsWith("http")) return false

        // Skip API calls
        if (isApiCall(cleanUrl)) return false

        // Skip subtitle files
        if (isSubtitleFile(cleanUrl)) return false

        // Skip if file extension indicates non-video
        if (hasNonVideoExtension(cleanUrl)) return false

        // Check content type
        contentType?.let { type ->
            val lowerType = type.lowercase()

            // Skip non-video content types explicitly
            if (lowerType.contains("text/") ||
                lowerType.contains("application/json") ||
                lowerType.contains("application/xml") ||
                lowerType.contains("image/") ||
                lowerType.contains("font/") ||
                lowerType.contains("text/vtt") ||  // VTT subtitles
                lowerType.contains("application/ttml+xml")) { // TTML subtitles
                return false
            }

            if (lowerType.contains("video") || lowerType.contains("audio") ||
                lowerType.contains("mpegurl") || lowerType.contains("dash")
            ) {
                return true
            }

            // Skip non-media content types
            if (lowerType.contains("text/") || lowerType.contains("application/json") ||
                lowerType.contains("image/") || lowerType.contains("font/")
            ) {
                return false
            }
        }

        // Check file size (skip very small files - likely tracking pixels)
        contentLength?.let { size ->
            if (size < 1024) return false // Less than 1KB
        }

        // Check for video-like patterns in URL
        if (hasVideoLikePattern(cleanUrl)) return true

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
     * Extract clean video info and filter out ad-related metadata
     */

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
        val urlWithoutParams = url.split('?')[0]
        val extension = urlWithoutParams.substringAfterLast('.', "")
        return extension in NON_VIDEO_EXTENSIONS
    }

    private fun hasVideoLikePattern(url: String): Boolean {
        val videoPatterns = listOf(
            "video",
            "watch",
            "play",
            "stream",
            "/v/",
            "/embed/",
            ".mp4",
            ".avi",
            ".mkv",
            ".mov",
            ".m3u8",
            ".mpd"
        )
        return videoPatterns.any { pattern ->
            url.contains(pattern, ignoreCase = true)
        }
    }
}