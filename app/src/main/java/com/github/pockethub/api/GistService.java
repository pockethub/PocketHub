package com.github.pockethub.api;

import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.PUT;
import retrofit.http.Path;
import rx.Observable;

public interface GistService {

    @GET("/gists/{id}/star")
    Observable<Response> checkIfStarred(@Path("id") String id);

    @PUT("/gists/{id}/star")
    Observable<Response> starGist(@Path("id") String id);

    @DELETE("/gists/{id}/star")
    Observable<Response> unstarGist(@Path("id") String id);

    @DELETE("/gists/{id}")
    Observable<Response> deleteGist(String id);
}
