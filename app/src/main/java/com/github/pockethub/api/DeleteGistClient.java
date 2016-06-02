package com.github.pockethub.api;

import com.alorma.github.sdk.services.client.GithubClient;

import retrofit.RestAdapter;
import retrofit.client.Response;
import rx.Observable;

public class DeleteGistClient extends GithubClient<Response> {

    private String id;

    public DeleteGistClient(String id) {
        this.id = id;
    }

    @Override
    protected Observable<Response> getApiObservable(RestAdapter restAdapter) {
        return restAdapter.create(GistService.class).deleteGist(id);
    }
}
