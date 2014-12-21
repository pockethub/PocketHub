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
package com.github.mobile.ui.gist;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_COMMENTS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_GISTS;
import android.accounts.Account;
import android.app.Activity;
import android.util.Log;

import com.github.mobile.R;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.util.HtmlUtils;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.service.GistService;

/**
 * Task to edit a comment on a {@link Gist}
 */
public class EditCommentTask extends ProgressDialogTask<Comment> {

    private static final String TAG = "EditCommentTask";

    @Inject
    private GistService service;

    private final Comment comment;

    private final String gistId;

    /**
     * Edit task for editing a comment on a {@link Gist}
     *
     * @param activity
     * @param comment
     */
    protected EditCommentTask(Activity activity, String gistId, Comment comment) {
        super(activity);

        this.gistId = gistId;
        this.comment = comment;
    }

    /**
     * Execute the task and edit the comment
     *
     * @return this task
     */
    public EditCommentTask start() {
        showIndeterminate(R.string.editing_comment);
        execute();
        return this;
    }

    @Override
    public Comment run(Account account) throws Exception {
        Comment edited = editComment(gistId, comment);
        String formatted = HtmlUtils.format(edited.getBodyHtml()).toString();
        edited.setBodyHtml(formatted);
        return edited;

    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception editing comment on gist", e);

        ToastUtils.show((Activity) getContext(), e.getMessage());
    }

    /**
     * Edit gist comment.
     *
     * TODO: Remove this method once egit GistService.java Gist Comment APIs are
     * fixed. https://github.com/eclipse/egit-github/pull/7
     *
     * @param comment
     * @return edited comment
     * @throws IOException
     */
    private Comment editComment(String gistId, Comment comment)
            throws Exception {
        StringBuilder uri = new StringBuilder(SEGMENT_GISTS);
        uri.append('/').append(gistId);
        uri.append(SEGMENT_COMMENTS);
        uri.append('/').append(comment.getId());
        return service.getClient().post(uri.toString(), comment, Comment.class);
    }
}
