package de.mylabs.jellyStream.ffprobe

@Suppress("ConstructorParameterNaming")
data class Chapter(
    val end: Long,
    val end_time: String,
    val id: Long,
    val start: Long,
    val start_time: String,
    val tags: Map<String, String> = emptyMap(),
    val time_base: String
)
