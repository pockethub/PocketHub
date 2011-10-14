package com.github.android.app.authenticator;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

class OAuth2CaptureWebViewClient extends WebViewClient {
    private static final String TAG = "OAuthWebViewClient";
    private final String successRedirectUrl= "https://github.com/rtyley/agit";

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.i(TAG, "Processing webview url click...");

        if (url.contains(successRedirectUrl)) {
            Uri uri = Uri.parse(url);
            String code = uri.getQueryParameter("code");
            Log.i(TAG,"got code ="+code);
        } else {
            view.loadUrl(url);
        }
        return true;
    }

    public void onPageFinished(WebView view, String url) {
        Log.i(TAG, "Finished loading URL: " +url);
//                if (progressBar.isShowing()) {
//                    progressBar.dismiss();
//                }
    }

    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Log.e(TAG, "Error: " + description);
        Toast.makeText(view.getContext(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
//                alertDialog.setTitle("Error");
//                alertDialog.setMessage(description);
//                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        return;
//                    }
//                });
//                alertDialog.show();
    }
}
