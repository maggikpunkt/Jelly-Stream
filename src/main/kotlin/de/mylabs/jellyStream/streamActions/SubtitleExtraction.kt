package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

class SubtitleExtraction(stream: Stream, baseName: String) : Extraction(stream, baseName) {

    override fun getFileExtension(): String {
        return when (stream.codec_name) {
            "subrip" -> "srt"
            "hdmv_pgs_subtitle" -> "mks"
            else -> stream.codec_name
        }
    }

    override fun getCodec(index: Int): List<String> {
        val list: MutableList<String> = ArrayList<String>()
        list.addAll(listOf("-c:$index", "copy"))
        when (stream.codec_name) {
            "hdmv_pgs_subtitle" -> {
                list.add("-f")
                list.add("matroska")
            }

            else -> {}
        }
        return list
    }

    /*

        var extension = stream.codec_name!!
        when (stream.codec_name) {
            "ass" -> {
                println("Will extract ${stream.getName()}")
                println("Will transcode ${stream.getName()}")
            }
        }

     */
}
