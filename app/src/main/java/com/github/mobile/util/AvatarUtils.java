package com.github.mobile.util;

import static android.graphics.Bitmap.CompressFormat.PNG;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.view.View.VISIBLE;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.mobile.R.drawable;
import com.github.mobile.R.id;
import com.google.inject.Inject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.eclipse.egit.github.core.User;

import roboguice.util.RoboAsyncTask;

/**
 * Avatar utilities
 */
public class AvatarUtils {

    private static final String TAG = "AvatarHelper";

    private static final float CORNER_RADIUS_IN_DIP = 6;

    private static final int CACHE_SIZE = 75;

    private static abstract class FetchAvatarTask extends RoboAsyncTask<BitmapDrawable> {

        private static final Executor EXECUTOR = Executors.newFixedThreadPool(2);

        private FetchAvatarTask(Context context) {
            super(context, EXECUTOR);
        }

        @Override
        protected void onException(Exception e) throws RuntimeException {
            Log.d(TAG, "Avatar load failed", e);
        }
    }

    private final float cornerRadius;

    private final Map<Integer, BitmapDrawable> loaded = new LinkedHashMap<Integer, BitmapDrawable>(CACHE_SIZE, 1.0F) {

        private static final long serialVersionUID = -4191624209581976720L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, BitmapDrawable> eldest) {
            return size() >= CACHE_SIZE;
        }
    };

    private final Context context;

    private final File avatarDir;

    private final Drawable loadingAvatar;

    /**
     * Create avatar helper
     *
     * @param context
     */
    @Inject
    public AvatarUtils(final Context context) {
        this.context = context;

        loadingAvatar = context.getResources().getDrawable(drawable.gravatar_icon);

        avatarDir = new File(context.getCacheDir(), "avatars/github.com");
        if (!avatarDir.isDirectory())
            avatarDir.mkdirs();

        float density = context.getResources().getDisplayMetrics().density;
        cornerRadius = CORNER_RADIUS_IN_DIP * density;
    }

    /**
     * Create bitmap from raw image and set to view
     *
     * @param image
     * @param view
     * @param user
     * @return this helper
     */
    protected AvatarUtils setImage(final BitmapDrawable image, final ImageView view, final User user) {
        if (!Integer.valueOf(user.getId()).equals(view.getTag(id.iv_gravatar)))
            return this;

        view.setTag(id.iv_gravatar, null);

        if (image != null) {
            loaded.put(user.getId(), image);
            view.setImageDrawable(image);
            view.setVisibility(VISIBLE);
        }

        return this;
    }

    /**
     * Get image for user
     *
     * @param user
     * @return image
     */
    protected BitmapDrawable getImage(final User user) {
        File avatarFile = new File(avatarDir, Integer.toString(user.getId()));

        if (!avatarFile.exists() || avatarFile.length() == 0)
            return null;

        Bitmap bitmap = decode(avatarFile);
        if (bitmap != null)
            return new BitmapDrawable(context.getResources(), bitmap);
        else {
            avatarFile.delete();
            return null;
        }
    }

    /**
     * Decode file to bitmap
     *
     * @param file
     * @return bitmap
     */
    protected Bitmap decode(final File file) {
        Options options = new Options();
        options.inDither = false;
        options.inPreferredConfig = ARGB_8888;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    /**
     * Fetch avatar from URL
     *
     * @param url
     * @param userId
     * @return bitmap
     */
    protected BitmapDrawable fetchAvatar(final String url, final Integer userId) {
        File rawAvatar = new File(avatarDir, userId.toString() + "-raw");
        HttpRequest request = HttpRequest.get(url);
        if (request.ok())
            request.receive(rawAvatar);

        if (!rawAvatar.exists() || rawAvatar.length() == 0)
            return null;

        Bitmap bitmap = decode(rawAvatar);
        if (bitmap == null) {
            rawAvatar.delete();
            return null;
        }

        bitmap = ImageUtils.roundCorners(bitmap, cornerRadius);
        if (bitmap == null) {
            rawAvatar.delete();
            return null;
        }

        File roundedAvatar = new File(avatarDir, userId.toString());
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(roundedAvatar);
            if (bitmap.compress(PNG, 100, output))
                return new BitmapDrawable(context.getResources(), bitmap);
            else
                return null;
        } catch (IOException e) {
            Log.d(TAG, "Exception writing rounded avatar", e);
            return null;
        } finally {
            if (output != null)
                try {
                    output.close();
                } catch (IOException e) {
                    // Ignored
                }
            rawAvatar.delete();
        }
    }

    /**
     * Sets the logo on the {@link ActionBar} to the user's avatar.
     *
     * @param actionBar
     * @param user
     * @return this helper
     */
    public AvatarUtils bind(final ActionBar actionBar, final User user) {
        if (user == null)
            return this;

        final String avatarUrl = user.getAvatarUrl();
        if (TextUtils.isEmpty(avatarUrl))
            return this;

        final Integer userId = Integer.valueOf(user.getId());

        BitmapDrawable loadedImage = loaded.get(userId);
        if (loadedImage != null) {
            actionBar.setLogo(loadedImage);
            return this;
        }

        new FetchAvatarTask(context) {

            @Override
            public BitmapDrawable call() throws Exception {
                synchronized (AvatarUtils.this) {
                    BitmapDrawable image = getImage(user);
                    if (image == null)
                        image = fetchAvatar(avatarUrl, userId);
                    return image;
                }
            }

            @Override
            protected void onSuccess(BitmapDrawable image) throws Exception {
                if (image != null)
                    actionBar.setLogo(image);
            }
        }.execute();

        return this;
    }

    /**
     * Bind view to image at URL
     *
     * @param view
     * @param user
     * @return this helper
     */
    public AvatarUtils bind(final ImageView view, final User user) {
        if (user == null) {
            view.setImageDrawable(loadingAvatar);
            return this;
        }

        final String avatarUrl = user.getAvatarUrl();
        if (TextUtils.isEmpty(avatarUrl)) {
            view.setImageDrawable(loadingAvatar);
            return this;
        }

        final Integer userId = Integer.valueOf(user.getId());

        BitmapDrawable loadedImage = loaded.get(userId);
        if (loadedImage != null) {
            view.setImageDrawable(loadedImage);
            view.setVisibility(VISIBLE);
            view.setTag(id.iv_gravatar, null);
            return this;
        }

        view.setImageDrawable(loadingAvatar);
        view.setTag(id.iv_gravatar, userId);

        new FetchAvatarTask(context) {

            @Override
            public BitmapDrawable call() throws Exception {
                if (!userId.equals(view.getTag(id.iv_gravatar)))
                    return null;

                synchronized (AvatarUtils.this) {
                    BitmapDrawable image = getImage(user);
                    if (image == null)
                        image = fetchAvatar(avatarUrl, userId);
                    return image;
                }
            }

            @Override
            protected void onSuccess(BitmapDrawable image) throws Exception {
                setImage(image, view, user);
            }

        }.execute();

        return this;
    }
}
