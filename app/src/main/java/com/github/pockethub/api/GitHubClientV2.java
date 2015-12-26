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

package com.github.pockethub.api;

import com.github.pockethub.model.Authorization;

import org.eclipse.egit.github.core.client.GitHubClient;

import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

public class GitHubClientV2 extends GitHubClient {
    private static String API_URL = "https://api.github.com";
    private static GitHubClientV2Interface sGitHubClientInterface;

    public static GitHubClientV2Interface getServiceClient() {
        if (sGitHubClientInterface == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            request.addHeader("Accept", "application/vnd.github.v3.full+json");
                        }
                    })
                    .setEndpoint(API_URL)
                    .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("Retrofit"))
                    .build();

            sGitHubClientInterface = restAdapter.create(GitHubClientV2Interface.class);
        }

        return sGitHubClientInterface;
    }

    public interface GitHubClientV2Interface {
        @DELETE("/repos/{owner}/{repo}")
        Response deleteRepository(
                @Header("Authorization") String basicCredentials,
                @Path("owner") String owner,
                @Path("repo") String repo);

        @GET("/authorizations")
        List<Authorization> getAuthorizations(@Header("Authorization") String token);

        @POST("/authorizations")
        Authorization createDeleteAuthorization(@Header("Authorization") String basicCredentials,
                                                @Body Authorization authorization);
    }
}
