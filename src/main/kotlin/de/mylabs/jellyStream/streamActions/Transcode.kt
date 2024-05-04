package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

abstract class Transcode(open val stream: Stream) {
    fun getMapping(): List<String> {
        return listOf("-map", "0:${stream.index}")
    }

    abstract fun getCodec(index: Int): List<String>
}
