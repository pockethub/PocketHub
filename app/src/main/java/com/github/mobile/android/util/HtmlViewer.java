package com.github.mobile.android.util;

import static android.content.Intent.ACTION_VIEW;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.mobile.android.core.gist.GistUrlMatcher;
import com.github.mobile.android.core.issue.IssueUrlMatcher;
import com.github.mobile.android.gist.ViewGistsActivity;
import com.github.mobile.android.issue.ViewIssueActivity;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.Issue;

/**
 * Helper to display an HTML block in a {@link WebView}
 */
public class HtmlViewer implements Runnable {

    private static final String URL_PAGE = "file:///android_asset/html-viewer.html";

    private static final String URL_RELOAD = "javascript:reloadHtml()";

    private static final String URL_UPDATE_HEIGHT = "javascript:updateHeight()";

    private final IssueUrlMatcher issueMatcher = new IssueUrlMatcher();

    private final GistUrlMatcher gistMatcher = new GistUrlMatcher();

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
        int issueNumber = issueMatcher.getNumber(url);
        if (issueNumber > 0) {
            Issue issue = new Issue();
            issue.setNumber(issueNumber);
            issue.setHtmlUrl(url);
            context.startActivity(ViewIssueActivity.createIntent(issue));
            return;
        }

        String gistId = gistMatcher.getId(url);
        if (gistId != null) {
            Gist gist = new Gist().setId(gistId).setHtmlUrl(url);
            context.startActivity(ViewGistsActivity.createIntent(gist));
            return;
        }

        context.startActivity(new Intent(ACTION_VIEW, Uri.parse(url)));
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
