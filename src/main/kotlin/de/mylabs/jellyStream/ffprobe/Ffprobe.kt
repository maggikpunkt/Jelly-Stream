package de.mylabs.jellyStream.ffprobe

import com.google.gson.Gson
import java.io.File

object Ffprobe {
    val gson: Gson = Gson()

    fun probe(file: File, ffprobe: String): FfprobeResult {
        val process = ProcessBuilder(
            ffprobe,
            "-v",
            "quiet",
            "-print_format",
            "json",
            "-show_format",
            "-show_streams",
            "-show_chapters",
            file.absolutePath
        ).redirectOutput(ProcessBuilder.Redirect.PIPE).redirectError(ProcessBuilder.Redirect.INHERIT).start()

        val json = process.inputReader().readText()
        process.waitFor()
        return gson.fromJson(json, FfprobeResult::class.java)
    }
}
