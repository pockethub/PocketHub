package com.github.pockethub.android.ui.repo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;

import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.github.pockethub.android.Intents;
import com.github.pockethub.android.ui.DialogFragment;
import com.github.pockethub.android.ui.WebView;
import com.meisolsson.githubsdk.service.repositories.RepositoryContentService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

        final Repository repo = getParcelableExtra(Intents.EXTRA_REPOSITORY);
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.addJavascriptInterface(this, "Readme");

        ServiceGenerator.createService(getActivity(), RepositoryContentService.class)
                .getReadmeHtml(repo.owner().login(), repo.name(), null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(response -> {
                    String baseUrl = String.format("https://github.com/%s/%s/raw/%s/",
                            repo.owner().login(), repo.name(), "master");

                    String data = PAGE_START + response.body() + PAGE_END;
                    webview.loadDataWithBaseURL(baseUrl, data, "text/html", "UTF-8", null);
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
