package de.mylabs.jellyStream.ffprobe

@Suppress("ConstructorParameterNaming")
data class Disposition(
    val attached_pic: Int,
    val captions: Int,
    val clean_effects: Int,
    val comment: Int,
    val default: Int,
    val dependent: Int,
    val descriptions: Int,
    val dub: Int,
    val forced: Int,
    val hearing_impaired: Int,
    val karaoke: Int,
    val lyrics: Int,
    val metadata: Int,
    val non_diegetic: Int,
    val original: Int,
    val still_image: Int,
    val timed_thumbnails: Int,
    val visual_impaired: Int
)
