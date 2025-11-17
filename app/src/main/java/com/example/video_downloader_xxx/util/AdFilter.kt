package com.example.video_downloader_xxx.util

import android.util.Log
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.regex.Matcher
import java.util.regex.Pattern

class AdFilter {

    companion object {

        // ================================
        // VIDEO EXTENSION REGEX
        // ================================
        private val VIDEO_REGEX = Pattern.compile(
            "\\.(mp4|mp4v|mpv|m1v|m4v|mpg|mpg2|mpeg|xvid|webm|3gp|avi|mov|mkv|ogg|ogv|ogm|m3u8|mpd|ism(?:[vc]|/manifest)?)(?:[\\?#]|$)",
            Pattern.CASE_INSENSITIVE
        )


        // ================================
        // ADS DOMAIN
        // ================================
        private val AD_DOMAINS = setOf(
            "googleads.com", "doubleclick.net", "googletag", "adsystem.com",
            "facebook.com/ad", "ads.yahoo.com", "amazon-adsystem.com",
            "adsystem.amazon.com", "googlesyndication.com", "2mdn.net",
            "ads.twitter.com", "analytics.twitter.com", "scorecardresearch.com",
            "quantserve.com", "outbrain.com", "taboola.com", "advertising.com",
            "adskeeper.co.uk", "criteo.com"
        )

        // ================================
        // ADS PATTERNS
        // ================================
        private val AD_PATTERNS = listOf(
            Pattern.compile(".*\\/(ads?|advertisement|advert|adserv|adservice|adserver|adnxs|adsystem)\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*[?&](ad|ads|adv|advert|advertisement)([=&].*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/(tracking|analytics|metrics|telemetry|beacon)\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/(pixel|track|ping)\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*banner.*\\.(gif|jpg|jpeg|png).*", Pattern.CASE_INSENSITIVE)
        )

        // ================================
        // NON-VIDEO EXTENSIONS
        // ================================
        private val NON_VIDEO_EXTENSIONS = setOf(
            "js", "css", "html", "xml", "ico", "png", "gif", "jpg", "jpeg",
            "svg", "woff", "woff2", "ttf", "otf", "eot", "pdf", "txt", "json",
            "php", "jsp", "asp", "cur", "webp", "bmp", "tif", "tiff",
            "vtt", "srt", "ass", "ssa", "sub", "ttml",
            "m3u", "pls", "torrent"
        )

        // ================================
        // DOMAINS NOT ALLOWED TO DETECT VIDEO
        // ================================
        private val NON_VIDEO_DOMAINS = setOf(
            "youtube.com", "youtu.be",
            "google.com", "googlevideo.com", "play.google.com",
            "facebook.com", "fbcdn.net",
            "twitter.com", "t.co",
            "instagram.com", "cdninstagram.com",
            "tiktok.com", "pinterest.com",
            "vimeo.com", "dailymotion.com"
        )

        // ================================
        // TRACKING PATTERNS
        // ================================
        private val TRACKING_PATTERNS = listOf(
            Pattern.compile(".*\\/collect\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/track\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/pixel\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/beacon\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*utm_.*", Pattern.CASE_INSENSITIVE)
        )

        // ================================
        // API CALL PATTERNS
        // ================================
        private val API_PATTERNS = listOf(
            Pattern.compile(".*\\/api\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/backend\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/config\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*get-config\\?.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\/v\\d+\\/.*config.*", Pattern.CASE_INSENSITIVE)
        )

        // ================================
        // SUBTITLE FILE PATTERNS
        // ================================
        private val SUBTITLE_PATTERNS = listOf(
            Pattern.compile(".*\\/subtitle\\/.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.(vtt|srt|ass|ssa|sub|ttml).*", Pattern.CASE_INSENSITIVE)
        )
    }


    // ========================================================
    // CHECK VIDEO MIME TYPE
    // ========================================================
    fun getVideoMimeType(url: String?): String? {
        if (url == null) return null

        val matcher: Matcher = VIDEO_REGEX.matcher(url.lowercase())

        if (!matcher.find()) return null

        return when (matcher.group(1)) {

            "mp4", "mp4v", "m4v" -> "video/mp4"
            "mpv" -> "video/MPV"
            "m1v", "mpg", "mpg2", "mpeg" -> "video/mpeg"
            "xvid" -> "video/x-xvid"
            "webm" -> "video/webm"
            "3gp" -> "video/3gpp"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "mkv" -> "video/x-mkv"
            "ogg", "ogv", "ogm" -> "video/ogg"
            "m3u8" -> "application/x-mpegURL"
            "mpd" -> "application/dash+xml"
            "ism", "ism/manifest", "ismv", "ismc" -> "application/vnd.ms-sstr+xml"

            else -> null
        }
    }


