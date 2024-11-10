package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream
import java.io.File

class GroupExtraction(val stream: Stream, val baseName: String, val ignoreTitle: Boolean) {

    fun getName(): String = "$baseName.mks"

    fun fileAlreadyExists(): Boolean {
        return File(getName()).exists()
    }

    fun getMapping(): List<String> {
        return listOf("-map", "0:${stream.index}")
    }

    fun titleNeedsCleaning(): Boolean = titleNeedsCleaning(stream.getTitle() ?: "")

    fun getCodec(index: Int): List<String> {
        val list: MutableList<String> = ArrayList<String>()

        list += (listOf("-c:$index", "copy"))
        if (titleNeedsCleaning()) {
            // delete stream title
            list += listOf("-metadata:s:$index", "title=")
        }

        return list
    }

    fun getGroupCodec(): List<String> {
        val list: MutableList<String> = ArrayList<String>()
        // delete global title
        list += listOf("-metadata:g", "title=")

        // don't copy chapter information
        list += listOf("-map_chapters", "-1")

        // force matroska format because we use mks file extension and ffmpeg does not know what to do with it
        list += listOf("-f", "matroska")

        return list
    }
}