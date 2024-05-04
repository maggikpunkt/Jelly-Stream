package de.mylabs.jellyStream.ffprobe

@Suppress("ConstructorParameterNaming", "LongParameterList")
class Stream(
    val avg_frame_rate: String,
    val bit_rate: String?,
    val bits_per_sample: Int?,
    val channel_layout: String?,
    val channels: Int?,
    val chroma_location: String?,
    val closed_captions: Int?,
    val codec_long_name: String,
    val codec_name: String,
    val codec_tag: String,
    val codec_tag_string: String,
    val codec_type: String,
    val coded_height: Int?,
    val coded_width: Int?,
    val color_range: String?,
    val display_aspect_ratio: String?,
    val disposition: Disposition,
    val duration: String?,
    val duration_ts: Int?,
    val extradata_size: Int?,
    val film_grain: Int?,
    val has_b_frames: Int?,
    val height: Int?,
    val index: Int,
    val initial_padding: Int?,
    val level: Int?,
    val pix_fmt: String?,
    val profile: String?,
    val r_frame_rate: String,
    val refs: Int?,
    val sample_aspect_ratio: String?,
    val sample_fmt: String?,
    val sample_rate: String?,
    val start_pts: Int,
    val start_time: String,
    val tags: Map<String, String>?,
    val time_base: String,
    val width: Int?
) {

    fun getName(): String {
        return "#${this.index} (${this.codec_type},${this.codec_name},${this.getLanguage()})"
    }

    fun getLanguage(): String? {
        return this.tags?.getOrDefault(LANGUAGE, this.tags[LANGUAGE.uppercase()])
    }

    companion object {
        const val LANGUAGE = "language"
        const val VIDEO = "video"
        const val AUDIO = "audio"
        const val SUBTITLE = "subtitle"
        const val DATA = "data"
        const val ATTACHMENT = "attachment"
    }
}
