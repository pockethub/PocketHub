package com.github.mobile.util;

import static android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE;
import static android.app.DownloadManager.COLUMN_STATUS;
import static android.app.DownloadManager.STATUS_SUCCESSFUL;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

import java.io.File;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.github.mobile.core.commit.CommitUtils;

/**
 * Getter for a file
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class GingerbreadFileGetter {

    private Context context;

    private String source;

    private File destination;

    private DownloadManager dm;

    private BroadcastReceiver receiver;

    private long downloadId;

    public interface OnDownloadCompleteListener {
        public abstract void onDownloadCompleted(boolean result, File file);
    }

    OnDownloadCompleteListener onDownloadCompleteListener;

    /**
     * Create file getter for context
     *
     * @param context
     * @param source
     */
    public GingerbreadFileGetter(Context context, String source) {
        this.context = context;
        this.source = source;
        this.destination = new File(
                Environment
                        .getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS),
                CommitUtils.getName(source));
        this.dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GingerbreadFileGetter.this.onReceive(context, intent);
            }
        };
        registerDownloadReceiver();
    }

    public void beginDownload() {
        Request request = new Request(Uri.parse(source));
        request.setDestinationUri(Uri.fromFile(destination));
        request.setVisibleInDownloadsUi(true);

        downloadId = dm.enqueue(request);
    }

    public void cancelDownload() {
        dm.remove(downloadId);
        unregisterDownloadReceiver();
    }

    public void setDownloadCompleteListener(OnDownloadCompleteListener listener) {
        onDownloadCompleteListener = listener;
    }

    private void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!ACTION_DOWNLOAD_COMPLETE.equals(action))
            return;

        Query query = new Query();
        query.setFilterById(downloadId);

        Cursor c = dm.query(query);
        if (!c.moveToFirst())
            return;

        int colStatus = c.getColumnIndex(COLUMN_STATUS);
        if (STATUS_SUCCESSFUL == c.getInt(colStatus)) {
            if (destination.exists())
                onDownloadCompleteListener.onDownloadCompleted(true,
                        destination);
            else
                onDownloadCompleteListener.onDownloadCompleted(false, null);
        } else {
            onDownloadCompleteListener.onDownloadCompleted(false, null);
        }

        unregisterDownloadReceiver();
    }

    private void registerDownloadReceiver() {
        context.registerReceiver(receiver, new IntentFilter(
                ACTION_DOWNLOAD_COMPLETE));
    }

    private void unregisterDownloadReceiver() {
        context.unregisterReceiver(receiver);
    }

}
