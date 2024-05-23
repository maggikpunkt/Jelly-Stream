package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

class AudioExtraction(stream: Stream, baseName: String, ignoreTitle: Boolean) :
    Extraction(stream, baseName, ignoreTitle) {
    override fun getFileExtension(): String {
        return when (stream.codec_name) {
            "eac3" -> "ac3"
            else -> stream.codec_name
        }
    }

    override fun getCodec(index: Int): List<String> {
        val list: MutableList<String> = ArrayList<String>()
        list.addAll(listOf("-c:$index", "copy"))
        if (getFileExtension() != stream.codec_name) {
            list.add("-f")
            list.add(stream.codec_name)
        }
        return list
    }
}
