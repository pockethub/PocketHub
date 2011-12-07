package com.github.mobile.android.util;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Utilities for displaying source code in a {@link WebViewClient}
 */
public class SourceEditor {

    /**
     * Bind {@link Object#toString()} to given {@link WebView}
     *
     * @param view
     * @param provider
     * @return view
     */
    public static WebView showSource(WebView view, final Object provider) {
        view.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

        });
        view.getSettings().setJavaScriptEnabled(true);
        view.addJavascriptInterface(provider, "SourceProvider");
        view.loadUrl("file:///android_asset/source-editor.html");
        return view;
    }
}
