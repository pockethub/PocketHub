/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html.ImageGetter;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.request.RequestMarkdownDTO;
import com.alorma.github.sdk.bean.dto.response.Content;
import com.alorma.github.sdk.bean.info.FileInfo;
import com.alorma.github.sdk.bean.info.RepoInfo;
import com.alorma.github.sdk.services.content.GetFileContentClient;
import com.alorma.github.sdk.services.content.GetMarkdownClient;
import com.alorma.github.sdk.services.user.GetAuthUserClient;
import com.bugsnag.android.Bugsnag;
import com.github.pockethub.R;
import com.github.pockethub.rx.ObserverAdapter;
import com.google.inject.Inject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.util.Base64.DEFAULT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.lang.Integer.MAX_VALUE;

/**
 * Getter for an image
 */
public class HttpImageGetter implements ImageGetter {

    private static class LoadingImageGetter implements ImageGetter {

        private final Drawable image;

        private LoadingImageGetter(final Context context, final int size) {
            int imageSize = ServiceUtils.getIntPixels(context, size);
            image = context.getResources().getDrawable(
                    R.drawable.image_loading_icon);
            image.setBounds(0, 0, imageSize, imageSize);
        }

        @Override
        public Drawable getDrawable(String source) {
            return image;
        }
    }

    private static boolean containsImages(final String html) {
        return html.contains("<img");
    }

    private static final String HOST_DEFAULT = "github.com";

    private final LoadingImageGetter loading;

    private final Context context;

    private final File dir;

    private final int width;

    private final Map<Object, CharSequence> rawHtmlCache = new HashMap<>();

    private final Map<Object, CharSequence> fullHtmlCache = new HashMap<>();

