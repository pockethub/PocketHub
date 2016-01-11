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
package com.github.pockethub.ui.gist;

import android.accounts.Account;
import android.app.Activity;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.request.CommentRequest;
import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.services.gists.EditGistCommentClient;
import com.github.pockethub.R;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.HtmlUtils;
import com.github.pockethub.util.ToastUtils;

/**
 * Task to edit a comment on a {@link Gist}
 */
public class EditCommentTask extends ProgressDialogTask<GithubComment> {

    private static final String TAG = "EditCommentTask";

    private final String commentId;

    private final String body;

    private final String gistId;

    /**
     * Edit task for editing a comment on a {@link Gist}
     *
     * @param activity
     * @param commentId
     */
    protected EditCommentTask(Activity activity, String gistId, String commentId, String body) {
        super(activity);

        this.gistId = gistId;
        this.commentId = commentId;
        this.body = body;
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
    public GithubComment run(Account account) throws Exception {
        GithubComment edited = new EditGistCommentClient(gistId, commentId, new CommentRequest(body)).observable().toBlocking().first();
        edited.body_html = HtmlUtils.format(edited.body_html).toString();
        return edited;

    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception editing comment on gist", e);

        ToastUtils.show((Activity) getContext(), e.getMessage());
    }
}
