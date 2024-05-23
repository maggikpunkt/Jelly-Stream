package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

abstract class Transcode(open val stream: Stream, open val cleanTitle: Boolean) {
    fun getMapping(): List<String> {
        return listOf("-map", "0:${stream.index}")
    }

    abstract fun getCodec(index: Int): List<String>

    fun titleNeedsCleaning(): Boolean = titleNeedsCleaning(stream.getTitle() ?: "")
}
