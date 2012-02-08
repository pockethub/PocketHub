package com.github.mobile.android.util;

import static android.view.View.VISIBLE;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.mobile.android.AccountDataManager;
import com.github.mobile.android.R.id;

import org.eclipse.egit.github.core.User;

import roboguice.util.RoboAsyncTask;

/**
 * Avatar utilities
 */
public class AvatarHelper {

    private static final int RADIUS = 8;

    private static final String TAG = "GHAU";

    private final AccountDataManager cache;

    /**
     * Create avatar helper
     *
     * @param cache
     */
    public AvatarHelper(final AccountDataManager cache) {
        this.cache = cache;
    }

    /**
     * Bind view to user
     *
     * @param view
     * @param user
     */
    public void bind(final ImageView view, final User user) {
        bind(view, user.getLogin(), user.getAvatarUrl());
    }

    /**
     * Bind view to image at URL
     *
     * @param view
     * @param login
     * @param avatarUrl
     */
    public void bind(final ImageView view, final String login, final String avatarUrl) {
        if (avatarUrl == null)
            return;

        byte[] image = cache.getAvatar(login);
        if (image != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            view.setImageBitmap(Image.roundCorners(bitmap, RADIUS));
            view.setVisibility(VISIBLE);
            return;
        }

        view.setTag(id.iv_gravatar, login);

        new RoboAsyncTask<byte[]>(cache.getContext()) {

            public byte[] call() throws Exception {
                if (!login.equals(view.getTag(id.iv_gravatar)))
                    return null;

                HttpRequest request = HttpRequest.get(avatarUrl);
                if (!request.ok())
                    return null;

                byte[] content = request.bytes();
                cache.setAvatar(login, content);
                return content;
            }

            protected void onSuccess(byte[] image) throws Exception {
                if (image == null || image.length == 0)
                    return;

                if (!login.equals(view.getTag(id.iv_gravatar)))
                    return;

                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                view.setImageBitmap(Image.roundCorners(bitmap, RADIUS));
                view.setVisibility(VISIBLE);
            }

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Avatar load failed", e);
            }
        }.execute();
    }
}
