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
package com.github.pockethub.android.core.gist;

import android.app.Activity;
import android.content.Context;

import com.github.pockethub.android.util.HttpImageGetter;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.service.gists.GistCommentService;
import com.meisolsson.githubsdk.service.gists.GistService;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import retrofit2.Response;
import retrofit2.http.Body;
import roboguice.RoboGuice;

/**
 * Task to load and store a {@link Gist}.
 */
public class RefreshGistTask {

    private final Context context;
    private final GistService service;

    @Inject
    private GistStore store;

    private final String id;

    private final HttpImageGetter imageGetter;

    /**
     * Create task to refresh the given {@link Gist}.
     *
     * @param gistId
     * @param imageGetter
     */
    public RefreshGistTask(Activity activity, String gistId,
                           HttpImageGetter imageGetter) {
        id = gistId;
        this.imageGetter = imageGetter;
        this.context = activity;
        this.service = ServiceGenerator.createService(context, GistService.class);
        RoboGuice.injectMembers(activity, this);
    }

    public Single<FullGist> refresh() {
        Single<Gist> gistSingle = store.refreshGist(id);
        Single<List<GitHubComment>> commentSingle = getGistComments();
        Single<Boolean> starredSingle = service.checkIfGistIsStarred(id)
                .map(response -> response.code() == 204);

        return Single.zip(gistSingle, starredSingle, commentSingle, FullGist::new);
    }

    Single<List<GitHubComment>> getGistComments() {
        return ServiceGenerator.createService(context, GistCommentService.class)
                .getGistComments(id, 0)
                .flatMapObservable(response -> Observable.fromIterable(response.body().items()))
                .map(comment -> {
                    imageGetter.encode(comment, comment.bodyHtml());
                    return comment;
                })
                .toList();

    }
}
