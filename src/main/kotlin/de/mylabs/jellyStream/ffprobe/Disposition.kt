package de.mylabs.jellyStream.ffprobe

@Suppress("ConstructorParameterNaming")
data class Disposition(
    val attached_pic: Int = 0,
    val captions: Int = 0,
    val clean_effects: Int = 0,
    val comment: Int = 0,
    val default: Int = 0,
    val dependent: Int = 0,
    val descriptions: Int = 0,
    val dub: Int = 0,
    val forced: Int = 0,
    val hearing_impaired: Int = 0,
    val karaoke: Int = 0,
    val lyrics: Int = 0,
    val metadata: Int = 0,
    val non_diegetic: Int = 0,
    val original: Int = 0,
    val still_image: Int = 0,
    val timed_thumbnails: Int = 0,
    val visual_impaired: Int = 0
)
