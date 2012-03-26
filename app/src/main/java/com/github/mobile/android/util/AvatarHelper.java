package com.github.mobile.android.util;

import static android.view.View.VISIBLE;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.mobile.android.persistence.AccountDataManager;
import com.github.mobile.android.R.id;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.egit.github.core.User;

import roboguice.util.RoboAsyncTask;

/**
 * Avatar utilities
 */
public class AvatarHelper {

    private static final int RADIUS = 8;

    private static final String TAG = "GHAU";

    private final Map<String, Bitmap> loaded = new ConcurrentHashMap<String, Bitmap>();

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
     * Create bitmap from raw image and set to view
     *
     * @param image
     * @param view
     * @param login
     * @return this helper
     */
    protected AvatarHelper setImage(final byte[] image, final ImageView view, final String login) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        Bitmap rounded = Image.roundCorners(bitmap, RADIUS);
        loaded.put(login, rounded);
        view.setImageBitmap(rounded);
        view.setVisibility(VISIBLE);
        return this;
    }

    /**
     * Bind view to image at URL
     *
     * @param view
     * @param login
     * @param avatarUrl
     * @return this helper
     */
    public AvatarHelper bind(final ImageView view, final String login, final String avatarUrl) {
        if (avatarUrl == null)
            return this;

        Bitmap loadedImage = loaded.get(login);
        if (loadedImage != null) {
            view.setImageBitmap(loadedImage);
            view.setVisibility(VISIBLE);
            return this;
        }

        byte[] image = cache.getAvatar(login);
        if (image != null)
            return setImage(image, view, login);

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

                setImage(image, view, login);
            }

            protected void onException(Exception e) throws RuntimeException {
                Log.d(TAG, "Avatar load failed", e);
            }
        }.execute();

        return this;
    }
}
