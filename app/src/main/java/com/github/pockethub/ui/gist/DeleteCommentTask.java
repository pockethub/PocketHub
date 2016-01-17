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

import com.alorma.github.sdk.bean.dto.response.Gist;
import com.alorma.github.sdk.bean.dto.response.GithubComment;
import com.alorma.github.sdk.services.gists.DeleteGistCommentClient;
import com.github.pockethub.R;
import com.github.pockethub.ui.ProgressDialogTask;
import com.github.pockethub.util.ToastUtils;

/**
 * Task to delete a comment on  a {@link Gist}
 */
public class DeleteCommentTask extends ProgressDialogTask<GithubComment> {

    private static final String TAG = "DeleteCommentTask";

    private final GithubComment comment;

    private final String gistId;

    /**
     * Delete task for deleting a comment on a {@link Gist}
     *
     * @param activity
     * @param gistId
     * @param comment
     */
    public DeleteCommentTask(Activity activity, String gistId, GithubComment comment) {
        super(activity);

        this.gistId = gistId;
        this.comment = comment;
    }

    @Override
    protected GithubComment run(Account account) throws Exception {
        new DeleteGistCommentClient(gistId, comment.id).observable().toBlocking();
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
}
