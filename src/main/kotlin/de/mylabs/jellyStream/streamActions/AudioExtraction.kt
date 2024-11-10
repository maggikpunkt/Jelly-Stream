package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

class AudioExtraction(stream: Stream, baseName: String, ignoreTitle: Boolean) :
    Extraction(stream, baseName, ignoreTitle) {
    override fun getFileExtension(): String {
        return when (stream.codec_name) {
            "eac3" -> "ac3"
            null -> throw IllegalArgumentException("codec name is null")
            else -> stream.codec_name
        }
    }

    override fun getCodec(index: Int): List<String> {
        val list: MutableList<String> = ArrayList<String>()
        list.addAll(listOf("-c:$index", "copy"))
        if (getFileExtension() != stream.codec_name) {
            if (titleNeedsCleaning()) {
                // delete stream title
                list += listOf("-metadata:s:$index", "title=")
            }

            // delete global title
            list += listOf("-metadata:g", "title=")

            // don't copy chapter information
            list += listOf("-map_chapters", "-1")

            if (stream.codec_name != null) {
                // force format because ffmpeg does not want to put eac3 in a file with ac3 file extension
                list += listOf("-f", stream.codec_name)
            }
        }
        return list
    }
}
