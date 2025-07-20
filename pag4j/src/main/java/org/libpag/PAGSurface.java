package org.libpag;

public class PAGSurface {

    public static PAGSurface MakeOffscreen(int width, int height) {
        long nativeSurface = SetupOffscreen(width, height);
        if (nativeSurface == 0) {
            return null;
        }
        return new PAGSurface(nativeSurface);
    }

    private static native long SetupOffscreen(int width, int height);

    private PAGSurface(long nativeSurface) {
        this.nativeSurface = nativeSurface;
    }

    /**
     * The width of surface in pixels.
     */
    public native int width();

    /**
     * The height of surface in pixels.
     */
    public native int height();

    /**
     * Update the size of surface, and reset the internal surface.
     */
    public native void updateSize();

    /**
     * Erases all pixels of this surface with transparent color. Returns true if the content has changed.
     */
    public native boolean clearAll();

    /**
     * Free the cache created by the surface immediately. Call this method can reduce memory pressure.
     */
    public native void freeCache();

    /**
     * Copies pixels from current PAGSurface to the specified bitmap.
     */
    public native boolean copyPixelsTo(byte[] pixels, int stride);

    /**
     * Free up resources used by the PAGSurface instance immediately instead of relying on the
     * garbage collector to do this for you at some point in the future.
     */
    public void release() {
        // Must call freeCache() here, otherwise, the cache may not be freed until the PAGPlayer is
        // garbage collected.
        freeCache();
        nativeRelease();
    }

    private native void nativeRelease();

    private static native void nativeInit();

    private native void nativeFinalize();

    protected void finalize() {
        nativeFinalize();
    }

    static {
        LibraryLoadUtils.loadLibrary("pag4j");
        nativeInit();
    }

    long nativeSurface = 0;
}
