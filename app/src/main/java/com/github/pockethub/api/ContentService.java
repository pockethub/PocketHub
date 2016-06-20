package com.github.pockethub.api;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.HEAD;
import retrofit.http.Path;
import rx.Observable;

public interface ContentService {

    /**
     * Get's readme content. Note it's a HTTP HEAD so only headers will be returned for checking
     * the existences
     * @param owner
     * @param repo
     * @return
     */
    @HEAD("/repos/{owner}/{repo}/readme")
    Observable<Response> hasReadme(@Path("owner") String owner, @Path("repo") String repo);

}
