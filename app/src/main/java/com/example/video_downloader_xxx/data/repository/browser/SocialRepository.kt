package com.example.video_downloader_xxx.data.repository.browser

import com.example.video_downloader_xxx.data.model.Social

class SocialRepository {

    fun getDefaultSocials(): List<Social> {
        return listOf(
            Social(
                "Facebook",
                "https://www.google.com/s2/favicons?sz=128&domain=facebook.com",
                "https://facebook.com"
            ),
            Social(
                "YouTube",
                "https://www.google.com/s2/favicons?sz=128&domain=youtube.com",
                "https://youtube.com"
            ),
            Social(
                "Instagram",
                "https://www.google.com/s2/favicons?sz=128&domain=instagram.com",
                "https://instagram.com"
            ),
            Social(
                "X (Twitter)",
                "https://www.google.com/s2/favicons?sz=128&domain=x.com",
                "https://x.com"
            ),
            Social("TikTok", "https://tiktok.com/favicon.ico", "https://tiktok.com"),
            Social(
                "Reddit",
                "https://www.google.com/s2/favicons?sz=128&domain=reddit.com",
                "https://reddit.com"
            ),
            Social(
                "Pinterest",
                "https://www.google.com/s2/favicons?sz=128&domain=pinterest.com",
                "https://pinterest.com"
            ),
            Social(
                "LinkedIn",
                "https://www.google.com/s2/favicons?sz=128&domain=linkedin.com",
                "https://linkedin.com"
            ),
            Social(
                "Snapchat",
                "https://www.google.com/s2/favicons?sz=128&domain=snapchat.com",
                "https://snapchat.com"
            ),
            Social(
                "Threads",
                "https://www.google.com/s2/favicons?sz=128&domain=threads.net",
                "https://threads.net"
            )
        )
    }
}