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
            if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS) {
                JarUtils.thisJar().ifPresent(path -> {
                    File jni = new File(path.getParent().toFile(), "hmcl.windows.dll");

                    try (InputStream inputStream = NativeLibraryLoader.class.getClassLoader().getResourceAsStream(String.format("jni-%s.dll", Architecture.CURRENT_ARCH.getDisplayName()))) {
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
            }
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "Fail to load JNI. HMCL may crash.", e);
        }

        loaded = true;
    }
}
