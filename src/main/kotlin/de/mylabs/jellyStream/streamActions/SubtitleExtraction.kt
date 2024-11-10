package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

class SubtitleExtraction(stream: Stream, baseName: String, ignoreTitle: Boolean) :
    Extraction(stream, baseName, ignoreTitle) {

    override fun getFileExtension(): String {
        return when (stream.codec_name) {
            "subrip" -> "srt"
            "hdmv_pgs_subtitle", "dvb_subtitle" -> "mks"
            null -> throw IllegalArgumentException("codec name is null")
            else -> stream.codec_name
        }
    }

    override fun getCodec(index: Int): List<String> {
        val list: MutableList<String> = ArrayList<String>()
        list.addAll(listOf("-c:$index", "copy"))
        when (stream.codec_name) {
            "hdmv_pgs_subtitle", "dvb_subtitle" -> {
                if (titleNeedsCleaning()) {
                    // delete stream title
                    list += listOf("-metadata:s:$index", "title=")
                }

                // delete global title
                list += listOf("-metadata:g", "title=")

                // don't copy chapter information
                list += listOf("-map_chapters", "-1")

                // force matroska format because we use mks file extension and ffmpeg does not know what to do with it
                list += listOf("-f", "matroska")
            }

            else -> {}
        }
        return list
    }
}
