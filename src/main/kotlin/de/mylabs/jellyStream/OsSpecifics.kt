package de.mylabs.jellyStream

enum class Os {
    WINDOWS,
    OTHER
}

object OsSpecifics {

    fun getOs(): Os {
        val osName = System.getProperty("os.name").lowercase()

        return when {
            "windows" in osName -> Os.WINDOWS
            listOf("mac", "nix", "sunos", "solaris", "bsd").any { it in osName } -> Os.OTHER
            else -> Os.OTHER
        }
    }
}
