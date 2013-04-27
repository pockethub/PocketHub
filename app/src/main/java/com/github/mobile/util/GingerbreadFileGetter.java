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
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.Gravity;
import android.widget.Toast;

import com.github.mobile.core.commit.CommitUtils;
import com.github.mobile.ui.LightProgressDialog;

/**
 * Getter for a file
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class GingerbreadFileGetter {

    private Context context;

    private String source;

    private File destination;

    private LightProgressDialog progress;

    private DownloadManager dm;

    private BroadcastReceiver receiver;

    private long downloadId;

    /**
     * Listener for {@link GingerbreadFileGetter} complete/cancelled downloads
     */
    public interface FileGetterListener {
        /**
         * Callback for when download is complete
         *
         * @param success
         * @param file
         */
        public abstract void onDownloadComplete(boolean success, File file);

        /**
         * Callback for when download is cancelled
         */
        public abstract void onDownloadCancel();
    }

    FileGetterListener fileGetterLisnener;

    private static LightProgressDialog createProgressDialog(Context context,
            String title) {
        LightProgressDialog dialog = (LightProgressDialog) LightProgressDialog
                .create(context, title);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    /**
     * Create file getter for context
     *
     * @param context
     * @param source
     */
    public GingerbreadFileGetter(Context context, String source) {
        this.context = context;
        this.source = source;
        this.destination = getDownloadDestination(CommitUtils.getName(source));
        this.progress = (LightProgressDialog) LightProgressDialog.create(
                context, "Downloading\n" + CommitUtils.getName(source) + "...");
        this.dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GingerbreadFileGetter.this.onReceive(context, intent);
            }
        };
    }

    /**
     * Begin downloading the specified file
     */
    public void beginDownload() {
        if (destination == null) {
            progress.dismiss();
            fileGetterLisnener.onDownloadComplete(false, null);
            return;
        }

        registerDownloadReceiver();

        String title = "Downloading\n" + CommitUtils.getName(source) + "...";
        progress = createProgressDialog(context, title);
        progress.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                GingerbreadFileGetter.this.cancelDownload();
            }
        });
        Request request = new Request(Uri.parse(source));
        request.setDestinationUri(Uri.fromFile(destination));
        request.setVisibleInDownloadsUi(true);

        progress.show();
        downloadId = dm.enqueue(request);
    }

    /**
     * Cancel currently downloading file and remove it from the download
     * manager. If there is a downloaded file, partial or complete, it is
     * deleted.
     */
    public void cancelDownload() {
        progress.dismiss();

        Toast toast = Toast.makeText(context, "Download cancelled",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        dm.remove(downloadId);
        unregisterDownloadReceiver();

        fileGetterLisnener.onDownloadCancel();
    }

    /**
     * Set the {@link FileGetterListener}
     *
     * @param listener
     */
    public void setFileGetterListener(FileGetterListener listener) {
        fileGetterLisnener = listener;
    }

    private void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!ACTION_DOWNLOAD_COMPLETE.equals(action))
            return;

        progress.dismiss();

        Query query = new Query();
        query.setFilterById(downloadId);

        Cursor c = dm.query(query);
        if (!c.moveToFirst())
            return;

        int colStatus = c.getColumnIndex(COLUMN_STATUS);
        if (STATUS_SUCCESSFUL == c.getInt(colStatus)) {
            if (destination.exists())
                fileGetterLisnener.onDownloadComplete(true, destination);
            else
                fileGetterLisnener.onDownloadComplete(false, null);
        } else {
            fileGetterLisnener.onDownloadComplete(false, null);
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

    private File getDownloadDestination(String file) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
            return new File(
                    Environment
                            .getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS),
                    file);
        else
            return null;
    }

}
