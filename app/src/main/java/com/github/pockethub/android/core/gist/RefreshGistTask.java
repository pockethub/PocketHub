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

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.services.gists.GetGistCommentsClient;
import com.github.pockethub.android.api.CheckGistStarredClient;
import com.github.pockethub.android.util.HtmlUtils;
import com.github.pockethub.android.util.HttpImageGetter;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;

/**
 * Task to load and store a {@link Gist}
 */
public class RefreshGistTask implements Observable.OnSubscribe<FullGist> {

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
        RoboGuice.injectMembers(activity, this);
    }

    @Override
    public void call(Subscriber<? super FullGist> subscriber) {
        try {
            Gist gist = store.refreshGist(id);
            List<GithubComment> comments;
            if (gist.comments > 0)
                comments = new GetGistCommentsClient(id).observable().toBlocking().first().first;
            else
                comments = Collections.emptyList();
            for (GithubComment comment : comments) {
                String formatted = HtmlUtils.format(comment.body_html)
                        .toString();
                comment.body_html = formatted;
                imageGetter.encode(comment, formatted);
            }
            CheckGistStarredClient client = new CheckGistStarredClient(id);

            subscriber.onNext(new FullGist(gist, client.observable().toBlocking().first(), comments));
        }catch (IOException e){
            subscriber.onError(e);
        }
    }
}
