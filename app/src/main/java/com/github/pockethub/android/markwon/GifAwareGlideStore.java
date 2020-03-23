package com.github.pockethub.android.markwon;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.glide.GlideImagesPlugin;

class GifAwareGlideStore implements GlideImagesPlugin.GlideStore {
    private final Context context;

    public GifAwareGlideStore(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RequestBuilder<Drawable> load(@NonNull AsyncDrawable drawable) {
        return Glide.with(context)
                .load(drawable.getDestination())
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource instanceof Animatable) {
                            ((Animatable) resource).start();
                        }
                        return false;
                    }
                });
    }

    @Override
    public void cancel(@NonNull Target<?> target) {
        Glide.with(context).clear(target);
    }
}
