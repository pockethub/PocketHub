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
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.WebView;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(android.webkit.WebView view,
                                                              WebResourceRequest request) {
                return shouldIntercept(request.getUrl().toString());
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(android.webkit.WebView view,
                                                              String url) {
                return shouldIntercept(url);
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

            /**
             * This method will inject polyfills to the auth javascript if the version is
             * below Lollipop. After Lollipop WebView is updated via the Play Store so the polyfills
             * are not needed.
             *
             * @param url The requests url
             * @return null if there request should not be altered or a new response
             *     instance with polyfills.
             */
            private WebResourceResponse shouldIntercept(String url) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return null;
                }

                if (url.matches(".*frameworks.*.js")) {
                    InputStream in1 = null;
                    InputStream in2 = null;
                    Response response = null;
                    try {
                        response = new OkHttpClient.Builder()
                                .build()
                                .newCall(new Request.Builder().get().url(url).build())
                                .execute();

                        if (response.body() != null) {
                            in1 = response
                                    .body()
                                    .byteStream();
                        }

                        in2 = getAssets().open("polyfills.js");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (response == null) {
                        return null;
                    }

                    SequenceInputStream inputStream = new SequenceInputStream(in2, in1);
                    return new WebResourceResponse("text/javascript", "utf-8", inputStream);
                } else {
                    return null;
                }
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
