package com.github.mobile.android.util;

import static android.view.View.VISIBLE;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.File;

import roboguice.util.RoboAsyncTask;

/**
 * Avatar utilities
 */
public class Avatar {

    private static final String TAG = "GHAU";

    /**
     * Bind view to image at URL
     *
     * @param context
     * @param view
     * @param avatarUrl
     */
    public static void bind(final Context context, final ImageView view, final String avatarUrl) {
        if (avatarUrl == null)
            return;
        new RoboAsyncTask<File>(context) {

            public File call() throws Exception {
                HttpRequest request = HttpRequest.get(avatarUrl);
                if (!request.ok())
                    return null;
                File file = File.createTempFile("avatar", ".jpg", context.getFilesDir());
                request.receive(file);
                return file;
            }

            protected void onSuccess(File file) throws Exception {
                if (file == null || !file.exists() || file.length() == 0)
                    return;
                Bitmap bitmap = Image.getBitmap(file);
                if (bitmap != null) {
                    view.setImageBitmap(Image.roundCorners(bitmap, 6));
                    view.setVisibility(VISIBLE);
                }
            }

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Avatar load failed", e);
            }
        }.execute();
    }
}
