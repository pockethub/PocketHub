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
package com.github.pockethub.core.gist;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.services.gists.GetGistCommentsClient;
import com.github.pockethub.accounts.AuthenticatedUserTask;
import com.github.pockethub.api.CheckGistStarredClient;
import com.github.pockethub.util.HtmlUtils;
import com.github.pockethub.util.HttpImageGetter;
import com.google.inject.Inject;

import java.util.Collections;
import java.util.List;

/**
 * Task to load and store a {@link Gist}
 */
public class RefreshGistTask extends AuthenticatedUserTask<FullGist> {

    private static final String TAG = "RefreshGistTask";

    @Inject
    private GistStore store;

    private final String id;

    private final HttpImageGetter imageGetter;

    /**
     * Create task to refresh the given {@link Gist}
     *
     * @param context
     * @param gistId
     * @param imageGetter
     */
    public RefreshGistTask(Context context, String gistId,
            HttpImageGetter imageGetter) {
        super(context);

        id = gistId;
        this.imageGetter = imageGetter;
    }

    @Override
    public FullGist run(Account account) throws Exception {
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

        return new FullGist(gist, client.observable().toBlocking().first(), comments);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception loading gist", e);
    }
}
