package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream
import java.io.File

abstract class Extraction(val stream: Stream, val baseName: String) {
    abstract fun getFileExtension(): String

    abstract fun getCodec(index: Int): List<String>

    fun getName(): String {
        val nameParts: MutableList<String?> = ArrayList<String?>()
        nameParts.add(baseName)
        nameParts.add(
            stream.tags?.get("title")
                ?.replace('[', '(')
                ?.replace(']', ')')
                ?.replace(BAD_FILENAME_CHARACTERS, "_")
                ?.trim('_')
        )
        if (stream.disposition.forced == 1) nameParts.add("forced")
        if (stream.disposition.hearing_impaired == 1) nameParts.add("sdh")
        if (stream.disposition.default == 1) nameParts.add("default")
        nameParts.add(stream.getLanguage())
        nameParts.add(getFileExtension())
        return nameParts.filterNotNull().filter { it.isNotBlank() }.joinToString(separator = ".")
    }

    fun fileAlreadyExists(): Boolean {
        return File(getName()).exists()
    }

    fun getMapping(): List<String> {
        return listOf("-map", "0:${stream.index}")
    }

    companion object {
        val BAD_FILENAME_CHARACTERS = Regex("(?U)[^\\w()_ ]+")
    }
}
