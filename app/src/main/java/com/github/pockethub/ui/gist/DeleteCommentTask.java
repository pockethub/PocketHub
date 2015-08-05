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
package com.github.pockethub.ui.gist;


import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMMENTS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_GISTS;
import android.accounts.Account;
import android.app.Activity;
import android.util.Log;

import com.github.pockethub.R;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.ToastUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Task to delete a comment on  a {@link Gist}
 */
public class DeleteCommentTask extends ProgressDialogTask<Comment> {

    private static final String TAG = "DeleteCommentTask";

    private final Comment comment;

    @Inject
    private GistService service;

    private final String gistId;

    /**
     * Delete task for deleting a comment on a {@link Gist}
     *
     * @param context
     * @param repository
     * @param comment
     */
    public DeleteCommentTask(Activity activity, String gistId, Comment comment) {
        super(activity);

        this.gistId = gistId;
        this.comment = comment;
    }

    @Override
    protected Comment run(Account account) throws Exception {
        deleteComment(gistId, comment.getId());
        return comment;
    }

    /**
     * Delete comment
     *
     * @return this task
     */
    public DeleteCommentTask start() {
        showIndeterminate(R.string.deleting_comment);

        execute();
        return this;
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception deleting comment on gist", e);

        ToastUtils.show((Activity) getContext(), e.getMessage());
    }

    /**
     * Delete the Gist comment with the given id
     *
     * TODO: Remove this method once egit GistService.java Gist Comment APIs are
     * fixed. https://github.com/eclipse/egit-github/pull/7
     *
     * @param commentId
     * @throws IOException
     */
    private void deleteComment(String gistId, long commentId) throws Exception {
        service.getClient().delete(SEGMENT_GISTS + '/' + gistId + SEGMENT_COMMENTS + '/' + commentId);
    }
}
