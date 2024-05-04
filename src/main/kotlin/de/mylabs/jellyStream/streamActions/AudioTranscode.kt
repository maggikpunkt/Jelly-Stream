package de.mylabs.jellyStream.streamActions

import de.mylabs.jellyStream.ffprobe.Stream

class AudioTranscode(override val stream: Stream, val kBitPerChannel: Int) : Transcode(stream) {
    override fun getCodec(index: Int): List<String> {
        when (stream.codec_name) {
            "ac3", "eac3", "dts" -> {
                //  "-ac:$index", "2",
                var br = kBitPerChannel * stream.channels!!
                val list = ArrayList<String>()
                list += listOf("-c:$index", "aac")

                if (stream.channel_layout == "5.1(side)") {
                    // AAC can not handle 5.1(side), this leads to audio problems:
                    // "Using a PCE to encode channel layout “5.1(side)”"
                    // Work around is down mixing to stereo
                    list += listOf("-ac:$index", "2")
                    br = kBitPerChannel * 2
                }

                list += listOf("-b:$index", "${br}k")
                return list
            }

            "aac", "mp3", "opus" -> {
                return listOf("-c:$index", "copy")
            }

            else -> {
                throw IllegalArgumentException()
            }
        }
    }
}