/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.zip.GZIPInputStream;

/**
 * Reader of previously fetched request data
 */
public class RequestReader {

    private static final String TAG = "RequestReader";

    private final File handle;

    private final int version;

    /**
     * Create request reader
     *
     * @param file
     * @param formatVersion
     */
    public RequestReader(File file, int formatVersion) {
        handle = file;
        version = formatVersion;
    }

    /**
     * Read request data
     *
     * @return read data
     */
    @SuppressWarnings("unchecked")
    public <V> V read() {
        if (!handle.exists() || handle.length() == 0)
            return null;

        RandomAccessFile dir = null;
        FileLock lock = null;
        ObjectInputStream input = null;
        boolean delete = false;
        try {
            dir = new RandomAccessFile(handle, "rw");
            lock = dir.getChannel().lock();
            input = new ObjectInputStream(new GZIPInputStream(
                    new FileInputStream(dir.getFD()), 8192 * 8));
            int streamVersion = input.readInt();
            if (streamVersion != version) {
                delete = true;
                return null;
            }
            return (V) input.readObject();
        } catch (IOException e) {
            Log.d(TAG, "Exception reading cache " + handle.getName(), e);
            return null;
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Exception reading cache " + handle.getName(), e);
            return null;
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException e) {
                    Log.d(TAG, "Exception closing stream", e);
                }
            if (delete)
                try {
                    dir.setLength(0);
                } catch (IOException e) {
                    Log.d(TAG, "Exception truncating file", e);
                }
            if (lock != null)
                try {
                    lock.release();
                } catch (IOException e) {
                    Log.d(TAG, "Exception unlocking file", e);
                }
            if (dir != null)
                try {
                    dir.close();
                } catch (IOException e) {
                    Log.d(TAG, "Exception closing file", e);
                }
        }
    }
}
