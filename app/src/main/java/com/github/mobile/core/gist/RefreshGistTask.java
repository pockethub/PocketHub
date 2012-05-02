/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.core.gist;

import android.content.Context;
import android.util.Log;

import com.github.mobile.accounts.AuthenticatedUserTask;
import com.github.mobile.util.HtmlUtils;
import com.google.inject.Inject;

import java.util.Collections;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Task to load and store a {@link Gist}
 */
public class RefreshGistTask extends AuthenticatedUserTask<FullGist> {

    private static final String TAG = "RefreshGistTask";

    @Inject
    private GistStore store;

    @Inject
    private GistService service;

    private final String id;

    /**
     * Create task to refresh the given {@link Gist}
     *
     * @param context
     * @param gistId
     */
    public RefreshGistTask(Context context, String gistId) {
        super(context);

        id = gistId;
    }

    @Override
    public FullGist run() throws Exception {
        Gist gist = store.refreshGist(id);
        List<Comment> comments;
        if (gist.getComments() > 0)
            comments = service.getComments(id);
        else
            comments = Collections.emptyList();
        for (Comment comment : comments)
            comment.setBodyHtml(HtmlUtils.format(comment.getBodyHtml()).toString());
        return new FullGist(gist, service.isStarred(id), comments);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception loading gist", e);
    }
}