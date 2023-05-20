package org.jackhuang.hmcl.util;

import org.jackhuang.hmcl.util.io.JarUtils;
import org.jackhuang.hmcl.util.platform.Architecture;
import org.jackhuang.hmcl.util.platform.OperatingSystem;

import java.io.*;
import java.util.logging.Level;

import static org.jackhuang.hmcl.util.Logging.LOG;

public final class NativeLibraryLoader {
    public static boolean isProcessIdentityState() {
        return processIdentityState;
    }

    private static boolean processIdentityState = false;

    private NativeLibraryLoader() {
    }

    private static boolean loaded = false;

    public static void loadJNI() {
        if (loaded) {
            return;
        }

        try {
            JarUtils.thisJar().ifPresent(path -> {
                String sharedLibraryType;
                if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS) {
                    sharedLibraryType = "dll";
                } else if (OperatingSystem.CURRENT_OS == OperatingSystem.LINUX || OperatingSystem.CURRENT_OS == OperatingSystem.OSX) {
                    sharedLibraryType = "so";
                } else {
                    sharedLibraryType = "unknown";
                }
                File jni = new File(path.getParent().toFile(), String.format("hmcl.jni.%s", sharedLibraryType));

                try (InputStream inputStream = NativeLibraryLoader.class.getClassLoader().getResourceAsStream(String.format("native/jni-%s.%s", Architecture.CURRENT_ARCH.getDisplayName(), sharedLibraryType))) {
                    if (inputStream != null) {
                        try (FileOutputStream fileOutputStream = new FileOutputStream(jni)) {
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = inputStream.read(buffer, 0, 8192)) >= 0) {
                                fileOutputStream.write(buffer, 0, read);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.load(jni.getAbsolutePath());

                LOG.log(Level.INFO, String.format("Load JNI from \"%s\".", jni.getAbsolutePath()));
                processIdentityState = true;
            });
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "Fail to load JNI. HMCL may crash.", e);
        }

        loaded = true;
    }
}
