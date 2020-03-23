package com.github.pockethub.android.markwon;

import android.graphics.Picture;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.caverock.androidsvg.SVG;
/**
 * Convert the {@link SVG}'s internal representation to an Android-compatible one ({@link Picture}).
 */
public class SvgDrawableTranscoder implements ResourceTranscoder<SVG, Drawable> {
    @Nullable
    @Override
    public Resource<Drawable> transcode(@NonNull Resource<SVG> toTranscode, @NonNull Options options) {
        SVG svg = toTranscode.get();
        float ratio = svg.getDocumentAspectRatio();
        int width = svg.getDocumentWidth() > 0 ? (int) svg.getDocumentWidth() : 1024;
        int height = (int) (svg.getDocumentHeight() > 0 ? svg.getDocumentHeight() : (width / ratio));
        Picture picture = svg.renderToPicture(width, height);
        PictureDrawable drawable = new PictureDrawable(picture);
        return new SimpleResource<>(drawable);
    }
}
