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
package com.github.pockethub.ui.issue;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.request.CommentRequest;
import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.issues.EditIssueCommentClient;
import com.github.pockethub.R;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.HtmlUtils;
import com.github.pockethub.util.InfoUtils;
import com.github.pockethub.util.ToastUtils;

/**
 * Task to edit a comment on an issue in a repository
 */
public class EditCommentTask extends ProgressDialogTask<GithubComment> {

    private static final String TAG = "EditCommentTask";

    private final Repo repository;

    private final String id;

    private final String comment;

    /**
     * Edit task for editing a comment on the given issue in the given
     * repository
     *
     * @param context
     * @param repository
     * @param comment
     */
    public EditCommentTask(final Context context, final Repo repository,
            final String id, final String comment) {
        super(context);

        this.repository = repository;
        this.id = id;
        this.comment = comment;
    }

    @Override
    protected GithubComment run(Account account) throws Exception {
        GithubComment edited = new EditIssueCommentClient(
                InfoUtils.createRepoInfo(repository), id, new CommentRequest(comment)).observable().toBlocking().first();
        edited.body_html = HtmlUtils.format(edited.body_html).toString();
        return edited;
    }

    /**
     * Edit comment
     *
     * @return this task
     */
    public EditCommentTask start() {
        showIndeterminate(R.string.editing_comment);

        execute();
        return this;
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception editing comment on issue", e);

        ToastUtils.show((Activity) getContext(), e.getMessage());
    }
}
