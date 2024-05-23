package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

class AudioTranscode(override val stream: Stream, override val cleanTitle: Boolean, val kBitPerChannel: Int) :
    Transcode(stream, cleanTitle) {
    override fun getCodec(index: Int): List<String> {
        val list = ArrayList<String>()

        when (stream.codec_name) {
            "ac3", "eac3", "dts", "vorbis" -> {
                //  "-ac:$index", "2",
                var br = kBitPerChannel * stream.channels!!

                list += listOf("-c:$index", "aac")

                if (stream.channel_layout == "5.1(side)" || (stream.channels > 2 && stream.channel_layout.isNullOrEmpty())) {
                    // AAC can not handle 5.1(side), this leads to audio problems:
                    // "Using a PCE to encode channel layout “5.1(side)”"
                    // Work around is down mixing to stereo
                    list += listOf("-ac:$index", "2")
                    br = kBitPerChannel * 2
                }

                list += listOf("-b:$index", "${br}k")

            }

            "aac", "mp3", "opus" -> {
                list += listOf("-c:$index", "copy")
            }

            else -> {
                throw IllegalArgumentException("Codec unknown")
            }
        }

        if (titleNeedsCleaning()) {
            list += listOf("-metadata:s:$index", "title=")
        }

        return list
    }
}