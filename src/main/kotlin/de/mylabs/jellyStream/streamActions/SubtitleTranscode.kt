package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

class SubtitleTranscode(override val stream: Stream, ignoreTitle: Boolean) : Transcode(stream, ignoreTitle) {
    override fun getCodec(index: Int): List<String> {
        val list = ArrayList<String>()
        val disposition = ArrayList<String>()
        when (stream.codec_name) {
            "dvd_subtitle" -> {
                list += listOf("-c:$index", "copy")
            }

            else -> {
                list += listOf("-c:$index", "mov_text")
            }
        }

        if (titleNeedsCleaning()) {
            list += listOf("-metadata:s:$index", "title=")
        }

        if ((stream.guessedDisposition?.forced ?: 0) == 1) {
            disposition += "forced"
        }

        if ((stream.guessedDisposition?.hearing_impaired ?: 0) == 1) {
            disposition += "hearing_impaired"
        }

        if (disposition.isNotEmpty()) {
            list += "-disposition:s:$index"
            list += disposition.joinToString("+", "+")
        }

        return list
    }
}
