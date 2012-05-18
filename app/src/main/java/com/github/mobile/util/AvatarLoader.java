/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.util;

import static android.graphics.Bitmap.CompressFormat.PNG;
import static android.graphics.Bitmap.Config.ARGB_8888;
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
public class AvatarLoader {

    private static final String TAG = "AvatarLoader";

    private static final float CORNER_RADIUS_IN_DIP = 6;

    private static final int CACHE_SIZE = 75;

    private static abstract class FetchAvatarTask extends RoboAsyncTask<BitmapDrawable> {

        private static final Executor EXECUTOR = Executors.newFixedThreadPool(1);

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
    public AvatarLoader(final Context context) {
        this.context = context;

        loadingAvatar = context.getResources().getDrawable(drawable.gravatar_icon);

        avatarDir = new File(context.getCacheDir(), "avatars/github.com");
        if (!avatarDir.isDirectory())
            avatarDir.mkdirs();

        float density = context.getResources().getDisplayMetrics().density;
        cornerRadius = CORNER_RADIUS_IN_DIP * density;
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
    public AvatarLoader bind(final ActionBar actionBar, final User user) {
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
                final BitmapDrawable image = getImage(user);
                if (image != null)
                    return image;
                else
                    return fetchAvatar(avatarUrl, userId);
            }

            @Override
            protected void onSuccess(BitmapDrawable image) throws Exception {
                actionBar.setLogo(image);
            }
        }.execute();

        return this;
    }

    private AvatarLoader setImage(final Drawable image, final ImageView view) {
        return setImage(image, view, null);
    }

    private AvatarLoader setImage(final Drawable image, final ImageView view, Object tag) {
        view.setImageDrawable(image);
        view.setTag(id.iv_avatar, tag);
        return this;
    }

    /**
     * Bind view to image at URL
     *
     * @param view
     * @param user
     * @return this helper
     */
    public AvatarLoader bind(final ImageView view, final User user) {
        if (user == null)
            return setImage(loadingAvatar, view);

        final String avatarUrl = user.getAvatarUrl();
        if (TextUtils.isEmpty(avatarUrl))
            return setImage(loadingAvatar, view);

        final Integer userId = Integer.valueOf(user.getId());

        BitmapDrawable loadedImage = loaded.get(userId);
        if (loadedImage != null)
            return setImage(loadedImage, view);

        setImage(loadingAvatar, view, userId);

        new FetchAvatarTask(context) {

            @Override
            public BitmapDrawable call() throws Exception {
                if (!userId.equals(view.getTag(id.iv_avatar)))
                    return null;

                final BitmapDrawable image = getImage(user);
                if (image != null)
                    return image;
                else
                    return fetchAvatar(avatarUrl, userId);
            }

            @Override
            protected void onSuccess(final BitmapDrawable image) throws Exception {
                if (image == null)
                    return;
                loaded.put(userId, image);
                if (userId.equals(view.getTag(id.iv_avatar)))
                    setImage(image, view);
            }

        }.execute();

        return this;
    }
}
