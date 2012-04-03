package com.github.mobile.android.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.mobile.android.issue.ViewIssueActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.Issue;

/**
 * Helper to display an HTML block in a {@link WebView}
 */
public class HtmlViewer implements Runnable {

    private static final String REGEX_ISSUE = "https?://.+/[^/]+/[^/]+/issues/(issue/)?(\\d+)";

    private static final Pattern PATTERN_ISSUE = Pattern.compile(REGEX_ISSUE);

    private static final String URL_PAGE = "file:///android_asset/html-viewer.html";

    private static final String URL_RELOAD = "javascript:reloadHtml()";

    private static final String URL_UPDATE_HEIGHT = "javascript:updateHeight()";

    private final Matcher issueMatcher = PATTERN_ISSUE.matcher("");

    private boolean inLoad;

    private int height;

    private boolean loaded;

    private final WebView view;

    private final float scale;

    private String html = "";

    /**
     * Create viewer
     *
     * @param view
     */
    public HtmlViewer(final WebView view) {
        this.view = view;
        view.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.equals(URL_PAGE)) {
                    view.loadUrl(url);
                    return false;
                } else {
                    loadExternalUrl(view.getContext(), url);
                    return true;
                }
            }

            public void onPageFinished(WebView view, String url) {
                loaded = true;
            }
        });
        view.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100)
                    view.loadUrl(URL_UPDATE_HEIGHT);
            }
        });
        view.setHorizontalScrollBarEnabled(false);
        view.setVerticalScrollBarEnabled(false);
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setBuiltInZoomControls(false);
        view.addJavascriptInterface(this, "HtmlViewer");
        view.loadUrl(URL_PAGE);
        scale = view.getScale();
    }

    private void loadExternalUrl(final Context context, final String url) {
        issueMatcher.reset(url);
        if (issueMatcher.matches()) {
            Issue issue = new Issue();
            issue.setNumber(Integer.parseInt(issueMatcher.group(2)));
            issue.setHtmlUrl(url);
            context.startActivity(ViewIssueActivity.viewIssueIntentFor(issue));
        } else
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    /**
     * Set HTML to display
     *
     * @param html
     * @return this viewer
     */
    public HtmlViewer setHtml(String html) {
        if (html == null)
            html = "";
        if (!this.html.equals(html)) {
            this.html = html;
            if (loaded) {
                inLoad = true;
                view.loadUrl(URL_RELOAD);
                inLoad = false;
                run();
            }
        }
        return this;
    }

    /**
     * @return html
     */
    public String getHtml() {
        return html;
    }

    /**
     * @return view
     */
    public WebView getView() {
        return view;
    }

    /**
     * Update height
     *
     * @param height
     */
    public void setHeight(final int height) {
        int newHeight = view.getPaddingTop() + Math.round(height * scale + 0.5F) + view.getPaddingBottom();
        this.height = newHeight;
        if (!inLoad && view.getLayoutParams().height != newHeight)
            view.post(this);
    }

    public void run() {
        int newHeight = height;
        int currentHeight = view.getLayoutParams().height;
        if (newHeight == currentHeight)
            return;
        view.getLayoutParams().height = newHeight;
        if (!view.isLayoutRequested())
            view.requestLayout();
    }
}