    // ========================================================
    // CHECK ADS
    // ========================================================
    fun isAd(url: String): Boolean {
        val clean = url.lowercase()
        return isAdDomain(clean) || isAdPattern(clean) || isTrackingUrl(clean)
    }

    fun isHLSVideoSegment(url: String): Boolean {
        val cleanUrl = url.lowercase().trim()

        // Must be a .ts file
        if (!cleanUrl.contains(".ts")) return false

        // Common HLS patterns
        if (cleanUrl.contains("/hls/")) return true
        if (cleanUrl.contains("/stream/")) return true

        // Detect segments like:
        val dynamicPattern = Regex(".*\\d{8,}_[a-f0-9]+/\\d+k/hls/.*\\.ts.*")
        if (cleanUrl.matches(dynamicPattern)) return true

        return false
    }

    fun isHLSAdSegment(url: String): Boolean {
        val cleanUrl = url.lowercase().trim()

        if (!cleanUrl.contains(".ts")) return false

        // Common ad indicators
        val adIndicators = listOf(
            "ad-", "ads-", "preroll", "midroll", "postroll",
            "sponsor", "promo", "commercial",
            "/ad/", "/ads/", "/advertising/"
        )

        return adIndicators.any { cleanUrl.contains(it) }
    }



    // ========================================================
    // MAIN VIDEO DETECTION
    // ========================================================
    fun isVideoCandidate(url: String, contentType: String?, contentLength: Long?): Boolean {

        val cleanUrl = url.lowercase().trim()
        Log.d("AdFilterDebug", "🔍 Checking: $cleanUrl")

        if (isAd(cleanUrl)) return false
        if (isNonVideoDomain(cleanUrl)) return false
        if (!cleanUrl.startsWith("http")) return false
        if (isApiCall(cleanUrl)) return false
        if (isSubtitleFile(cleanUrl)) return false
        if (hasNonVideoExtension(cleanUrl)) return false

        // Content-Type
        if (contentType != null) {
            val ct = contentType.lowercase()

            if (ct.contains("video") ||
                ct.contains("audio") ||
                ct.contains("mpegurl") ||
                ct.contains("dash")
            ) return true

            if (ct.contains("text") || ct.contains("json") || ct.contains("xml"))
                return false
        }

        // Content-Length
        if (contentLength != null && contentLength < 1_000_000)
            return false

        // URL pattern
        return hasVideoLikePattern(cleanUrl)
    }


    // ========================================================
    // HELPER FUNCTIONS
    // ========================================================

    private fun hostOf(url: String): String? =
        try { URI(url).host?.lowercase() } catch (_: Exception) { null }

    private fun isNonVideoDomain(url: String): Boolean {
        val h = hostOf(url) ?: return false
        return NON_VIDEO_DOMAINS.any { d -> h == d || h.endsWith(".$d") }
    }

    private fun isApiCall(url: String) = API_PATTERNS.any { it.matcher(url).matches() }

    private fun isSubtitleFile(url: String) = SUBTITLE_PATTERNS.any { it.matcher(url).matches() }

    private fun isAdDomain(url: String) = AD_DOMAINS.any { url.contains(it) }

    private fun isAdPattern(url: String) = AD_PATTERNS.any { it.matcher(url).matches() }

    private fun isTrackingUrl(url: String) = TRACKING_PATTERNS.any { it.matcher(url).matches() }

    private fun hasNonVideoExtension(url: String): Boolean {
        val clean = url.substringBefore("?").substringBefore("#")
        val ext = clean.substringAfterLast('.', "").trim()
        return ext in NON_VIDEO_EXTENSIONS
    }

    private fun hasVideoLikePattern(url: String): Boolean {
        val u = url.lowercase()
        return setOf(".mp4", ".mkv", ".avi", ".mov", ".webm", ".m3u8", ".mpd", ".ts")
            .any { u.contains(it) }
                || u.contains("/hls/")
                || u.contains("/stream/")
                || (u.contains("/video/") && !u.contains("thumb") && !u.contains("banner"))
    }


    // ========================================================
    // BLOCK RESPONSE
    // ========================================================
    fun createBlockedResponse(): WebResourceResponse =
        WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))


    // ========================================================
    // URL NORMALIZER
    // ========================================================
    fun normalizeUrl(url: String): String {
        val base = url.lowercase().substringBefore("?")
        if (base.endsWith(".ts")) return base.substringBeforeLast("/") + "/"
        return base
    }
}
