package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

class SubtitleTranscode(override val stream: Stream) : Transcode(stream) {
    override fun getCodec(index: Int): List<String> {
        when (stream.codec_name) {
            "dvd_subtitle" -> {
                return listOf("-c:$index", "copy")
            }

            else -> {
                return listOf("-c:$index", "mov_text")
            }
        }
    }
}
