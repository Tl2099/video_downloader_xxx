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
                "Instagram",
                "https://www.google.com/s2/favicons?sz=128&domain=instagram.com",
                "https://instagram.com"
            ),
            Social(
                "X (Twitter)",
                "https://d1yjjnpx0p53s8.cloudfront.net/styles/logo-thumbnail/s3/012024/twitter-x.png",
                "https://x.com"
            ),
            Social("TikTok",
                "https://cdn-icons-png.flaticon.com/512/3116/3116491.png",
                "https://tiktok.com"),
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