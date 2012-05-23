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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.Integer.MAX_VALUE;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.text.TextUtils;
import android.widget.TextView;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.github.mobile.R.drawable;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import roboguice.util.RoboAsyncTask;

/**
 * Getter for an image
 */
public class HttpImageGetter implements ImageGetter {

    private static class LoadingImageGetter implements ImageGetter {

        private final Drawable image;

        private LoadingImageGetter(final Context context, final int size) {
            int imageSize = Math.round(context.getResources().getDisplayMetrics().density * size + 0.5F);
            image = context.getResources().getDrawable(drawable.image_loading_icon);
            image.setBounds(0, 0, imageSize, imageSize);
        }

        public Drawable getDrawable(String source) {
            return image;
        }
    }

    private static boolean containsImages(final String html) {
        return html.indexOf("<img") != -1;
    }

    private final LoadingImageGetter loading;

    private final Context context;

    private final File dir;

    private final int width;

    private final Map<Object, CharSequence> rawHtmlCache = new HashMap<Object, CharSequence>();

    private final Map<Object, CharSequence> fullHtmlCache = new HashMap<Object, CharSequence>();

    /**
     * Create image getter for context
     *
     * @param context
     */
    @Inject
    public HttpImageGetter(Context context) {
        this.context = context;
        dir = context.getCacheDir();
        width = ServiceUtils.getDisplayWidth(context);
        loading = new LoadingImageGetter(context, 24);
    }

    private HttpImageGetter show(final TextView view, final CharSequence html) {
        if (TextUtils.isEmpty(html))
            return hide(view);

        view.setText(html);
        view.setVisibility(VISIBLE);
        view.setTag(null);
        return this;
    }

    private HttpImageGetter hide(final TextView view) {
        view.setText(null);
        view.setVisibility(GONE);
        view.setTag(null);
        return this;
    }

    /**
     * Encode given HTML string and map it to the given id
     *
     * @param id
     * @param html
     * @return this image getter
     */
    public HttpImageGetter encode(final Object id, final String html) {
        if (TextUtils.isEmpty(html))
            return this;

        CharSequence encoded = HtmlUtils.encode(html, loading);
        // Use default encoding if no img tags
        if (containsImages(html))
            rawHtmlCache.put(id, encoded);
        else {
            rawHtmlCache.remove(id);
            fullHtmlCache.put(id, encoded);
        }
        return this;
    }

    /**
     * Bind text view to HTML string
     *
     * @param view
     * @param html
     * @param id
     * @return this image getter
     */
    public HttpImageGetter bind(final TextView view, final String html, final Object id) {
        if (TextUtils.isEmpty(html))
            return hide(view);

        CharSequence encoded = fullHtmlCache.get(id);
        if (encoded != null)
            return show(view, encoded);

        encoded = rawHtmlCache.get(id);
        if (encoded == null) {
            encoded = HtmlUtils.encode(html, loading);
            if (containsImages(html))
                rawHtmlCache.put(id, encoded);
            else {
                rawHtmlCache.remove(id);
                fullHtmlCache.put(id, encoded);
                return show(view, encoded);
            }
        }

        if (TextUtils.isEmpty(encoded))
            return hide(view);

        show(view, encoded);
        view.setTag(id);
        new RoboAsyncTask<CharSequence>(context) {

            @Override
            public CharSequence call() throws Exception {
                return HtmlUtils.encode(html, HttpImageGetter.this);
            }

            @Override
            protected void onSuccess(final CharSequence html) throws Exception {
                rawHtmlCache.remove(id);
                fullHtmlCache.put(id, html);

                if (id.equals(view.getTag()))
                    show(view, html);
            }
        }.execute();
        return this;
    }

    public Drawable getDrawable(String source) {
        File output = null;
        try {
            output = File.createTempFile("image", ".jpg", dir);
            synchronized (this) {
                HttpRequest request = HttpRequest.get(source);
                if (!request.ok())
                    throw new IOException("Unexpected response code: " + request.code());
                request.receive(output);
            }
            Bitmap bitmap = ImageUtils.getBitmap(output, width, MAX_VALUE);
            if (bitmap == null)
                return loading.getDrawable(source);

            BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            return drawable;
        } catch (IOException e) {
            return loading.getDrawable(source);
        } catch (HttpRequestException e) {
            return loading.getDrawable(source);
        } finally {
            if (output != null)
                output.delete();
        }
    }

}
