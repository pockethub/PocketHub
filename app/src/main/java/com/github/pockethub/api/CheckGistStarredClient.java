package com.github.pockethub.api;

import com.alorma.github.sdk.services.client.GithubClient;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.functions.Func1;

public class CheckGistStarredClient extends GithubClient<Boolean> {

    private String id;

    public CheckGistStarredClient(String id) {
        this.id = id;
    }

    @Override
    protected Observable<Boolean> getApiObservable(RestAdapter restAdapter) {
        return restAdapter.create(GistService.class)
                .checkIfStarred(id)
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends Response>>() {
                    @Override
                    public Observable<? extends Response> call(Throwable throwable) {
                        if (throwable instanceof RetrofitError) {
                            return Observable.just(((RetrofitError) throwable).getResponse());
                        }
                        return Observable.error(throwable);
                    }
                })
                .map(new Func1<Response, Boolean>() {
                    @Override
                    public Boolean call(Response r) {
                        return r != null && r.getStatus() == 204;
                    }
                });
    }
}
