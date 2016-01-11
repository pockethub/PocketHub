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
package com.github.pockethub.ui.commit;

import android.accounts.Account;
import android.app.Activity;
import android.util.Log;

import com.alorma.github.sdk.bean.dto.request.CommitCommentRequest;
import com.alorma.github.sdk.bean.dto.response.CommitComment;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.services.commit.PublishCommitCommentClient;
import com.github.pockethub.R;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.HtmlUtils;
import com.github.pockethub.util.InfoUtils;
import com.github.pockethub.util.ToastUtils;

/**
 * Task to comment on a commit
 */
public class CreateCommentTask extends ProgressDialogTask<CommitComment> {

    private static final String TAG = "CreateCommentTask";

    private final Repo repository;

    private final String commit;

    private final CommitCommentRequest comment;

    /**
     * Create task to create a comment
     *
     * @param activity
     * @param repository
     * @param commit
     * @param comment
     */
    protected CreateCommentTask(final Activity activity,
            final Repo repository, final String commit,
            final CommitCommentRequest comment) {
        super(activity);

        this.repository = repository;
        this.commit = commit;
        this.comment = comment;
    }

    /**
     * Execute the task and create the comment
     *
     * @return this task
     */
    public CreateCommentTask start() {
        showIndeterminate(R.string.creating_comment);
        execute();
        return this;
    }

    @Override
    public CommitComment run(final Account account) throws Exception {
        CommitComment created = new PublishCommitCommentClient(InfoUtils.createCommitInfo(repository, commit), comment).observable().toBlocking().first();

        created.body_html = HtmlUtils.format(created.body_html).toString();
        return created;

    }

    @Override
    protected void onException(final Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception creating comment on commit", e);

        ToastUtils.show((Activity) getContext(), e.getMessage());
    }
}
