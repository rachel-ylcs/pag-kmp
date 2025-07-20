package org.libpag;

public class LibraryLoadUtils {
    public static boolean loadLibrary(String libName) {
        if (libName == null || libName.isEmpty()) {
            return false;
        }
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                System.loadLibrary("libGLESv2");
                System.loadLibrary("libEGL");
            }
            System.loadLibrary(libName);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }
}
