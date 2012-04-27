package com.github.mobile;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.zip.GZIPOutputStream;

/**
 * Request writer
 */
public class RequestWriter {

    private static final String TAG = "GHRW";

    private final File handle;

    private final int version;

    /**
     * Create a request writer that writes to the given file
     *
     * @param file
     * @param formatVersion
     */
    public RequestWriter(File file, int formatVersion) {
        handle = file;
        version = formatVersion;
    }

    /**
     * Write request to file
     *
     * @param request
     * @return request
     */
    public <V> V write(V request) {
        RandomAccessFile dir = null;
        FileLock lock = null;
        ObjectOutputStream output = null;
        try {
            if (!handle.getParentFile().exists())
                handle.getParentFile().mkdirs();
            dir = new RandomAccessFile(handle, "rw");
            lock = dir.getChannel().lock();
            output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(dir.getFD()), 8192));
            output.writeInt(version);
            output.writeObject(request);
        } catch (IOException e) {
            Log.d(TAG, "Exception writing cache " + handle.getName(), e);
            return null;
        } finally {
            if (output != null)
                try {
                    output.close();
                } catch (IOException e) {
                    Log.d(TAG, "Exception closing stream", e);
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
        return request;
    }
}
