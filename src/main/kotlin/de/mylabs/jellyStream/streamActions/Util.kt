package de.mylabs.jellyStream.streamActions

fun titleNeedsCleaning(name: String): Boolean {
    for (word in listOf("1080p", "WEBRip", "x265", "x264", "720p", "BluRay")) {
        if (name.contains(word, true)) {
            return true
        }
    }
    return false
}
