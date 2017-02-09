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

import retrofit2.Response;
import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;

/**
 * Task to load and store a {@link Gist}
 */
public class RefreshGistTask implements Observable.OnSubscribe<FullGist> {

    private final Context context;

    @Inject
    private GistStore store;

    private final String id;

    private final HttpImageGetter imageGetter;

    /**
     * Create task to refresh the given {@link Gist}
     *
     * @param gistId
     * @param imageGetter
     */
    public RefreshGistTask(Activity activity, String gistId,
                           HttpImageGetter imageGetter) {
        id = gistId;
        this.imageGetter = imageGetter;
        this.context = activity;
        RoboGuice.injectMembers(activity, this);
    }

    @Override
    public void call(Subscriber<? super FullGist> subscriber) {
        try {
            Gist gist = store.refreshGist(id);
            List<GitHubComment> comments;
            if (gist.comments() > 0)
                comments = ServiceGenerator.createService(context, GistCommentService.class).getGistComments(id, 0).toBlocking().first().items();
            else
                comments = Collections.emptyList();

            for (GitHubComment comment : comments) {
                imageGetter.encode(comment, comment.bodyHtml());
            }
            Response<Boolean> response = ServiceGenerator.createService(context, GistService.class).checkIfGistIsStarred(id).toBlocking().first();
            boolean starred = response.code() == 204;


            subscriber.onNext(new FullGist(gist, starred, comments));
        }catch (IOException e){
            subscriber.onError(e);
        }
    }
}
