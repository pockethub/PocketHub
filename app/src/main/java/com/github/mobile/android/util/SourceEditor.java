package com.github.mobile.android.util;

import android.content.Intent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.mobile.android.ui.UrlLauncher;

/**
 * Utilities for displaying source code in a {@link WebView}
 */
public class SourceEditor {

    private static final String URL_PAGE = "file:///android_asset/source-editor.html";

    /**
     * Does the source editor have a highlighter set to match the given file name extension?
     *
     * @param extension
     * @return true if highlighting available, false otherwise
     */
    public static boolean isValid(String extension) {
        return "actionscript3".equals(extension) //
                || "applescript".equals(extension) //
                || "as3".equals(extension) //
                || "bash".equals(extension) //
                || "c".equals(extension) //
                || "cf".equals(extension) //
                || "coldfusion".equals(extension) //
                || "cpp".equals(extension) //
                || "cs".equals(extension) //
                || "css".equals(extension) //
                || "delphi".equals(extension) //
                || "diff".equals(extension) //
                || "erl".equals(extension) //
                || "erlang".equals(extension) //
                || "groovy".equals(extension) //
                || "html".equals(extension) //
                || "java".equals(extension) //
                || "js".equals(extension) //
                || "pas".equals(extension) //
                || "pascal".equals(extension) //
                || "patch".equals(extension) //
                || "pl".equals(extension) //
                || "php".equals(extension) //
                || "py".equals(extension) //
                || "rb".equals(extension) //
                || "sass".equals(extension) //
                || "scala".equals(extension) //
                || "scss".equals(extension) //
                || "sh".equals(extension) //
                || "sql".equals(extension) //
                || "txt".equals(extension) //
                || "vb".equals(extension) //
                || "vbnet".equals(extension) //
                || "xhtml".equals(extension) //
                || "xml".equals(extension) //
                || "xslt".equals(extension);
    }

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
                if (url.equals(URL_PAGE)) {
                    view.loadUrl(url);
                    return false;
                } else {
                    Intent intent = new UrlLauncher().create(url);
                    view.getContext().startActivity(intent);
                    return true;
                }
            }

        });
        int suffix = name.lastIndexOf('.');
        String ext = null;
        if (suffix != -1 && suffix + 2 < name.length()) {
            ext = name.substring(suffix + 1);
            if (!isValid(ext))
                ext = null;
        }
        if (ext == null)
            ext = "txt";
        final String brush = "brush: " + ext + ";";
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setBuiltInZoomControls(true);
        view.addJavascriptInterface(new Object() {
            public String toString() {
                return "<script type=\"syntaxhighlighter\" class=\"toolbar:false;" + brush + "\"><![CDATA[\n"
                        + provider.toString() + "\n]]></script>";
            }

        }, "SourceProvider");
        view.loadUrl(URL_PAGE);
        return view;
    }
}
