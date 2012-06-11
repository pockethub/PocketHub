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
package com.github.mobile.ui.issue;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.github.mobile.R.string;
import com.github.mobile.ui.ProgressDialogTask;
import com.github.mobile.util.HtmlUtils;
import com.github.mobile.util.ToastUtils;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Task to comment on an issue in a repository
 */
public class CreateCommentTask extends ProgressDialogTask<Comment> {

    private static final String TAG = "CreateCommentTask";

    private final IRepositoryIdProvider repository;

    private final int issueNumber;

    private final String comment;

    @Inject
    private IssueService service;

    /**
     * Create task for creating a comment on the given issue in the given
     * repository
     *
     * @param context
     * @param repository
     * @param issueNumber
     * @param comment
     */
    public CreateCommentTask(final Context context,
            final IRepositoryIdProvider repository, final int issueNumber,
            final String comment) {
        super(context);

        this.repository = repository;
        this.issueNumber = issueNumber;
        this.comment = comment;
    }

    @Override
    protected Comment run() throws Exception {
        Comment created = service.createComment(repository, issueNumber,
                comment);
        String formatted = HtmlUtils.format(created.getBodyHtml()).toString();
        created.setBodyHtml(formatted);
        return created;
    }

    /**
     * Create comment
     *
     * @return this task
     */
    public CreateCommentTask start() {
        dismissProgress();
        showIndeterminate(string.creating_comment);

        execute();
        return this;
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception creating comment on issue", e);

        ToastUtils.show((Activity) getContext(), e.getMessage());
    }
}
