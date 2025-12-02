package com.example.video_downloader_xxx.data.model

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class VideoClip(val start: Int = 0, val end: Int = 0) {
    constructor(
        range: ClosedFloatingPointRange<Float>
    ) : this(range.start.roundToInt(), range.endInclusive.roundToInt())
}