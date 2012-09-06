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

import android.content.Context;
import android.content.Intent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.mobile.ui.UrlLauncher;

/**
 * Utilities for displaying source code in a {@link WebView}
 */
public class SourceEditor {

    private static final String URL_PAGE = "file:///android_asset/source-editor.html";

    private final WebView view;

    private boolean wrap;

    private String name;

    private Object content;

    /**
     * Create source editor using given web view
     *
     * @param view
     */
    public SourceEditor(final WebView view) {
        WebViewClient client = new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (URL_PAGE.equals(url)) {
                    view.loadUrl(url);
                    return false;
                } else {
                    Context context = view.getContext();
                    Intent intent = new UrlLauncher(context).create(url);
                    context.startActivity(intent);
                    return true;
                }
            }
        };
        view.setWebViewClient(client);

        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        view.addJavascriptInterface(this, "SourceEditor");

        this.view = view;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @return content
     */
    public String getContent() {
        return content.toString();
    }

    /**
     * @return wrap
     */
    public boolean getWrap() {
        return wrap;
    }

    /**
     * Set whether lines should wrap
     *
     * @param wrap
     * @return this editor
     */
    public SourceEditor setWrap(final boolean wrap) {
        this.wrap = wrap;
        if (name != null && content != null)
            view.loadUrl(URL_PAGE);
        return this;
    }

    /**
     * Bind {@link Object#toString()} to given {@link WebView}
     *
     * @param name
     * @param provider
     * @return this editor
     */
    public SourceEditor setSource(String name, final Object provider) {
        this.name = name;
        this.content = provider;
        view.loadUrl(URL_PAGE);
        return this;
    }

    /**
     * Toggle line wrap
     *
     * @return this editor
     */
    public SourceEditor toggleWrap() {
        return setWrap(!wrap);
    }
}
