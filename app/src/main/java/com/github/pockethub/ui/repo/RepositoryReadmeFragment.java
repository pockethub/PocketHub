package com.github.pockethub.ui.repo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.widget.Toast;

import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.repo.GetReadmeContentsClient;
import com.github.pockethub.Intents;
import com.github.pockethub.rx.ObserverAdapter;
import com.github.pockethub.ui.DialogFragment;
import com.github.pockethub.ui.WebView;
import com.github.pockethub.util.InfoUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RepositoryReadmeFragment extends DialogFragment {

    private static final String PAGE_START = "<!DOCTYPE html><html lang=\"en\"> <head> <title></title>" +
            "<meta charset=\"UTF-8\"> " +
            "<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;\"/>" +
            "<script src=\"intercept.js\"></script>" +
            "<link href=\"github.css\" rel=\"stylesheet\"> </head> <body>";

    private static final String PAGE_END = "</body></html>";
    private WebView webview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new WebView(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webview = (WebView) view;

        Repo repo = getParcelableExtra(Intents.EXTRA_REPOSITORY);
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.addJavascriptInterface(this, "Readme");

        new GetReadmeContentsClient(InfoUtils.createRepoInfo(repo))
                .observable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<String>bindToLifecycle())
                .subscribe(new ObserverAdapter<String>() {
                    @Override
                    public void onNext(String s) {
                        super.onNext(s);
                        String data = PAGE_START + s + PAGE_END;
                        webview.loadDataWithBaseURL("file:///android_asset/", data, "text/html", "UTF-8", null);
                    }
                });
    }

    @JavascriptInterface
    public void startIntercept() {
        webview.startIntercept();
    }

    @JavascriptInterface
    public void stopIntercept() {
        webview.stopIntercept();
    }
}
