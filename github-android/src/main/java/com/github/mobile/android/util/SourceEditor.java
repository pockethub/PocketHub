package com.github.mobile.android.util;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Utilities for displaying source code in a {@link WebView}
 */
public class SourceEditor {

    /**
     * Bind {@link Object#toString()} to given {@link WebView}
     *
     * @param view
     * @param name
     * @param provider
     * @return view
     */
    public static WebView showSource(WebView view, String name, final Object provider) {
        view.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

        });
        int suffix = name.lastIndexOf('.');
        final String brush;
        if (suffix != -1 && suffix + 2 < name.length())
            brush = "brush: " + name.substring(suffix + 1) + ";";
        else
            brush = "";
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setBuiltInZoomControls(true);
        view.addJavascriptInterface(new Object() {
            public String toString() {
                return "<script type=\"syntaxhighlighter\" class=\"toolbar:false;" + brush + "\"><![CDATA[\n"
                        + provider.toString() + "\n]]></script>";
            }

        }, "SourceProvider");
        view.loadUrl("file:///android_asset/source-editor.html");
        return view;
    }
}
