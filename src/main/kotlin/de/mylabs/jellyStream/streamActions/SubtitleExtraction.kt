package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

class SubtitleExtraction(stream: Stream, baseName: String, ignoreTitle: Boolean) :
    Extraction(stream, baseName, ignoreTitle) {

    override fun getFileExtension(): String {
        return when (stream.codec_name) {
            "subrip" -> "srt"
            "hdmv_pgs_subtitle", "dvb_subtitle" -> "mks"
            else -> stream.codec_name
        }
    }

    override fun getCodec(index: Int): List<String> {
        val list: MutableList<String> = ArrayList<String>()
        list.addAll(listOf("-c:$index", "copy"))
        when (stream.codec_name) {
            "hdmv_pgs_subtitle", "dvb_subtitle" -> {
                if (titleNeedsCleaning()) {
                    list += listOf("-metadata:s:$index", "title=")
                }

                list.add("-f")
                list.add("matroska")
            }

            else -> {}
        }
        return list
    }
}
