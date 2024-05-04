package de.mylabs

import de.mylabs.jellyStream.Os
import de.mylabs.jellyStream.OsSpecifics
import de.mylabs.jellyStream.cli.JellyStream
import de.mylabs.jellyStream.nativeHelper.WindowsWorkaround

fun main(args: Array<String>) {
    var realArgs: Array<String> = args

    if (OsSpecifics.getOs() == Os.WINDOWS) {
        val workaround = WindowsWorkaround()
        realArgs = workaround.getCommandLineArguments(args, "de.mylabs.MainKt")
    } else {
        println(
            "Not running on Windows. No unicode parameter workaround used. If problems with unicode filenames occur report it to github project. Good luck."
        )
    }

    JellyStream().main(realArgs)
}
