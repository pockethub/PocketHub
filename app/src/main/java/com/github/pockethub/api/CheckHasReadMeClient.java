package com.github.pockethub.api;

import com.alorma.github.sdk.bean.info.RepoInfo;
import com.alorma.github.sdk.services.client.GithubClient;

import retrofit.RestAdapter;
import retrofit.client.Response;
import rx.Observable;

public class CheckHasReadMeClient extends GithubClient<Response> {

    private RepoInfo info;

    public CheckHasReadMeClient(RepoInfo info) {
        this.info = info;
    }

    @Override
    protected Observable<Response> getApiObservable(RestAdapter restAdapter) {
        return restAdapter.create(ContentService.class).hasReadme(info.owner, info.name);
    }
}
