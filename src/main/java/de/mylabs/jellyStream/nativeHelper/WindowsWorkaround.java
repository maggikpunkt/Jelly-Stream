package de.mylabs.jellyStream.nativeHelper;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Because Windows and the JVM don't like Unicode Command Line arguments but I have files with unicode names
// Stolen from: https://stackoverflow.com/a/41923480

@SuppressWarnings("all")
public class WindowsWorkaround {
    private static final Logger log = LoggerFactory.getLogger(WindowsWorkaround.class);

    private Kernel32 kernel32;
    private Shell32 shell32;

    public String[] getCommandLineArguments(String[] fallBackTo, String mainClass) {
        try {
            log.debug("In case we fail fallback would happen to: " + Arrays.toString(fallBackTo));
            String[] ret = getFullCommandLine();
            log.debug("According to Windows API programm was started with arguments: " + Arrays.toString(ret));

            List<String> argsOnly = null;
            for (String s : ret) {
                if (s.equals(mainClass) && argsOnly != null && argsOnly.isEmpty()) { // For running it in IntelliJ
                } else if (argsOnly != null) {
                    argsOnly.add(s);
                } else if (s.toLowerCase().endsWith(".jar")) {
                    argsOnly = new ArrayList<>();
                }
            }
            if (argsOnly != null) {
                ret = argsOnly.toArray(new String[0]);
            }

            log.debug("These arguments will be used: " + Arrays.toString(ret));
            return ret;
        } catch (Throwable t) {
            log.error("Failed to use JNA to get current program command line arguments", t);
            return fallBackTo;
        }
    }

    private String[] getFullCommandLine() {
        try {
            // int pid = kernel32.GetCurrentProcessId();
            IntByReference argc = new IntByReference();
            Pointer argv_ptr = getShell32().CommandLineToArgvW(getKernel32().GetCommandLineW(), argc);
            String[] argv = argv_ptr.getWideStringArray(0, argc.getValue());
            getKernel32().LocalFree(argv_ptr);
            return argv;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to get program arguments using JNA", t);
        }
    }

    private Kernel32 getKernel32() {
        if (kernel32 == null) {
            kernel32 = Native.loadLibrary("kernel32", Kernel32.class);
        }
        return kernel32;
    }

    private Shell32 getShell32() {
        if (shell32 == null) {
            shell32 = Native.loadLibrary("shell32", Shell32.class);
        }
        return shell32;
    }

}

interface Kernel32 extends StdCallLibrary {
    int GetCurrentProcessId();

    WString GetCommandLineW();

    Pointer LocalFree(Pointer pointer);
}

interface Shell32 extends StdCallLibrary {
    Pointer CommandLineToArgvW(WString command_line, IntByReference argc);
}