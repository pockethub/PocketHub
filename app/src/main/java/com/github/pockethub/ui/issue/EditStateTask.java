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

import com.alorma.github.sdk.bean.dto.response.Issue;
import com.alorma.github.sdk.bean.dto.response.IssueState;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.github.pockethub.R;
import com.github.pockethub.core.issue.IssueStore;
import com.github.pockethub.ui.ConfirmDialogFragment;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.ui.ProgressDialogTask;
import com.google.inject.Inject;

import static com.github.pockethub.RequestCodes.ISSUE_CLOSE;
import static com.github.pockethub.RequestCodes.ISSUE_REOPEN;

/**
 * Task to close or reopen an issue
 */
public class EditStateTask extends ProgressDialogTask<Issue> {

    @Inject
    private IssueStore store;

    private final Repo repositoryId;

    private final int issueNumber;

    private boolean close;

    /**
     * Create task to edit issue state
     *
     * @param activity
     * @param repositoryId
     * @param issueNumber
     */
    public EditStateTask(final DialogFragmentActivity activity,
            final Repo repositoryId, final int issueNumber) {
        super(activity);

        this.repositoryId = repositoryId;
        this.issueNumber = issueNumber;
    }

    /**
     * Confirm action
     *
     * @param close
     * @return this task
     */
    public EditStateTask confirm(boolean close) {
        if (close)
            ConfirmDialogFragment.show((DialogFragmentActivity) getContext(),
                    ISSUE_CLOSE, getString(R.string.issue_confirm_close_title),
                    getString(R.string.issue_confirm_close_message));
        else
            ConfirmDialogFragment.show((DialogFragmentActivity) getContext(),
                    ISSUE_REOPEN, getString(R.string.issue_confirm_reopen_title),
                    getString(R.string.issue_confirm_reopen_message));

        return this;
    }

    @Override
    protected Issue run(Account account) throws Exception {
        IssueState state;
        if (close)
            state = IssueState.closed;
        else
            state = IssueState.open;
        return store.changeState(repositoryId, issueNumber, state);
    }

    /**
     * Edit state of issue
     *
     * @param close
     * @return this task
     */
    public EditStateTask edit(boolean close) {
        if (close)
            showIndeterminate(R.string.closing_issue);
        else
            showIndeterminate(R.string.reopening_issue);

        this.close = close;

        execute();
        return this;
    }
}