    private final OkHttpClient okHttpClient;

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
        okHttpClient = new OkHttpClient();
    }

    private HttpImageGetter show(final TextView view, final CharSequence html) {
        if (TextUtils.isEmpty(html))
            return hide(view);

        view.setText(trim(html));
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

    //All comments end with "\n\n" removing 2 chars
    private CharSequence trim(CharSequence val){
        if(val.charAt(val.length()-1) == '\n' && val.charAt(val.length()-2) == '\n')
            val = val.subSequence(0, val.length()-2);
        return val;
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
        if (containsImages(html)) {
            CharSequence currentEncoded = rawHtmlCache.put(id, encoded);
            // Remove full html if raw html has changed
            if (currentEncoded == null
                    || !currentEncoded.toString().equals(encoded.toString()))
                fullHtmlCache.remove(id);
        } else {
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
    public HttpImageGetter bind(final TextView view, final String html,
            final Object id) {
        if (TextUtils.isEmpty(html))
            return hide(view);

        CharSequence encoded = fullHtmlCache.get(id);
        if (encoded != null)
            return show(view, encoded);

        encoded = rawHtmlCache.get(id);
        if (encoded == null) {
            if (!html.matches("<[a-z][\\s\\S]*>")) {
                RequestMarkdownDTO markdownDTO = new RequestMarkdownDTO();
                markdownDTO.text = html;
                new GetMarkdownClient(markdownDTO)
                        .observable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new ObserverAdapter<String>() {
                            @Override
                            public void onNext(String data) {
                                continueBind(view, data, id);
                            }

                            @Override
                            public void onError(Throwable e) {
                                continueBind(view, html, id);
                            }
                        });
            } else {
                return continueBind(view, html, id);
            }
        }
        return this;
    }

    private HttpImageGetter continueBind(final TextView view, final String html, final Object id){
        CharSequence encoded = HtmlUtils.encode(html, loading);
        if (containsImages(html))
            rawHtmlCache.put(id, encoded);
        else {
            rawHtmlCache.remove(id);
            fullHtmlCache.put(id, encoded);
            return show(view, encoded);
        }

        if (TextUtils.isEmpty(encoded))
            return hide(view);

        show(view, encoded);
        view.setTag(id);
        Observable.just(html)
                .subscribeOn(Schedulers.computation())
                .map(new Func1<String, CharSequence>() {
                    @Override
                    public CharSequence call(String htmlString) {
                        return HtmlUtils.encode(htmlString, HttpImageGetter.this);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ObserverAdapter<CharSequence>() {@Override
                    public void onNext(CharSequence htmlCharSequence) {
                        fullHtmlCache.put(id, htmlCharSequence);
                        if (id.equals(view.getTag())) {
                            show(view, htmlCharSequence);
                        }
                    }
                });
        return this;
    }

    /**
     * Request an image using the contents API if the source URI is a path to a
     * file already in the repository
     *
     * @param source
     * @return
     * @throws IOException
     */
    private Drawable requestRepositoryImage(final String source)
            throws IOException {
        if (TextUtils.isEmpty(source))
            return null;

        Uri uri = Uri.parse(source);
        if (!HOST_DEFAULT.equals(uri.getHost()))
            return null;

        List<String> segments = uri.getPathSegments();
        if (segments.size() < 5)
            return null;

        String prefix = segments.get(2);
        // Two types of urls supported:
        // github.com/github/android/raw/master/app/res/drawable-xhdpi/app_icon.png
        // github.com/github/android/blob/master/app/res/drawable-xhdpi/app_icon.png?raw=true
        if (!("raw".equals(prefix) || ("blob".equals(prefix) && !TextUtils
                .isEmpty(uri.getQueryParameter("raw")))))
            return null;

        String owner = segments.get(0);
        if (TextUtils.isEmpty(owner))
            return null;
        String name = segments.get(1);
        if (TextUtils.isEmpty(name))
            return null;
        String branch = segments.get(3);
        if (TextUtils.isEmpty(branch))
            return null;

        StringBuilder path = new StringBuilder(segments.get(4));
        for (int i = 5; i < segments.size(); i++) {
            String segment = segments.get(i);
            if (!TextUtils.isEmpty(segment))
                path.append('/').append(segment);
        }

        if (TextUtils.isEmpty(path))
            return null;
        FileInfo info = new FileInfo();
        info.repoInfo = InfoUtils.createRepoInfo(InfoUtils.createRepoFromData(owner, name), branch);
        info.path = path.toString();

        GetFileContentClient service = new GetFileContentClient(info);
        Content contents = service.observable().toBlocking().first();
        if (contents.children != null && contents.children.size() == 1) {
            byte[] content = Base64.decode(contents.children.get(0).content, DEFAULT);
            Bitmap bitmap = ImageUtils.getBitmap(content, width, MAX_VALUE);
            if (bitmap == null)
                return loading.getDrawable(source);
            BitmapDrawable drawable = new BitmapDrawable(
                    context.getResources(), bitmap);
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            return drawable;
        } else
            return null;
    }

    @Override
    public Drawable getDrawable(final String source) {
        try {
            Drawable repositoryImage = requestRepositoryImage(source);
            if (repositoryImage != null)
                return repositoryImage;
        } catch (Exception e) {
            // Ignore and attempt request over regular HTTP request
        }

        try {
            String logMessage = "Loading image: " + source;
            Log.d(getClass().getSimpleName(), logMessage);
            Bugsnag.leaveBreadcrumb(logMessage);

            Request request = new Request.Builder().get().url(source).build();

            com.squareup.okhttp.Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful())
                throw new IOException("Unexpected response code: " + response.code());

            Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());

            if (bitmap == null)
                return loading.getDrawable(source);

            BitmapDrawable drawable = new BitmapDrawable( context.getResources(), bitmap);
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            return drawable;
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Error loading image", e);
            Bugsnag.notify(e);
            return loading.getDrawable(source);
        }
    }

    /**
     * Remove Object from cache store.
     * @param id
     */
    public void removeFromCache(final Object id) {
        rawHtmlCache.remove(id);
        fullHtmlCache.remove(id);
    }
}
