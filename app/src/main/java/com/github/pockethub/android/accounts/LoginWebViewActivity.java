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

package com.github.pockethub.android.accounts;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.WebView;

public class LoginWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);

        // Needs the be activated to allow GitHub to perform their requests.
        webView.getSettings().setJavaScriptEnabled(true);

        String userAgent = webView.getSettings().getUserAgentString();
        // Remove chrome from the user agent since GitHub checks it incorrectly
        userAgent = userAgent.replaceAll("Chrome/\\d{2}\\.\\d\\.\\d\\.\\d", "");
        webView.getSettings().setUserAgentString(userAgent);

        String url = getIntent().getStringExtra(LoginActivity.INTENT_EXTRA_URL);
        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient() {
            MaterialDialog dialog = new MaterialDialog.Builder(LoginWebViewActivity.this)
                    .content(R.string.loading)
                    .progress(true, 0)
                    .build();

            @Override
            public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
                dialog.show();
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                dialog.dismiss();
            }

            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                Uri uri = Uri.parse(url);
                return overrideOAuth(uri) || super.shouldOverrideUrlLoading(view, url);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view,
                                                    WebResourceRequest request) {

                return overrideOAuth(request.getUrl())
                        || super.shouldOverrideUrlLoading(view, request);
            }

            private boolean overrideOAuth(Uri uri) {
                if (uri.getScheme().equals(getString(R.string.github_oauth_scheme))) {
                    Intent data = new Intent();
                    data.setData(uri);
                    setResult(RESULT_OK, data);
                    finish();
                    return true;
                }

                return false;
            }
        });

        setContentView(webView);
    }
}
