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

import static android.view.View.VISIBLE;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.github.mobile.R.drawable;
import com.github.mobile.core.search.SearchUser;
import com.google.inject.Inject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.User;

/**
 * Avatar utilities
 */
public class AvatarLoader {

    private static final float CORNER_RADIUS_IN_DIP = 3;

    private final Drawable loadingAvatar;

    private final RoundCornersTransformation roundCorners;

    private ActionBarTarget actionBarTarget;

    private final Picasso picasso;

    private final Context context;

    /**
     * Create avatar helper
     *
     * @param context
     */
    @Inject
    public AvatarLoader(final Context context) {
        this.context = context;
        picasso = Picasso.with(context);

        loadingAvatar = context.getResources().getDrawable(drawable.gravatar_icon);

        float density = context.getResources().getDisplayMetrics().density;
        final float cornerRadius = CORNER_RADIUS_IN_DIP * density;
        roundCorners = new RoundCornersTransformation(cornerRadius);
    }

    /**
     * Sets the logo on the {@link ActionBar} to the user's avatar.
     *
     * @param actionBar
     * @param user
     * @return this helper
     */
    public AvatarLoader bind(final ActionBar actionBar, final User user) {
        return bind(actionBar, new AtomicReference<User>(user));
    }

    /**
     * Sets the logo on the {@link ActionBar} to the user's avatar.
     *
     * @param actionBar
     * @param userReference
     * @return this helper
     */
    public AvatarLoader bind(final ActionBar actionBar,
            final AtomicReference<User> userReference) {
        if (userReference == null)
            return this;

        final User user = userReference.get();
        final String avatarUrl = user.getAvatarUrl();
        if (TextUtils.isEmpty(avatarUrl))
            return this;

        if (actionBarTarget == null)
            actionBarTarget = new ActionBarTarget(actionBar);

        picasso.cancelRequest(actionBarTarget);
        picasso.load(avatarUrl).transform(roundCorners).into(actionBarTarget);

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

        final String avatarUrl = getAvatarUrl(user);
        if (TextUtils.isEmpty(avatarUrl))
            return setImage(loadingAvatar, view);

        loadAvatar(view, avatarUrl);

        return this;
    }

    /**
     * Bind view to image at URL
     *
     * @param view
     * @param user
     * @return this helper
     */
    public AvatarLoader bind(final ImageView view, final CommitUser user) {
        if (user == null)
            return setImage(loadingAvatar, view);

        final String avatarUrl = getAvatarUrl(user);
        if (TextUtils.isEmpty(avatarUrl))
            return setImage(loadingAvatar, view);

        loadAvatar(view, avatarUrl);

        return this;
    }

    /**
     * Bind view to image at URL
     *
     * @param view
     * @param contributor
     * @return this helper
     */
    public AvatarLoader bind(final ImageView view, final Contributor contributor) {
        if (contributor == null)
            return setImage(loadingAvatar, view);

        final String avatarUrl = contributor.getAvatarUrl();
        if (TextUtils.isEmpty(avatarUrl))
            return setImage(loadingAvatar, view);

        loadAvatar(view, avatarUrl);

        return this;
    }

    /**
     * Bind view to image at URL
     *
     * @param view
     * @param user
     * @return this helper
     */
    public AvatarLoader bind(final ImageView view, final SearchUser user) {
        if (user == null)
            return setImage(loadingAvatar, view);

        final String avatarUrl = getAvatarUrl(user.getGravatarId());
        if (TextUtils.isEmpty(avatarUrl))
            return setImage(loadingAvatar, view);

        loadAvatar(view, avatarUrl);

        return this;
    }

    private void loadAvatar(final ImageView view, final String avatarUrl) {
        picasso
            .load(avatarUrl)
            .transform(roundCorners)
            .placeholder(loadingAvatar)
            .into(view);
    }

    private AvatarLoader setImage(final Drawable image, final ImageView view) {
        view.setImageDrawable(image);
        view.setVisibility(VISIBLE);
        return this;
    }

    private String getAvatarUrl(String id) {
        if (!TextUtils.isEmpty(id))
            return "https://secure.gravatar.com/avatar/" + id + "?d=404";
        else
            return null;
    }

    private String getAvatarUrl(User user) {
        String avatarUrl = user.getAvatarUrl();
        if (TextUtils.isEmpty(avatarUrl)) {
            String gravatarId = user.getGravatarId();
            if (TextUtils.isEmpty(gravatarId))
                gravatarId = GravatarUtils.getHash(user.getEmail());
            avatarUrl = getAvatarUrl(gravatarId);
        }
        return avatarUrl;
    }

    private String getAvatarUrl(CommitUser user) {
        return getAvatarUrl(GravatarUtils.getHash(user.getEmail()));
    }

    private class ActionBarTarget implements Target {

        private final ActionBar actionBar;

        public ActionBarTarget(final ActionBar actionBar) {
            this.actionBar = actionBar;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            actionBar.setLogo(new BitmapDrawable(context.getResources(), bitmap));
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

    private class RoundCornersTransformation implements Transformation {

        private static final String TRANSFORMATION_KEY = "roundCorners()";

        private final float cornerRadius;

        public RoundCornersTransformation(float cornerRadius) {
            this.cornerRadius = cornerRadius;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            return ImageUtils.roundCorners(source, cornerRadius);
        }

        @Override
        public String key() {
            return TRANSFORMATION_KEY;
        }
    }
}
