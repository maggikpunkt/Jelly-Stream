package de.mylabs.jellyStream.ffprobe

data class FfprobeResult(
    val chapters: List<Chapter> = emptyList(),
    val format: Format,
    val streams: List<Stream>
)
