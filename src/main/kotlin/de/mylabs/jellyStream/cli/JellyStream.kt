package de.mylabs.jellyStream.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import de.mylabs.jellyStream.exceptions.CanNotProcessException
import de.mylabs.jellyStream.ffprobe.Ffprobe
import de.mylabs.jellyStream.ffprobe.FfprobeResult
import de.mylabs.jellyStream.ffprobe.Stream
import de.mylabs.jellyStream.streamActions.*
import mu.KotlinLogging
import java.io.File
import java.util.stream.Collectors
import kotlin.time.measureTimedValue

class JellyStream : CliktCommand(
    help = """ This script converts .mkv files into .mp4 files optimized for streaming. 
    Incompatible streams are transcoded and/or extracted to files compatible with jellyfin external file naming.  
    """,
    epilog = """
      **Container:** The "-movflag +faststart" flag is used to enable instant streaming.
      
      **Video:** Video is always copied and never transcoded.
      
      **Audio:** mp4-compatible audio is always copied and never transcoded. Incompatible audio is transcoded to AAC-LC. Incompatible surround sound audio is additionally extracted to external files. This preserves the original audio while increasing compatibility for the price of having to store an additional audio track.
       
      **Subtitles:** Subtitles are transcoded to mov_text if possible. More complex subtitles (currently all except subrip) are additionally extracted to external files. hdmv_pgs_subtitle subtitles are placed in a .mks container because I could not figure out which file extension jellyfin needs for them.
      
      **Others:** All other stream types are currently not supported and will lead to an error instead of being thrown out silently.
      
      **Cleaning:** Audio and subtitle stream titles containing key words that describe video steams like 1080p, x264 etc. can be automatically cleaned by removing the title. 
    """.trimIndent()
) {

    private val logger = KotlinLogging.logger {}

    init {
        context {
            helpFormatter = { MordantHelpFormatter(it, showDefaultValues = true) }
        }
    }

    val input by argument("input", help = "The input file or directory").path(true, true, true, false, true)
    val output by option(
        "-o",
        "--output",
        help = "The output directory. Defaults to the directory of the processed file"
    ).path(
        true,
        false,
        true,
        true,
        true
    )

    val move by option(
        "-m",
        "--move",
        help = "Move processed files to this directory. If not set files will not be moved"
    ).path(
        true,
        false,
        true,
        false,
        true
    )
    val recursive by option(help = "Search input directory recursively").switch(
        "-r" to true,
        "--recursive" to true
    ).default(false)
    val breakOnError by option(help = "Stops on the first file that could not be processed").switch(
        "-b" to true,
        "--breakOnError" to true,
        "--skipOnError" to false
    ).default(false)

    val extractStereo by option(
        help = "Extracts stereo audio. By default only surround sound audio is extracted"
    ).switch(
        "--extractStereo" to true
    ).default(false)

    val kBitPerChannel by option(
        "--kBitPerChannel",
        help = "The Bitrate per audio channel in kBit/s"
    ).int()
        .default(64)
        .check("value must be even") { it in 16..512 }

    val loglevel by option(
        "--loglevel",
        help = "Sets the FFmpeg loglevel. Refer to the FFmpeg documentation"
    ).choice(
        "quiet",
        "panic",
        "fatal",
        "error",
        "warning",
        "info",
        "verbose",
        "debug",
        "trace"
    ).default("warning")
    val stats by option(help = "Sets the FFmpeg 'stats' or 'nostats' flag").switch(
        "--stats" to "stats",
        "--nostats" to "nostats"
    ).default("stats")

    val copyLastModified by option(
        help = "Sets the last modified attribute of " +
                "the new file based on the original file"
    ).switch(
        "--copyLastModified" to true,
        "--newLastModified" to false
    ).default(true)

    val cleanAudioStreamTitles by option(
        help = "Removes the title of audio streams if the title seems wrong. (See section 'Cleaning' below)"
    ).switch(
        "--keepAllAudioStreamTitles" to false,
        "--cleanAudioStreamTitles" to true
    ).default(true)

    val cleanSubtitleStreamTitles by option(
        help = "Removes the title of subtitle streams if the title seems wrong. (See section 'Cleaning' below)"
    ).switch(
        "--keepAllSubtitleStreamTitles" to false,
        "--cleanSubtitleStreamTitles" to true
    ).default(true)

    val guessSubtitleFlags by option(
        help = "Tries to guess subtitle dispositions for hearing_impaired or forced from the stream title"
    ).switch(
        "--guessSubtitleDispositions" to true,
    ).default(false)

    val dryRun by option(
        help = "Do not actually call ffmpeg but show output"
    ).switch(
        "--dryRun" to true
    ).default(false)

    val ffpmegLocation by option("--ffmpeg", help = "Path to the FFmpeg executable").default("ffmpeg")
    val ffprobeLocation by option("--ffprobe", help = "Path to the FFprobe executable").default("ffprobe")

    override fun run() {
        val inputFile = input.toFile()

        val files = ArrayList<File>()
        if (inputFile.isDirectory) {
            print("Searching in '$inputFile'")
            if (recursive) {
                print(" recursively")
                files.addAll(inputFile.walk().filter { it.isFile && it.extension == "mkv" })
            } else {
                files.addAll(inputFile.listFiles().filter { it.isFile && it.extension == "mkv" })
            }
            print("\n")

            if (files.isEmpty()) {
                System.err.println("No .mkv files found!")
            } else {
                println("Found:")
                for (file in files) {
                    println(" - ${inputFile.toPath().relativize(file.toPath())}")
                }
            }

            val errors = ArrayList<String>()
            val successes = ArrayList<String>()
            for (file in files) {
                val relativePath = inputFile.toPath().relativize(file.toPath()).toString()
                try {
                    processFile(file, output?.toFile() ?: file.parentFile)
                    successes.add(relativePath)
                } catch (e: CanNotProcessException) {
                    errors.add("$relativePath - \t${e.message}")
                    if (breakOnError) {
                        System.err.println("Breaking on file: $relativePath")
                        System.err.println("${e.message}")
                        break
                    } else {
                        System.err.println("Skipping $relativePath: ${e.message}")
                    }
                }
            }
            if (successes.isNotEmpty()) {
                println("\nSuccessfully processed:")
                successes.forEach { println(" - $it") }
            }
            if (errors.isNotEmpty()) {
                println("\nErrors:")
                errors.forEach { println(" - $it") }
            }
        } else if (inputFile.isFile) {
            try {
                processFile(inputFile, output?.toFile() ?: inputFile.parentFile)
            } catch (e: CanNotProcessException) {
                System.err.println(e.message)
            }
        } else if (!inputFile.exists()) {
            System.err.println("$inputFile does not exists")
        } else {
            System.err.println("What even is $input")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun processFile(inputFile: File, outputDirectory: File) {
        println("\nChecking '$inputFile'")

        exitWhenNot(inputFile.extension == "mkv", "File extension mut be mkv")
        exitWhen(!outputDirectory.exists(), "$outputDirectory must exist")
        val baseName = outputDirectory.path + File.separator + inputFile.nameWithoutExtension
        val newContainer = File(baseName + ".mp4")

        exitWhen(newContainer.exists(), "Skipping ${inputFile.name} '$newContainer' already exists")
        println("New file: '$newContainer'")

        var renameTarget: File? = null

        move?.let {
            val rt = File(it.toFile().absolutePath + File.separator + inputFile.name)
            exitWhen(
                rt.exists(),
                "Skipping ${inputFile.name}: Can not move original file to '${rt.absolutePath}' because it already exists"
            )
            renameTarget = rt
        }

        val ffprobeResult =
            try {
                ffprobe.probe(inputFile, ffprobeLocation)
            } catch (e: Exception) {
                logger.error { e }
                throw CanNotProcessException("FFprobe error")
            }

        exitWhenNot(ffprobeResult.format.format_name == "matroska,webm", "File is not a mkv file")

        checkStreams(ffprobeResult)

        val streams: Map<String, List<Stream>> =
            ffprobeResult.streams.stream().collect(Collectors.groupingBy { it.codec_type })

        val videoStream = checkVideoStream(streams)

        val transcodes = ArrayList<Transcode>()
        val extractions = ArrayList<Extraction>()

        if (guessSubtitleFlags) {
            for (stream in streams[Stream.SUBTITLE] ?: emptyList()) {
                stream.guessDisposition()
                if (stream.disposition.forced == 0 && (stream.guessedDisposition?.forced ?: 0) == 1) {
                    println("Guessing that ${stream.getName()} is forced because the title is ${stream.getTitle()}")
                }
                if (stream.disposition.hearing_impaired == 0 && (
                            stream.guessedDisposition?.hearing_impaired
                                ?: 0
                            ) == 1
                ) {
                    println(
                        "Guessing that ${stream.getName()} is hearing_impaired because the title is ${stream.getTitle()}"
                    )
                }
            }
        }

        scanAudioStreams(streams[Stream.AUDIO] ?: emptyList(), baseName).let {
            transcodes += it.first
            extractions += it.second
        }

        scanSubtitleStreams(streams[Stream.SUBTITLE] ?: emptyList(), baseName).let {
            transcodes += it.first
            extractions += it.second
        }

        for (extraction in extractions) {
            exitWhen(extraction.fileAlreadyExists(), "${extraction.getName()} already exists.")
        }

        val command = assembleCommand(videoStream, inputFile, newContainer, transcodes, extractions)

        if (loglevel in listOf(
                "info",
                "verbose",
                "debug",
                "trace"
            ) || dryRun
        ) {
            print("Command: ")
            println(command)
        } else {
            println("Processing...")
        }

        if (!dryRun) {
            val (result, timeTaken) = measureTimedValue {
                ProcessBuilder(command)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start()
                    .waitFor()
            }

            if (copyLastModified) {
                if (!newContainer.setLastModified(inputFile.lastModified())) {
                    println("Could not override last modified attribute.")
                }
            }

            if (result == 0) {
                println("Done. Processing time: $timeTaken")
                renameTarget?.let {
                    print("Moving '${inputFile.name}' to '${it.parentFile.absolutePath}'...")
                    inputFile.renameTo(renameTarget)
                    println(" Moved.")
                }
            } else {
                exit("FFmpeg returned with $result")
            }
        }
    }

    private fun scanAudioStreams(streams: List<Stream>, baseName: String): Pair<List<Transcode>, List<Extraction>> {
        val transcodes = ArrayList<Transcode>()
        val extractions = ArrayList<Extraction>()

        for (stream in streams) {
            when (stream.codec_name) {
                "ac3", "eac3", "dts", "vorbis" -> {
                    transcodes.add(AudioTranscode(stream, cleanAudioStreamTitles, kBitPerChannel))
                    if ((stream.channels!!) > 2 || extractStereo) {
                        extractions.add(AudioExtraction(stream, baseName, cleanAudioStreamTitles))
                    }
                }

                "aac", "mp3", "opus" -> {
                    transcodes.add(AudioTranscode(stream, cleanAudioStreamTitles, kBitPerChannel))
                }

                else -> {
                    unsupportedStream(stream)
                }
            }
        }

        return Pair(transcodes, extractions)
    }

    private fun scanSubtitleStreams(streams: List<Stream>, baseName: String): Pair<List<Transcode>, List<Extraction>> {
        val transcodes = ArrayList<Transcode>()
        val extractions = ArrayList<Extraction>()

        for (stream in streams) {
            when (stream.codec_name) {
                "ass" -> {
                    transcodes.add(SubtitleTranscode(stream, cleanSubtitleStreamTitles))
                    extractions.add(SubtitleExtraction(stream, baseName, cleanSubtitleStreamTitles))
                }

                "subrip", "dvd_subtitle" -> {
                    transcodes.add(SubtitleTranscode(stream, cleanSubtitleStreamTitles))
                }

                "hdmv_pgs_subtitle", "dvb_subtitle" -> {
                    extractions.add(SubtitleExtraction(stream, baseName, cleanSubtitleStreamTitles))
                }

                else -> {
                    unsupportedStream(stream)
                }
            }
        }

        return Pair(transcodes, extractions)
    }

    private fun assembleCommand(
        videoStream: Stream,
        inputFile: File,
        outputFile: File,
        transcodes: MutableList<Transcode>,
        extractions: MutableList<Extraction>
    ): List<String> {
        val command = ArrayList<String>()

        command.addAll(listOf(ffpmegLocation, "-hide_banner", "-loglevel", loglevel, "-$stats"))
        command.addAll(
            listOf(
                "-probesize",
                "300M",
                "-i",
                inputFile.path,
                "-map_metadata",
                "0",
                "-map",
                "0:${videoStream.index}",
                "-c:0",
                "copy"
            )
        )

        println("Copying ${videoStream.getName()}")

        transcodes.sortBy { it.stream.index }
        extractions.sortBy { it.stream.index }

        var i = 1
        for (transcode in transcodes) {
            val codec = transcode.getCodec(i)
            if (codec[1].lowercase() == "copy") {
                print("Copying ${transcode.stream.getName()}")
            } else {
                print("Transcoding ${transcode.stream.getName()} to ${codec[1]}")
            }
            if (transcode.titleNeedsCleaning()) {
                print(" and cleaning the title")
            }
            println()
            command.addAll(transcode.getMapping())
            command.addAll(transcode.getCodec(i))
            i++
        }

        command.addAll(listOf("-movflags", "+faststart", outputFile.path))

        for (extraction in extractions) {
            print("Extracting ${extraction.stream.getName()} to ${extraction.getName()}")
            if (extraction.titleNeedsCleaning()) {
                print(" (without the title)")
            }
            println()
            command.addAll(extraction.getMapping())
            command.addAll(extraction.getCodec(0))
            command.add(extraction.getName())
        }

        return command
    }

    private fun checkStreams(ffprobeResult: FfprobeResult) {
        val otherStream = ffprobeResult.streams
            .find {
                it.codec_type !in arrayOf(Stream.AUDIO, Stream.VIDEO, Stream.SUBTITLE)
            }
        if (otherStream != null) {
            unsupportedStream(otherStream)
        }
    }

    private fun checkVideoStream(streams: Map<String, List<Stream>>): Stream {
        when (
            streams[Stream.VIDEO]?.count() ?: 0
        ) {
            0 -> exit("No video stream found")
            1 -> {}
            else -> exit("Too many video streams found")
        }

        val videoStream = streams[Stream.VIDEO]!![0]

        if (videoStream.codec_name !in arrayOf("hevc", "h264")) {
            unsupportedStream(videoStream)
        }

        return videoStream
    }

    private fun exitWhenNot(checkResult: Boolean, message: String) {
        exitWhen(!checkResult, message)
    }

    private fun exitWhen(checkResult: Boolean, message: String) {
        if (checkResult) {
            exit(message)
        }
    }

    private fun unsupportedStream(stream: Stream): Nothing {
        exit("Stream ${stream.getName()} unsupported")
    }

    private fun exit(message: String): Nothing {
        throw CanNotProcessException(message)
    }

    companion object {
        val ffprobe = Ffprobe
    }
}
