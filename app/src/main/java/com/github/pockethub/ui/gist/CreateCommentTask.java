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
import com.alorma.github.sdk.services.gists.PublishGistCommentClient;
import com.github.pockethub.R;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.HtmlUtils;
import com.github.pockethub.util.ToastUtils;

/**
 * Task to comment on a {@link Gist}
 */
public class CreateCommentTask extends ProgressDialogTask<GithubComment> {

    private static final String TAG = "CreateCommentTask";

    private final String id;

    private final String comment;

    /**
     * Create task to create a comment
     *
     * @param activity
     * @param gistId
     * @param comment
     */
    protected CreateCommentTask(Activity activity, String gistId, String comment) {
        super(activity);

        this.id = gistId;
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
    public GithubComment run(Account account) throws Exception {
        GithubComment created = new PublishGistCommentClient(id, new CommentRequest(comment)).observable().toBlocking().first();
        created.body_html = HtmlUtils.format(created.body_html).toString();
        return created;

    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        super.onException(e);

        Log.d(TAG, "Exception creating comment on gist", e);

        ToastUtils.show((Activity) getContext(), e.getMessage());
    }
}
