/////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Tencent is pleased to support the open source community by making libpag available.
//
//  Copyright (C) 2023 THL A29 Limited, a Tencent company. All rights reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
//  except in compliance with the License. You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  unless required by applicable law or agreed to in writing, software distributed under the
//  license is distributed on an "as is" basis, without warranties or conditions of any kind,
//  either express or implied. see the license for the specific language governing permissions
//  and limitations under the license.
//
/////////////////////////////////////////////////////////////////////////////////////////////////

package org.libpag;

public class PAGFile extends PAGComposition {

    public interface LoadListener {
        /**
         * Callback for asynchronous loading.
         * Returns null if the file does not exist or the data is not a pag file.
         */
        void onLoad(PAGFile result);
    }

    /**
     * The maximum tag level current SDK supports.
     */
    public static native int MaxSupportedTagLevel();

    /**
     * Load a pag file from the specified path, returns null if the file does not exist or the
     * data is not a pag file.
     * Note: All PAGFiles loaded by the same path share the same internal cache. The internal
     * cache is alive until all PAGFiles are released. Use 'PAGFile.Load(byte[])' instead
     * if you don't want to load a PAGFile from the intenal caches.
     */
    public static PAGFile Load(String path) {
        return LoadFromPath(path);
    }

    public static PAGFile Load(byte[] bytes) {
        return LoadFromBytes(bytes, bytes.length, "");
    }

    private static native PAGFile LoadFromPath(String path);

    private static native PAGFile LoadFromBytes(byte[] bytes, int limit, String path);

    private PAGFile(long nativeContext) {
        super(nativeContext);
    }

    /**
     * The tag level this pag file requires.
     */
    public native int tagLevel();

    /**
     * The number of editable texts.
     */
    public native int numTexts();

    /**
     * The number of replaceable images.
     */
    public native int numImages();

    /**
     * The number of video compositions.
     */
    public native int numVideos();

    /**
     * The path string of this file, returns empty string if the file is loaded from byte stream.
     */
    public native String path();

    /**
     * Indicate how to stretch the original duration to fit target duration when file's duration is changed.
     * The default value is PAGTimeStretchMode::Repeat.
     */
    public native int timeStretchMode();

    /**
     * Set the timeStretchMode of this file.
     */
    public native void setTimeStretchMode(int value);

    /**
     * Set the duration of this PAGFile. Passing a value less than or equal to 0 resets the duration to its default value.
     */
    public native void setDuration(long duration);

    /**
     * Make a copy of the original file, any modification to current file has no effect on the result file.
     */
    public native PAGFile copyOriginal();

    private static native final void nativeInit();

    static {
        LibraryLoadUtils.loadLibrary("pag4j");
        nativeInit();
    }
}
