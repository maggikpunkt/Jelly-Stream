package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream
import java.io.File

abstract class Extraction(val stream: Stream, val baseName: String, val cleanTitle: Boolean) {
    abstract fun getFileExtension(): String

    abstract fun getCodec(index: Int): List<String>

    fun getName(): String {
        val nameParts: MutableList<String?> = ArrayList<String?>()
        nameParts.add(baseName)
        if (!titleNeedsCleaning()) {
            nameParts.add(
                stream.getTitle()
                    ?.replace('[', '(')
                    ?.replace(']', ')')
                    ?.replace(BAD_FILENAME_CHARACTERS, "_")
                    ?.trim('_')
            )
        }
        if (stream.disposition.forced == 1 || stream.guessedDisposition?.forced == 1) nameParts.add("forced")
        if (stream.disposition.hearing_impaired == 1 || stream.guessedDisposition?.hearing_impaired == 1) nameParts.add(
            "sdh"
        )
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

    fun titleNeedsCleaning(): Boolean = titleNeedsCleaning(stream.getTitle() ?: "")

    companion object {
        val BAD_FILENAME_CHARACTERS = Regex("(?U)[^\\w()_ ]+")
    }
}
